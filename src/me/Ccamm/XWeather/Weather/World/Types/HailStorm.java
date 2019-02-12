package me.Ccamm.XWeather.Weather.World.Types;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;

import me.Ccamm.XWeather.Main;
import me.Ccamm.XWeather.Weather.WeatherHandler;
import me.Ccamm.XWeather.Weather.World.WorldWeather;

public class HailStorm extends WorldWeather
{
	private static HailStorm hailstorm = null;
	private static int playerradius;
	private static int particlecount;
	private static boolean damageentities;
	private static int damage;
	private static boolean sound;
	private static double heightdif = 25;
	private static double spawnaboveplayer = 0.3;
	
	private HailStorm(FileConfiguration config)
	{
		super("HailStorm", config, 10);
		WorldWeather.addWeatherType(this);
	}
	
	public static HailStorm setUpHailStorms(FileConfiguration config)
	{
		if(hailstorm == null) {
			hailstorm = new HailStorm(config);
		} else {
			hailstorm.loadConfig(config);
			hailstorm.reloadName();
		}
		return hailstorm;
	}
	
	@Override
	public void loadMoreOptions(FileConfiguration config)
	{
		playerradius = config.getInt("HailStorm.RadiusAroundPlayer");
		particlecount = config.getInt("HailStorm.ParticleCount");
		damageentities = config.getBoolean("HailStorm.DamageEntities");
		sound = config.getBoolean("HailStorm.Sound");
		damage = config.getInt("HailStorm.Damage");
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setRain(w, dur);
	}
	
	@Override
	protected void weatherEffect(World w)
	{
		for(Player p : w.getPlayers()) {
			if(WeatherHandler.locationIsProtected(p.getLocation())) {continue;}
			spawnHail(p);
			moveEntities(p, playerradius);
		 }
	}
	
	private static void spawnHail(Player p)
	{
		Random r = new Random();
		Location loc = p.getLocation().clone();
		double dx;
		double dz;
		int n = 0;
		Snowball sb;
		
		if(r.nextDouble() <= spawnaboveplayer) {
			n++;
			dx = 1.5*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			dz = 1.5*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			loc.add(dx, 0, dz);
			if(WeatherHandler.isLocationLoaded(loc) && !WeatherHandler.locationIsProtected(loc)) {
				loc.setY(p.getWorld().getMaxHeight());
				loc.subtract(0, heightdif*r.nextDouble(), 0);
				sb = (Snowball) loc.getWorld().spawnEntity(loc, EntityType.SNOWBALL);
				sb.setMetadata("hailstone", new FixedMetadataValue(Main.getPlugin(), damageentities));
			}
		}
		
		for(int i = 0; i < particlecount-n; i++) {
			dx = playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			dz = playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			loc.add(dx, 0, dz);
			if(!WeatherHandler.isLocationLoaded(loc)
					|| WeatherHandler.locationIsProtected(loc)) {
				continue;
			}
			loc.setY(p.getWorld().getMaxHeight());
			loc.subtract(0, heightdif*r.nextDouble(), 0);
			sb = (Snowball) loc.getWorld().spawnEntity(loc, EntityType.SNOWBALL);
			sb.setMetadata("hailstone", new FixedMetadataValue(Main.getPlugin(), damageentities));
		}
	}
	
	public static int getDamage() {return damage;}
	public static boolean canDamage() {return damageentities;}
	public static boolean makeSound() {return sound;}
}
