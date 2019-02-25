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
	private static boolean debugmode;
	
	@Override
	public void onEnable()
	{
		plugin = this;
		configloader = new ConfigLoader();
		FileConfiguration config = configloader.loadConfig();
		debugmode = config.getBoolean("Debug");
		languageloader = LanguageLoader.setupLoader();
		WeatherHandler.setUpHandler(config);
		this.getCommand("xweather").setExecutor(new Commands());
		/*Add after first upload of the pro version to do update checks
		 * Remember that there is a section in Events.java too
		update = new Updater(this, 62733, config);
		
		update.checkForUpdates(null);*/
		
		Bukkit.getServer().getPluginManager().registerEvents(new Events(), this);
		Bukkit.getServer().getLogger().info(pluginprefix + "Finished loading plugin.");
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
	public static boolean isDebug() {return debugmode;}
}
