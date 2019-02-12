package me.Ccamm.XWeatherPlus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Updater 
{
	 private int project = 0;
	 private URL checkURL;
	 private String newVersion = "";
	 private JavaPlugin plugin;
	 private boolean checkupdate;
	 
	 public Updater(JavaPlugin plugin, int projectID, FileConfiguration config) 
	 {
		 this.plugin = plugin;
		 this.newVersion = plugin.getDescription().getVersion();
		 this.project = projectID;
		 this.checkupdate = config.getBoolean("CheckForUpdates");
		 try {
			 this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectID);
	     } catch (MalformedURLException e) {
	     }
	 }
	 
	 public String getResourceURL() 
	 {
		 return "https://www.spigotmc.org/resources/" + project;
	 }
	 
	 public void checkForUpdates(Player p)
	 {
		 if(!checkupdate) {return;}
		 
		 new BukkitRunnable()
		 {
			 @Override
			 public void run()
			 {
				 URLConnection con;
				try {
					con = checkURL.openConnection();
					newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
					LanguageLoader.sendMessage("ChatMessages.checkupdate", getResourceURL(), null, true);
					if(!plugin.getDescription().getVersion().equals(newVersion)) {
						LanguageLoader.sendMessage("ChatMessages.update", getResourceURL(), p, true);
					} else {
			        	LanguageLoader.sendMessage("ChatMessages.noupdate", getResourceURL(), null, true);
			        }
				} catch (IOException e) {
					LanguageLoader.sendMessage("ChatMessages.failupdate", getResourceURL(), null, true);
				}
			 }
		 }.runTaskAsynchronously(plugin);
	 }
	 
	 public boolean canCheckUpdate() 
	 {
		 return checkupdate;
	 }
}