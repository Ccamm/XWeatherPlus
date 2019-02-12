package me.Ccamm.XWeather;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.Ccamm.XWeather.Weather.WeatherHandler;

public class Main extends JavaPlugin
{
	private static Plugin plugin;
	private static String pluginprefix = "[XWeather] ";
	private static Updater update;
	private static ConfigLoader configloader;
	private static LanguageLoader languageloader;
	
	@Override
	public void onEnable()
	{
		plugin = this;
		configloader = new ConfigLoader();
		FileConfiguration config = configloader.loadConfig();
		languageloader = LanguageLoader.setupLoader();
		WeatherHandler.setUpHandler(config);
		this.getCommand("xweather").setExecutor(new Commands());
		update = new Updater(this, 62733, config);
		
		update.checkForUpdates(null);
		
		Bukkit.getServer().getPluginManager().registerEvents(new Events(), this);
	}
	
	@Override
	public void onLoad()
	{
		try {
			WorldGuardManager.setUp();
		} catch(NoClassDefFoundError e) {
			Bukkit.getServer().getLogger().info("No World Guard :(");
		}
	}
	
	@Override
	public void onDisable()
	{
		
	}
	
	public static FileConfiguration reloadCon() 
	{
		WeatherHandler.stopAll();
		languageloader = LanguageLoader.setupLoader();
		return configloader.reloadCon();
	}
	
	public static Plugin getPlugin() {return plugin;}
	public static String getPrefix() {return pluginprefix;}
	public static Updater getUpdater() {return update;}
	public static LanguageLoader getLanguageLoader() {return languageloader;}
}
