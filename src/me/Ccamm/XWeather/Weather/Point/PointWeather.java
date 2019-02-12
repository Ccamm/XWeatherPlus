package me.Ccamm.XWeather.Weather.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.Ccamm.XWeather.Main;
import me.Ccamm.XWeather.Weather.Point.Types.Tornado;

public class PointWeather implements PointWeatherType
{
	protected static HashSet<PointWeatherType> weatherevents = new HashSet<PointWeatherType>();
	
	protected Location location;
	protected int currentduration;
	protected boolean currentlyrunning;
	protected int totaldur;
	private int tickrate;
	
	protected PointWeather(Location location, int tickrate, int dur)
	{
		this.totaldur = dur;
		this.location = location;
		this.tickrate = tickrate;
		this.currentlyrunning = true;
		this.currentduration = 0;
	}

	public void start() 
	{
		weatherStart();
		
		new BukkitRunnable() {
			@Override
			public void run()
			{
				if(currentlyrunning == false || currentduration > totaldur || weatherEffect()) {
					stopEffect();
					cancel();
				}
			}
		}.runTaskTimer(Main.getPlugin(), 0, tickrate);
	}

	public void stop() 
	{
		currentlyrunning = false;
	}
	
	public static void setUp(FileConfiguration config)
	{
		Tornado.loadConfig(config);
	}
	
	public static boolean checkCommand(CommandSender sender, String[] args)
	{
		if(args[0].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("ChatCommands.set"))) {
			if(Tornado.onCommandSet(sender, args)) {
				return true;
			}
		} else if(args[0].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("ChatCommands.stop"))) {
			if(Tornado.onCommandStop(sender, args)) {
				return true;
			}
		}
		return false;
	}
	
	public static void stopAll()
	{
		for(PointWeatherType pwt : weatherevents) {
			pwt.stop();
		}
		weatherevents.clear();
	}
	
	public static void stopWeather(Class<?> pwtclass)
	{
		List<PointWeatherType> remove = new ArrayList<PointWeatherType>();
		for(PointWeatherType pwt : weatherevents) {
			if(pwtclass.isInstance(pwt)) {
				pwt.stop();
				remove.add(pwt);
			}
		}
		
		for(PointWeatherType pwt : remove) {
			weatherevents.remove(pwt);
		}
	}
	
	public static void naturalStart(World world)
	{
		Tornado.naturalStart(world);
	}
	
	protected String locationString()
	{
		return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
	}
	
	protected void weatherStart() {}
	protected void stopEffect() {}
	protected boolean weatherEffect() {return false;}
	protected void weatherEnd() {}
}
