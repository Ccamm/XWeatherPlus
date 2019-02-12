package me.Ccamm.XWeatherPlus;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;

public class Main extends JavaPlugin
{
	private static Plugin plugin;
	private static String pluginprefix = "[XWeatherPlus] ";
	private static Updater update;
	private static ConfigLoader configloader;
	private static LanguageLoader languageloader;
	
	@Override
	public void onEnable()
	{
		Plugin freeversion = Bukkit.getPluginManager().getPlugin("XWeather");
		if(freeversion != null) {
			Bukkit.getServer().getLogger().info(pluginprefix + "Detected that XWeather is loaded. Disabling so only XWeatherPlus will run");
			Bukkit.getServer().getLogger().info(pluginprefix + "To prevent this message from popping up each start up delete XWeather.jar");
			Bukkit.getServer().getLogger().info(pluginprefix + "You can copy your old config settings for XWeather straight into the XWeatherPlus config.yml");
			Bukkit.getServer().getLogger().info(pluginprefix + "The plugin will automatically update it with all of the new options");
			Bukkit.getPluginManager().disablePlugin(freeversion);
		}
		
		plugin = this;
		configloader = new ConfigLoader();
		FileConfiguration config = configloader.loadConfig();
		languageloader = LanguageLoader.setupLoader();
		WeatherHandler.setUpHandler(config);
		this.getCommand("xweather").setExecutor(new Commands());
		/*Add after first upload of the pro version to do update checks
		update = new Updater(this, 62733, config);
		
		update.checkForUpdates(null);*/
		
		Bukkit.getServer().getPluginManager().registerEvents(new Events(), this);
	}
	
	@Override
	public void onLoad()
	{
		try {
			WorldGuardManager.setUp();
		} catch(NoClassDefFoundError e) {}
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
