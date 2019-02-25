package me.Ccamm.XWeatherPlus.Weather.World.Types;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.World.Puddle;
import me.Ccamm.XWeatherPlus.Weather.World.WorldWeather;

public class HeavyRain extends WorldWeather
{
	private static HeavyRain heavyrain = null;
	
	private static int particlecount;
	private static int playerradius;
	private static double puddlechance;
	
	private HeavyRain(FileConfiguration config)
	{
		super("HeavyRain", config, 1);
		WorldWeather.addWeatherType(this);
	}
	
	public static HeavyRain setUpHeavyRain(FileConfiguration config)
	{
		if(heavyrain == null) {
			heavyrain = new HeavyRain(config);
		} else {
			heavyrain.loadConfig(config);
			heavyrain.reloadName();
		}
		return heavyrain;
	}
	
	@Override
	protected void loadMoreOptions(FileConfiguration config)
	{
		particlecount = config.getInt("HeavyRain.ParticleCount");
		playerradius = config.getInt("HeavyRain.RadiusAroundPlayer");
		puddlechance = config.getDouble("HeavyRain.PuddleChance");
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setRain(w, dur);
	}
	
	@Override
	protected void weatherEffect(World w)
	{
		Random r = new Random();
		for(Player p : w.getPlayers()) {
			if(WeatherHandler.locationIsProtected(p.getLocation())
					|| WeatherHandler.isDryBiome(p.getLocation())
					|| WeatherHandler.isSnowBiome(p.getLocation())) {continue;}
			spawnExtraWater(p);
			if(runningworlds.get(w) % 20 == 0 && r.nextDouble() <= puddlechance) {
				spawnPuddle(p);
			}
		}
	}
	
	private void spawnPuddle(Player p)
	{
		Random r = new Random();
		Location loc = p.getLocation().clone();
		double dx, dz;
		dx = playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
		dz = playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
		loc.add(dx, 0, dz);
		loc.setY(WeatherHandler.getHighestY(loc));
		if(!WeatherHandler.isLocationLoaded(loc)
				|| WeatherHandler.isDryBiome(loc)
				|| WeatherHandler.locationIsProtected(loc)
				|| WeatherHandler.isSnowBiome(loc)) {
			return;
		}
		new Puddle(loc, this);
	}
	
	private void spawnExtraWater(Player p)
	{
		Random r = new Random();
		Location loc = p.getLocation().clone();
		double dx, dz;
		
		for(int i = 0; i < particlecount; i++) {
			dx = playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			dz = playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			loc.add(dx, 0, dz);
			loc.setY(WeatherHandler.getHighestY(loc));
			if(!WeatherHandler.isLocationLoaded(loc)
					|| WeatherHandler.isDryBiome(loc)
					|| WeatherHandler.locationIsProtected(loc)
					|| WeatherHandler.isSnowBiome(loc)) {
				continue;
			}
			waterDrop(loc);
		}
	}
	
	private void waterDrop(Location loc)
	{
		loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 5);
		loc.getWorld().playSound(loc, Sound.WEATHER_RAIN, (float) 0.5, 0);
	}
	
	@Override
	protected void endWeather(World world) 
	{
		Puddle.dryUpPuddles(world);
		if(rainafter) {
			WeatherHandler.setRain(world, duration);
		} else {
			WeatherHandler.setSunny(world, duration);
		}
	}
}
