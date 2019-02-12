package me.Ccamm.XWeather.Weather.World.Types;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Ccamm.XWeather.Weather.WeatherHandler;
import me.Ccamm.XWeather.Weather.Wind;
import me.Ccamm.XWeather.Weather.World.WorldWeather;

public class SandStorm extends WorldWeather
{
	private static int particlecount; //Remember that this is counted 7 times
	private static int playerradius;
	private static double speed = 0.9; // 0.9
	private static double height = 5.0;
	private static SandStorm sandstorm = null;
	
	private SandStorm(FileConfiguration config) 
	{
		super("SandStorm", config, 1);
		WorldWeather.addWeatherType(this);
	}
	
	public static SandStorm setUpSandstorms(FileConfiguration config)
	{
		if(sandstorm == null) {
			sandstorm = new SandStorm(config);
		} else {
			sandstorm.loadConfig(config);
			sandstorm.reloadName();
		}
		return sandstorm;
	}
	
	@Override
	protected void loadMoreOptions(FileConfiguration config)
	{
		particlecount = config.getInt("SandStorm.ParticleCount")/7;
		speed = config.getDouble("SandStorm.ParticleSpeed");
		playerradius = config.getInt("SandStorm.RadiusAroundPlayer");
	}
	
	@Override
	protected void weatherEffect(World world)
	{
		for(Player p : world.getPlayers()) {
			if(!WeatherHandler.isDesert(p.getLocation())) {continue;}
			
			if(WeatherHandler.locationIsProtected(p.getLocation())) {continue;}
			
			for(double i = 1.0; i  <= 7.0 ; i += 1.0) {
				spawnSand(p, i);
			}
			moveEntities(p, playerradius);
		}
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setRain(w, dur);
	}
	
	private static void spawnSand(Player p, double heightmod)
	{
		Random r = new Random();
		double dx;
		double dz;
		Location loc = p.getLocation().clone();
		Block b;
		double[] v = Wind.getParticleVelocity(p.getWorld(), heightmod*height);
		for(int i = 0; i < particlecount; i++) {
			dx = playerradius*r.nextDouble()*(r.nextBoolean()? 1:-1);
			dz = playerradius*r.nextDouble()*(r.nextBoolean()? 1:-1);
			loc.add(dx, 0, dz);
			loc.setY(WeatherHandler.getHighestY(loc)-1);
			b = loc.getBlock();
			if(WeatherHandler.isDesert(loc) && b.getType() == Material.SAND && WeatherHandler.isLocationLoaded(loc)) {
				p.spawnParticle(Particle.CLOUD, loc.add(0,1,0), 0, v[0],v[1],v[2], speed);
			}
		}
	}
}
