package me.Ccamm.XWeatherPlus.Weather.World.Types;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.Point.Types.EarthQuakeCrack;
import me.Ccamm.XWeatherPlus.Weather.World.WorldWeather;

public class EarthQuake extends WorldWeather
{
	private static EarthQuake earthquake = null;
	private static HashMap<EarthQuakeCrack, World> cracks = new HashMap<EarthQuakeCrack, World>();
	
	private static int blockfallradius;
	private static int dropsperplayer;
	private static double chanceunderneath;
	private static int distanceunderneath;
	private static double fallchance;
	private static double randomdropchance = 0.5;
	private static boolean crackenabled;
	private static int cracktime;
	private static double crackchance;
	private static int crackdistance;
	
	private EarthQuake(FileConfiguration config)
	{
		super("EarthQuake", config, 5);
		WorldWeather.addWeatherType(this);
	}
	
	public static EarthQuake setUpEarthQuake(FileConfiguration config)
	{
		if(earthquake == null) {
			earthquake = new EarthQuake(config);
		} else {
			earthquake.loadConfig(config);
			earthquake.reloadName();
		}
		return earthquake;
	}
	
	@Override
	protected void loadMoreOptions(FileConfiguration config)
	{
		blockfallradius = config.getInt("EarthQuake.RadiusAroundPlayer");
		dropsperplayer = config.getInt("EarthQuake.DropsPerPlayer");
		chanceunderneath = config.getDouble("EarthQuake.ChanceUnderneath");
		fallchance = config.getDouble("EarthQuake.FallChance");
		distanceunderneath = config.getInt("EarthQuake.BlockDepthUnderneath");
		crackenabled = config.getBoolean("EarthQuake.Crack.Enabled");
		cracktime = config.getInt("EarthQuake.Crack.TimeBetweenCracks")*20;
		crackchance = config.getDouble("EarthQuake.Crack.CrackChance");
		crackdistance = config.getInt("EarthQuake.Crack.FromPlayer");
		EarthQuakeCrack.setupEarthQuakes(config);
	}
	
	@Override
	protected void weatherEffect(World world)
	{
		Random r = new Random();
		Location loc;
		double dx,dz;
		Block b;
		for(Player p : world.getPlayers()) {
			loc = p.getLocation().clone();
			for(int i = 0; i < dropsperplayer; i++) {
				if(r.nextDouble() > fallchance) {continue;}
				dx = blockfallradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
				dz = blockfallradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
				loc.setX(loc.getX() + dx);
				loc.setZ(loc.getZ() + dz);
				loc.setY((r.nextDouble() <= chanceunderneath 
						? loc.getY()-distanceunderneath*r.nextDouble():loc.getBlockY()));
				if(loc.getY() <= 0) {
					loc.setY(1);
				}
				b = blockFinder(loc);
				if(!WeatherHandler.locationIsProtected(b.getLocation())
						&& WeatherHandler.isLocationLoaded(b.getLocation())) {
					loc = b.getLocation();
					if(shouldNotIgnore(loc)) {
						loc.getWorld().spawnFallingBlock(loc, b.getBlockData());
						loc.getBlock().setType(Material.AIR);
						loc.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, (float) 0.2, (float) 0.5);
					}
					blockDrop(loc, r);
				}
			}
		}
		
		if(crackenabled
				&& !world.getPlayers().isEmpty()
				&& runningworlds.get(world) % cracktime == 0
				&& runningworlds.get(world) - cracktime > 0
				&& r.nextDouble() <= crackchance) {
			loc = world.getPlayers().iterator().next().getLocation().clone();
			dx = crackdistance*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			dz = crackdistance*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			loc.setX(loc.getX() + dx);
			loc.setZ(loc.getZ() + dz);
			if(!WeatherHandler.locationIsProtected(loc)
					&& WeatherHandler.isLocationLoaded(loc)) {
				cracks.put(new EarthQuakeCrack(loc), world);
			}
		}
	}
	
	@Override
	protected void endWeather(World world) 
	{
		for(EarthQuakeCrack crack : cracks.keySet()) {
			if(cracks.get(crack).equals(world)) {
				crack.setCancelled();
			}
		}
		
		if(rainafter) {
			WeatherHandler.setRain(world, duration);
		} else {
			WeatherHandler.setSunny(world, duration);
		}
	}
	
	private void blockDrop(Location location, Random r)
	{
		Location loc = location.clone();
		Block b;
		for(int x = -1; x <= 1; x++) {
			for(int z = -1; z <= 1 ; z++) {
				if(x == 0 && z == 0) {continue;}
				loc.setX(location.getX() + x);
				loc.setZ(location.getZ() + z);
				b = blockFinder(loc);
				if(!WeatherHandler.locationIsProtected(b.getLocation())
						&& WeatherHandler.isLocationLoaded(b.getLocation())
						&& r.nextDouble() <= randomdropchance
						&& shouldNotIgnore(loc)) {
					loc = b.getLocation();
					loc.getWorld().spawnFallingBlock(loc, b.getBlockData());
					loc.getBlock().setType(Material.AIR);
					loc.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, (float) 0.2, (float) 0.5);
				}
			}
		}
	}
	
	private boolean shouldNotIgnore(Location loc)
	{
		Location location = loc.clone().subtract(0,1,0);
		if(location.getBlock().getType().equals(Material.AIR)) {
			return true;
		}
		return false;
	}
	
	private Block blockFinder(Location loc)
	{
		Location location = loc.clone();
		Block b = location.getBlock();
		if(!b.getType().equals(Material.AIR)) {return b;}
		if(location.getBlockY() < WeatherHandler.getHighestY(location)-1) {
			while(b.getType().equals(Material.AIR)) {
				location.add(0,1,0);
				b = location.getBlock();
			}
		} else {
			while(b.getType().equals(Material.AIR)) {
				location.subtract(0,1,0);
				b = location.getBlock();
			}
		}
		return b;
	}
}
