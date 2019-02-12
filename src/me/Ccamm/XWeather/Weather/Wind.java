package me.Ccamm.XWeather.Weather;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.Ccamm.XWeather.Main;

public class Wind 
{
	private static HashSet<Wind> wind = new HashSet<Wind>();
	private static int windupdateperiod;
	private static double modifier = 0.05;
	private static int updateperiod;
	private static int currentperiod = 0;
	private static double entityamp = 63.64;
	private static boolean alreadyrunning = false;
	private static boolean constantwinddir;
	
	private World world;
	private double[] windvec;
	private double[] nextwindvec;
	
	public Wind(World world)
	{
		Random r = new Random();
		this.world = world;
		if(constantwinddir) {
			this.windvec = WeatherHandler.normalisedvector(45.0, 0.0, -45.0);
		} else {
			this.windvec = WeatherHandler.normalisedvector(
					r.nextDouble()*(r.nextBoolean() ? 1:-1), 0.0, r.nextDouble()*(r.nextBoolean() ? 1:-1));
		}
		
		this.nextwindvec = windvec;
		wind.add(this);
	}
	
	public static void loadWind(FileConfiguration config)
	{
		windupdateperiod = config.getInt("WeatherControl.Wind.WindChangePeriod")*20;
		updateperiod = config.getInt("WeatherControl.Wind.UpdateWindPeriod")*20;
		constantwinddir = !config.getBoolean("WeatherControl.Wind.CanChangeDirection");
		setUpWorlds();
		windChange();
	}
	
	private static void setUpWorlds()
	{
		for(World w : WeatherHandler.getWorlds()) {
			if(getWindInstance(w) == null) {
				new Wind(w);
			}
		}
	}
	
	private static void windChange()
	{
		if(alreadyrunning) {
			Bukkit.getServer().getLogger().info(Main.getPrefix() + "A server restart is required for some changes to occur.");
			return;
		}
		alreadyrunning = true;
		
		if(constantwinddir) {return;}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				currentperiod += updateperiod;
				if(windupdateperiod < currentperiod) {
					currentperiod = 0;
					changeWind();
				}
				updateAllWind();
			}
		}.runTaskTimer(Main.getPlugin(), updateperiod, updateperiod); //Might need to change to synchronously
	}
	
	private static void updateAllWind()
	{
		for(Wind windobject : wind) {
			windobject.updateWind();
		}
	}
	
	private void updateWind()
	{
		windvec = WeatherHandler.normalisedvector(
				windvec[0] + modifier*(nextwindvec[0] - windvec[0]),
				0.0,
				windvec[2] + modifier*(nextwindvec[2] - windvec[2]));
	}
	
	private static void changeWind()
	{
		Random r = new Random();
		for(Wind windobject : wind) {
			windobject.changeNextWindDirection(r.nextDouble()*(r.nextBoolean() ? 1:-1), r.nextDouble()*(r.nextBoolean() ? 1:-1));
		}
	}
	
	public static Wind getWindInstance(World world)
	{
		for(Wind windobject : wind) {
			if(windobject.getWorld().equals(world)) {
				return windobject;
			}
		}
		return null;
	}
	
	public static double[] getParticleVelocity(World world, double ymod) {
		Wind windobject = getWindInstance(world);
		if(windobject == null) {
			Bukkit.getServer().getLogger().warning(Main.getPrefix() + "World does not have a wind instance assigned to it!");
			return null;
		}
		double[] v = windobject.getWindVelocity();
		v[1] = ymod/entityamp;
		return WeatherHandler.normalisedvector(v);
	}
	
	public static double[] getPlayerVelocity(World world, float speed)
	{
		Wind windobject = getWindInstance(world);
		if(windobject == null) {
			Bukkit.getServer().getLogger().warning(Main.getPrefix() + "World does not have a wind instance assigned to it!");
			return null;
		}
		double[] v = windobject.getWindVelocity();
		//((float) entityamp)*
		return WeatherHandler.vector3D(v[0], v[1], v[2], speed);
	}
	
	public void changeNextWindDirection(double x, double z)
	{
		nextwindvec = WeatherHandler.normalisedvector(x, 0.0, z);
	}
	
	private World getWorld() {return world;}
	private double[] getWindVelocity() {return windvec;}
}
