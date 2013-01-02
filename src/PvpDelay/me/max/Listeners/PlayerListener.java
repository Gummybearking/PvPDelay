package PvpDelay.me.max.Listeners;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import PvpDelay.me.max.PDMain;

public class PlayerListener implements Listener{
	public PDMain plugin;
	public PlayerListener(PDMain p) {
		plugin = p;
	}
	
	public static HashMap<String, String> Commanded    = new HashMap<String, String>();
	public static HashMap<String, Integer> ShouldDelay = new HashMap<String, Integer>();
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String command = event.getMessage().replace("/", "");
		Player p       = event.getPlayer();
		List<String> list = plugin.getConfig().getStringList("To-Delay");
		for(String s : list) {
			if(command.startsWith(s)) {
				Commanded.put(p.getName(), null);
			}
		}
	}
	@EventHandler
	public static void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(Commanded.containsKey(p.getName())) {
			Commanded.remove(p.getName());
		}
	}
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		String minutes = plugin.getConfig().getString("Delay.Minutes");
		String seconds = plugin.getConfig().getString("Delay.Seconds");
		int min   = Integer.valueOf(minutes);
		int sec   = Integer.valueOf(seconds);
		Player p = e.getPlayer();
		if(e.getCause() != TeleportCause.COMMAND) return;
		if(Commanded.containsKey(p.getName())) {
			
			int value = ((min * 60) + sec);
			Commanded.remove(p.getName());
			ShouldDelay.put(p.getName(), value);
		}
	}
//	@EventHandler
//	public static void onSpellCast(SpellCastEvent e) {
//		Player p = e.getCaster();
//		if(ShouldDelay.containsKey(p.getName())){
//			e.setCancelled(true);
//			
//			p.sendMessage(
//				ChatColor.translateAlternateColorCodes('&', "&7You cannot cast spells for &6" + ShouldDelay.get(p.getName())+ " Seconds")
//			);
//		}
//	}
//	@EventHandler
//	public static void onSpellTarget(SpellTargetEvent e) {
//		LivingEntity le = e.getTarget();
//		if(!(le instanceof Player))
//			return;
//		Player p = (Player) le;
//		if(ShouldDelay.containsKey(p.getName())){
//			e.setCancelled(true);
//			
//			p.sendMessage(
//				ChatColor.translateAlternateColorCodes('&', "&7You cannot attack this person &6" + ShouldDelay.get(p.getName())+ " Seconds")
//			);
//		}
//	}
	@EventHandler
	public void onPvP(EntityDamageByEntityEvent event) {
		Entity e = event.getEntity();
		Entity d = event.getDamager();
		if(e instanceof Player && d instanceof Player) {
			if(ShouldDelay.containsKey(((Player) e).getName())){
				event.setCancelled(true);
				Player a = (Player) e;
				Player b = (Player) d;
				
				msg(a,   "Damaged-While-Protected");
				
				msg(b,a, "Attacks-While-Protected");
			}
			if(ShouldDelay.containsKey(((Player) d).getName())) {
				event.setCancelled(true);
				Player a = (Player) d;
				Player b = (Player) e;
				
				msg(b, a, "Damaged-While-Protected");
				
				msg(a,    "Attacks-While-Protected");
			}
		}else if(e instanceof Player && d instanceof Arrow) {
			if(ShouldDelay.containsKey(((Player) e).getName())){
				event.setCancelled(true);
				Player a = (Player) e;				
				msg(a,   "Damaged-While-Protected");				
			}
		}
	}
	@EventHandler
	public void onPotionThrown(PotionSplashEvent e) {
		ThrownPotion p = e.getPotion();
		boolean isDangerous = false;
		for(PotionEffect eff : p.getEffects()) {
			if(
					eff.getType() == PotionEffectType.POISON||
					eff.getType() == PotionEffectType.BLINDNESS||
					eff.getType() == PotionEffectType.SLOW||
					eff.getType() == PotionEffectType.CONFUSION||
					eff.getType() == PotionEffectType.HARM||
					eff.getType() == PotionEffectType.HUNGER||
					eff.getType() == PotionEffectType.WEAKNESS||
					eff.getType() == PotionEffectType.SLOW_DIGGING||
					eff.getType() == PotionEffectType.WEAKNESS
			) {
				isDangerous = true;
			}
		}
		if(!isDangerous)
			return;
		if(!(p.getShooter() instanceof Player))
			return;
		if(ShouldDelay.containsKey(((Player)p.getShooter()).getName())) {
			msg(((Player)p.getShooter()), "Attacks-While-Protected");
			e.setCancelled(true);		
		}
		for(Entity en : e.getAffectedEntities()) {
			if(en instanceof Player) {
				if(ShouldDelay.containsKey(((Player)en).getName())){
					e.setCancelled(true);
					msg( ((Player)en) , "Attacks-While-Protected");
				}
			}
		}
	}
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
		List<String> materials = plugin.getConfig().getStringList("Interaction-Blocks");
		if(!materials.contains(p.getItemInHand().getType().toString()))
				return;
		if(ShouldDelay.containsKey(p.getName())){
			e.setCancelled(true);
			msg( p , "Attacks-While-Protected");
		} else {
			if(playerTooNear(p)) {
				e.setCancelled(true);
				msg( p , "Too-Close");
			}
		}
	}
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(e.getCause() != DamageCause.BLOCK_EXPLOSION && e.getCause() != DamageCause.ENTITY_EXPLOSION)
			return;
		if(!(e.getEntity() instanceof Player))
			return;
		
		Player p = (Player) e.getEntity();
		
		if(!ShouldDelay.containsKey(p.getName()))
			return;
		e.setCancelled(true);
		msg(p,   "Damaged-While-Protected");				
	}
	public void msg(Player p, Player b, String s) {
		String Message = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages." + s));
		if(Message.contains("%t")) {
			int time = ShouldDelay.get(b.getName());
			int mins = (int) Math.floor(time/60);
			int secs = time%60;
			if(secs == 0 && mins > 0) {
				Message = Message.replace("%t", mins + " minutes");
			} else if(secs > 0 && mins > 0) {
				Message = Message.replace("%t", mins + " minutes and " + secs + " seconds");
			}else if(secs > 0 && mins == 0) {
				Message = Message.replace("%t", secs + " seconds");
			}else{
			}
		}
		p.sendMessage(Message);
	}
	public void msg(Player p, String s) {
		msg(p,p,s);
	}
	public boolean playerTooNear(Player pl) {
		Location l = pl.getLocation();
		for(String player : ShouldDelay.keySet()) {
			Player p = Bukkit.getPlayer(player);
			if(p.getWorld().equals(l.getWorld()) && p.getLocation().distance(l) <= 5)
				return true;
		
		}
		return false;
	}
	
}
