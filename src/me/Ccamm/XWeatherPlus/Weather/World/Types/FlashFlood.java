package me.Ccamm.XWeatherPlus.Weather.World.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.World.WorldWeather;

public class FlashFlood extends WorldWeather
{
	private static FlashFlood flashflood = null;
	private static int playerradius;
	private static int ticksperwater;
	private static HashMap<World, List<Location>> waterdrops = new HashMap<World, List<Location>>();
	
	private FlashFlood(FileConfiguration config)
	{
		super("FlashFlood", config, 1);
		WorldWeather.addWeatherType(this);
	}
	
	public static FlashFlood setUpFlashFlood(FileConfiguration config)
	{
		if(flashflood == null) {
			flashflood = new FlashFlood(config);
		} else {
			flashflood.loadConfig(config);
		}
		return flashflood;
	}
	
	@Override
	public void loadMoreOptions(FileConfiguration config)
	{
		playerradius = config.getInt("FlashFlood.RadiusAroundPlayer");
		ticksperwater = config.getInt("FlashFlood.TicksBetweenFlooding");
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setRain(w, dur);
		WeatherHandler.setThundering(w, dur);
	}
	
	@Override
	protected void weatherEffect(World w)
	{
		for(Player p : w.getPlayers()) {
			
			if(runningworlds.get(w) % ticksperwater == 0) {
				flood(p.getLocation());
			}
		}
	}
	
	private void flood(Location location)
	{
		/*For Legacy
		b.getType().equals(Material.WATER) || b.getType().equals(Material.STATIONARY_WATER)
		b.getType().equals(Material.LONG_GRASS) || b.getType().equals(Material.DOUBLE_PLANT)
		 */
		
		Location loc = location.clone();
		Random r = new Random();
		loc.add(playerradius*r.nextDouble()*(r.nextBoolean()?1:-1),
				0, 
				playerradius*r.nextDouble()*(r.nextBoolean()?1:-1));
		loc.setY(WeatherHandler.getHighestY(loc)-1);
		if(WeatherHandler.locationIsProtected(loc)) {return;}
		if(WeatherHandler.isDryBiome(loc)) {return;}
		if(WeatherHandler.isSnowBiome(loc)) {return;}
		if(!WeatherHandler.isLocationLoaded(loc)) {return;}
		Block b = loc.getBlock();
		if(b.getType().equals(Material.WATER)) {
			return;
		} else if(b.getType().equals(Material.GRASS) || b.getType().equals(Material.TALL_GRASS)) {
			floodLocation(loc);
		} else {
			loc.add(0,1,0);
			if(WeatherHandler.locationIsProtected(loc) || !WeatherHandler.checkValidWaterLocation(loc)) {return;}
			floodLocation(loc);
		}
	}
	
	private void floodLocation(Location loc)
	{
		addLocation(loc);
		Block b = loc.getBlock();
		b.setType(Material.WATER);
	}
	
	@Override
	protected void stopWeather(World world)
	{
		changeBack(world);
		endWeather(world);
	}
	
	private void changeBack(World w)
	{
		if(!waterdrops.containsKey(w)) {return;}
		List<Location> drops = waterdrops.get(w);
		Block b;
		for(Location loc : drops) {
			b = loc.getBlock();
			//b.getType().equals(Material.WATER) || b.getType().equals(Material.STATIONARY_WATER)
			if(b.getType().equals(Material.WATER)) {
				b.setType(Material.AIR);
			}
		}
	}
	
	private void addLocation(Location loc)
	{
		List<Location> drops = new ArrayList<Location>();
		if(!waterdrops.containsKey(loc.getWorld())) {
			drops.add(loc);
			waterdrops.put(loc.getWorld(), drops);
		} else {
			drops = waterdrops.get(loc.getWorld());
			drops.add(loc);
			waterdrops.put(loc.getWorld(), drops);
		}
	}
}
