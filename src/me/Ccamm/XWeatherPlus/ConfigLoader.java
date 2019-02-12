package me.Ccamm.XWeatherPlus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigLoader 
{	
	public FileConfiguration loadConfig()
	{
		Main.getPlugin().saveDefaultConfig();

		if(isMissing()) {
			newreplaceOldConfig();
		}
		return Main.getPlugin().getConfig();
	}
	
	private boolean isMissing()
	{
		YamlConfiguration defconfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Main.getPlugin().getResource("config.yml")));
        for (Entry<String, Object> entry : defconfig.getValues(true).entrySet()) {
            if(!Main.getPlugin().getConfig().isSet(entry.getKey())) {
            	return true;
            }
        }
		return false;
	}
	
	public void newreplaceOldConfig()
	{
		replaceOldConfig();
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "Saving your old options to the new config file.");
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "WARNING: This may not save all previous options if paths have been changed from a previous version.");
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "To help the missing paths will be printed to help you check");
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "If you need comments the default config has been saved to defaultconfig.yml");
		File newcf = new File(Main.getPlugin().getDataFolder(), "config.yml");
		File oldcf = new File(Main.getPlugin().getDataFolder(), "oldconfig.yml");
		YamlConfiguration newconfig = YamlConfiguration.loadConfiguration(newcf);
		YamlConfiguration oldconfig = YamlConfiguration.loadConfiguration(oldcf);
		
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "Missing config paths:");
		for(Entry<String, Object> entry : newconfig.getValues(true).entrySet()) {
			if(!oldconfig.isSet(entry.getKey())) {
				newconfig.set(entry.getKey(), entry.getValue());
				Bukkit.getServer().getLogger().info(Main.getPrefix() + "\t- " + entry.getKey());
			} else {
				newconfig.set(entry.getKey(), oldconfig.get(entry.getKey()));
			}
		}
		
		try {
			newconfig.save(newcf);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		try {
			saveDefConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		try {
			addComments();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	private void saveDefConfig() throws Exception
	{
		File defaultconfig = new File(Main.getPlugin().getDataFolder(), "defaultconfig.yml");
		if(defaultconfig.exists()) {
			defaultconfig.delete();
			defaultconfig.createNewFile();
		} else {
			defaultconfig.createNewFile();
		}
		
		BufferedReader resourcereader = new BufferedReader(new InputStreamReader(Main.getPlugin().getResource("config.yml")));
		BufferedWriter defaultwriter = new BufferedWriter(new FileWriter(defaultconfig));
		
		String line;
		while((line = resourcereader.readLine()) != null) {
			defaultwriter.write(line + "\r\n");
		}
		defaultwriter.close();
		resourcereader.close();
	}
	
	public void replaceOldConfig()
	{
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "There has been some updates to the config.yml");
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "Saving the old config to oldconfig.yml and making a new one.");
		File oldcf = new File(Main.getPlugin().getDataFolder(), "oldconfig.yml");
		if(!oldcf.exists()) {
			try {
				oldcf.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileConfiguration oldc = Main.getPlugin().getConfig();
		
		try {
			oldc.save(oldcf);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		File cf = new File(Main.getPlugin().getDataFolder(), "config.yml");
		if(cf.exists()) {
			cf.delete();
		}
		
		Main.getPlugin().saveDefaultConfig();
	}
	
	public FileConfiguration reloadCon() 
	{
		Main.getPlugin().saveDefaultConfig();
		Main.getPlugin().reloadConfig();
		return Main.getPlugin().getConfig();
	}
}
