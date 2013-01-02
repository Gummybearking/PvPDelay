package PvpDelay.me.max;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import PvpDelay.me.max.Listeners.PlayerListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import PvpDelay.me.max.Listeners.PlayerListener;

public class PDMain extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");
	public void onEnable() {
		PluginDescriptionFile pdffile = this.getDescription();
		this.logger.info(pdffile.getName() + " Has Been Enabled!");
		File config = new File(this.getDataFolder(), "config.yml");
		if(!config.exists()) {
			this.saveDefaultConfig();
			System.out.println("[PvpDelay] Generating a new config");
		}
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
            	HashMap<String, Integer> Map = PlayerListener.ShouldDelay;
            	for(Object o : Map.keySet().toArray()) {
            		String s = (String) o;
            		int x = PlayerListener.ShouldDelay.get(s);
            		if(x <= 0)
            			PlayerListener.ShouldDelay.keySet().remove(s);
            		else {
            			PlayerListener.ShouldDelay.put(s,x-1);
            		}
          
            	}
            }
        }, 0, 20);

	}		
	public void onDisable() {
		PluginDescriptionFile pdffile = this.getDescription();
		this.logger.info(pdffile.getName() + " Has Been Disabled!");
	}
	public void msg(Player p, Player b, String s) {
		PlayerListener pl = new PlayerListener(this);
		pl.msg(p, b, s);
	}
	public void msg(Player p, String s) {
		msg(p,p,s);
	}
	public void debug(String s) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.isOp()) p.sendMessage(s);
		}
	}
}
