package me.Ccamm.XWeatherPlus.Weather.World.Types;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.World.WorldWeather;

public class ShootingStars extends WorldWeather
{
	private static ShootingStars shootingstars = null;
	private static int playerradius;
	private static int particlecount;
	private static double starsize;
	private static double shootingchance;
	private static double maxstarspeed;
	private static double minstarspeed;
	private static double starheight;
	private static double dd;
	
	private ShootingStars(FileConfiguration config)
	{
		super("ShootingStars", config, 40);
		loadConfig(config);
		WorldWeather.addWeatherType(this);
	}
	
	public static ShootingStars setUpShootingStars(FileConfiguration config)
	{
		if(shootingstars == null) {
			shootingstars = new ShootingStars(config);
		} else {
			shootingstars.loadConfig(config);
		}
		return shootingstars;
	}
	
	@Override 
	public void loadMoreOptions(FileConfiguration config)
	{
		playerradius = config.getInt("ShootingStars.RadiusAroundPlayer");
		particlecount = config.getInt("ShootingStars.ParticleCount");
		shootingchance = config.getDouble("ShootingStars.ChanceOfStar");
		starsize = config.getDouble("ShootingStars.StarSize");
		maxstarspeed = config.getDouble("ShootingStars.MaxStarSpeed");
		minstarspeed = config.getDouble("ShootingStars.MinStarSpeed");
		starheight = config.getDouble("ShootingStars.StarHeight");
		dd = (double) starsize/Math.sqrt(particlecount);
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setSunny(w, dur);
	}
	
	@Override
	protected void weatherEffect(World w)
	{
		Random r = new Random();
		float time = w.getTime();
		if(time < 13000 || time > 23000) {return;}
		
		for(Player p : w.getPlayers()) {
			if(r.nextDouble() < shootingchance) {
				shootingStar(p);
			}
		}
	}
	
	private void shootingStar(Player p)
	{
		Random r = new Random();
		Location loc = p.getLocation().clone();
		
		double dx = playerradius*r.nextDouble()*(r.nextBoolean()?1:-1);
		double dz = playerradius*r.nextDouble()*(r.nextBoolean()?1:-1);
		double speed = (maxstarspeed-minstarspeed)*r.nextDouble() + minstarspeed;
		loc.add(dx, 0, dz);
		if(!WeatherHandler.isLocationLoaded(loc)) {return;}
		if(loc.getY() + starheight >= p.getWorld().getMaxHeight()) {
			loc.setY(p.getWorld().getMaxHeight());
		} else {
			loc.add(0,starheight,0);
		}
		
		double[] v = WeatherHandler.normalisedvector(
				r.nextDouble()*(r.nextBoolean() ? 1:-1),
				0.0,
				r.nextDouble()*(r.nextBoolean() ? 1:-1));

		for(double x = 0; x <= starsize; x += dd) {
			for(double z = 0; z <= starsize; z += dd) {
				loc.add(x,0,z);
				p.spawnParticle(Particle.FIREWORKS_SPARK, loc, 0, v[0], v[1], v[2], speed);
				loc.subtract(x,0,z);
			}
		}
	}
}
