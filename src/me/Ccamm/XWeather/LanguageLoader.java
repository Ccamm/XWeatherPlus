package me.Ccamm.XWeather;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class LanguageLoader 
{
	private static LanguageLoader loader = null;
	private FileConfiguration language;
	private HashMap<String, String> variables = new HashMap<String, String>();
	
	private LanguageLoader()
	{
		variables.put("<update>", "");
		variables.put("<world>", "");
		variables.put("<prefix>", "ChatPrefix");
		variables.put("<weather>", "WeatherTypes");
		variables.put("<tornado_location>", "Stop Having Sneaky Looks At My Code");
		variables.put("<set>", "ChatCommands.set");
		variables.put("<stop>", "ChatCommands.stop");
		variables.put("<reload>", "ChatCommands.reload");
		variables.put("<print_all_weathertypes>", "WeatherTypes");
		variables.put("<player>", "");
		
		getLanguage();
	}
	
	public static LanguageLoader setupLoader()
	{
		if(loader == null) {
			loader = new LanguageLoader();
		} else {
			loader.getLanguage();
		}
		return loader;
	}
	
	public String lineInterpreter(String msg, String special)
	{
		String message = ChatColor.translateAlternateColorCodes('&', msg);
		if(message.indexOf('<') == -1) {return message;}
		for(String v : variables.keySet()) {
			if(v.equals("<print_all_weathertypes>")) {continue;}
			if(v.equals("<prefix>") || v.equals("<set>") || v.equals("<stop>") || v.equals("<reload>")) {
				message = message.replaceAll(v, language.getString(variables.get(v))); 
			} else {
				message = message.replaceAll(v, special);
			}
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String lineInterpreter(String msg, String special, String[] ... varmatch) {
		String message = msg;
		for(String[] match : varmatch) {
			message = message.replace(match[0], match[1]);
		}
		return lineInterpreter(message, special);
	}
	
	public boolean doesPrintAllWeather(String msg)
	{
		if(msg.indexOf("<print_all_weathertypes>") == -1) {
			return false;
		}
		return true;
	}
	
	public FileConfiguration getLanguage()
	{
		File languagefile = new File(Main.getPlugin().getDataFolder(), "language.yml");
		if(!languagefile.exists()) {
			Bukkit.getServer().getLogger().info(Main.getPrefix() + "Could not find language.yml, making a new one now");
			Main.getPlugin().saveResource("language.yml", false);
		}
		language = YamlConfiguration.loadConfiguration(languagefile);
		if(isMissing()) {
			replaceOldLanguage();
		}
		return language;
	}
	
	private void replaceOldLanguage()
	{
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "You are missing some options in your language.yml.");
		Bukkit.getServer().getLogger().info(Main.getPrefix() + "Saving old language file and making a new one.");
		File oldl = new File(Main.getPlugin().getDataFolder(), "oldlanguage.yml");
		if(!oldl.exists()) {
			try {
				oldl.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			language.save(oldl);
		} catch(IOException e) {
			e.printStackTrace();
		}
		Main.getPlugin().saveResource("language.yml", true);
		File languagefile = new File(Main.getPlugin().getDataFolder(), "language.yml");
		language = YamlConfiguration.loadConfiguration(languagefile);
	}
	
	private boolean isMissing()
	{
		YamlConfiguration deflang = YamlConfiguration.loadConfiguration(
				new InputStreamReader(Main.getPlugin().getResource("language.yml")));
		for(Entry<String, Object> entry : deflang.getValues(true).entrySet()) {
			if(!language.isSet(entry.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	public static void sendMessage(String langsection, String special, Player p, boolean sendtoconsole)
	{
		String message = ChatColor.translateAlternateColorCodes('&', Main.getLanguageLoader().lineInterpreter(
				Main.getLanguageLoader().getLanguage().getString(langsection), special));
		if(sendtoconsole) {Bukkit.getServer().getLogger().info(ChatColor.stripColor(message));}
		if(p != null && p.isOnline()) {
			p.sendMessage(message);
		}
	}
	
	public static void sendMessage(String langsection, String special, Player p, boolean sendtoconsole,
			String[] ...varmatch)
	{
		String message = ChatColor.translateAlternateColorCodes('&', Main.getLanguageLoader().lineInterpreter(
				Main.getLanguageLoader().getLanguage().getString(langsection), special, varmatch));
		if(sendtoconsole) {Bukkit.getServer().getLogger().info(ChatColor.stripColor(message));}
		if(p != null && p.isOnline()) {
			p.sendMessage(message);
		}
	}
}
