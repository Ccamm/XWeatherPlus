package me.Ccamm.XWeatherPlus.Weather.World.Types;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.Wind;
import me.Ccamm.XWeatherPlus.Weather.World.WorldWeather;

public class Windy extends WorldWeather
{
	private static int particlecount;
	private static int playerradius;
	private static double speed = 0.8;
	private static Windy windystorm = null;
	private static boolean startsandstorm = false;
	
	private Windy(FileConfiguration config) 
	{
		super("Windy", config, 1);
		loadConfig(config);
		WorldWeather.addWeatherType(this);
	}
	
	public static Windy setUpWindystorms(FileConfiguration config)
	{
		if(windystorm == null) {
			windystorm = new Windy(config);
		} else {
			windystorm.loadConfig(config);
			windystorm.reloadName();
		}
		return windystorm;
	}
	
	@Override
	public void loadMoreOptions(FileConfiguration config)
	{
		particlecount = config.getInt("Windy.ParticleCount");
		speed = config.getDouble("Windy.ParticleSpeed");
		playerradius = config.getInt("Windy.RadiusAroundPlayer");
		startsandstorm = config.getBoolean("Windy.StartSandstorm");
	}
	
	@Override
	protected void weatherEffect(World w)
	{
		for(Player p : w.getPlayers()) {
			if(WeatherHandler.isDesert(p.getLocation()) || WeatherHandler.locationIsProtected(p.getLocation())) {
				continue;
			}
			
			spawnParticle(p);
			moveEntities(p, playerradius);
		}
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setSunny(w, dur);
		if(startsandstorm) {
			getWeatherType("SandStorm").start(dur, w);
		}
	}
	
	private void spawnParticle(Player p)
	{
		Random r = new Random();
		double dx, dy, dz;
		Location loc = p.getLocation().clone();
		double[] vp = Wind.getParticleVelocity(p.getWorld(), 0.0);
		
		
		for(int i = 0; i < particlecount; i++) {
			dx = playerradius*r.nextDouble()*(r.nextBoolean()? 1:-1);
			dz = playerradius*r.nextDouble()*(r.nextBoolean()? 1:-1);
			loc.add(dx, 0, dz);
			
			dy = WeatherHandler.getHighestY(loc) + 5*r.nextDouble();
			
			if(dy < 0) {dy = 0;}
			if(dy > p.getWorld().getMaxHeight()) {dy = p.getWorld().getMaxHeight();}
			
			loc.setY(dy);
			if(!WeatherHandler.isDesert(loc) && WeatherHandler.isLocationLoaded(loc)) {
				p.spawnParticle(Particle.SMOKE_NORMAL, loc, 0, vp[0],vp[1],vp[2], speed);
			}
		}
	}
}
