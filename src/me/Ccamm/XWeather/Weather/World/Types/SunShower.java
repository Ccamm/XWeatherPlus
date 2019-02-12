package me.Ccamm.XWeather.Weather.World.Types;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Ccamm.XWeather.Weather.WeatherHandler;
import me.Ccamm.XWeather.Weather.World.WorldWeather;

public class SunShower extends WorldWeather
{
	private static SunShower sunshower = null;
	private static int particlecount;
	private static int playerradius;
	
	private SunShower(FileConfiguration config)
	{
		super("SunShower", config, 1);
		loadConfig(config);
		WorldWeather.addWeatherType(this);
	}
	
	public static SunShower setUpSunShower(FileConfiguration config)
	{
		if(sunshower == null) {
			sunshower = new SunShower(config);
		} else {
			sunshower.loadConfig(config);
			sunshower.reloadName();
		}
		return sunshower;
	}
	
	@Override
	public void loadMoreOptions(FileConfiguration config)
	{
		particlecount = config.getInt("SunShower.ParticleCount");
		playerradius = config.getInt("SunShower.RadiusAroundPlayer");
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setSunny(w, dur);
	}
	
	@Override
	protected void weatherEffect(World w)
	{
		for(Player p : w.getPlayers()) {
			if(WeatherHandler.isDryBiome(p.getLocation())) {
				continue;
			}
			spawnParticle(p);
		}
	}
	
	private void spawnParticle(Player p)
	{
		Random r = new Random();
		Location loc = p.getLocation().clone();
		double dx, dz;
		
		for(int i = 0; i < particlecount; i++) {
			dx = playerradius*r.nextDouble()*(r.nextBoolean()? 1:-1);
			dz = playerradius*r.nextDouble()*(r.nextBoolean()? 1:-1);
			loc.add(dx, 0, dz);
			if(!WeatherHandler.isLocationLoaded(loc)) {continue;}
			loc.setY(p.getWorld().getMaxHeight());
			p.spawnParticle(Particle.DRIP_WATER, loc, 1);
		}
	}
}
