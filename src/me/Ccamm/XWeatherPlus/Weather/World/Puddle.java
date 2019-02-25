package me.Ccamm.XWeatherPlus.Weather.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.scheduler.BukkitRunnable;

import me.Ccamm.XWeatherPlus.Main;
import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;

public class Puddle 
{
	private static HashSet<WorldWeatherType> currentpdlweather = new HashSet<WorldWeatherType>();
	private static ArrayList<World> wetworlds = new ArrayList<World>();
	private static ArrayList<Puddle> puddles = new ArrayList<Puddle>();
	private static double sizeincrease = 0.75;
	
	private LinkedList<Location> puddlelocations = new LinkedList<Location>();
	private Location location;
	
	public Puddle(Location location, WorldWeatherType wwt)
	{
		this.location = location;
		
		createPuddle();
		if(!currentpdlweather.contains(wwt)) {
			currentpdlweather.add(wwt);
		}
	}
	
	private boolean setLocToPuddle(Location loc)
	{
		if(!WeatherHandler.checkValidWaterLocation(loc)) {return false;}
		Block b = loc.getBlock();
		if(!b.getType().equals(Material.AIR)) {return false;}
		b.setType(Material.WATER);
		Levelled waterlvl = (Levelled) b.getBlockData();
		waterlvl.setLevel(7);
		loc.getBlock().setBlockData(waterlvl);
		return true;
	}
	
	private void createPuddle()
	{
		if(!setLocToPuddle(location)) {return;}
		puddles.add(this);
		Random r = new Random();
		Location loc = location.clone();
		for(int x = -1; x <= 1; x++) {
			for(int z = -1; z <= 1; z++) {
				if(x == 0 && z == 0) {continue;}
				if(r.nextDouble() <= sizeincrease) {
					loc.setX(location.getX() + x);
					loc.setZ(location.getZ() + z);
					puddlelocations.add(loc.clone());
				}
			}
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(puddlelocations.isEmpty()) {
					cancel();
				} else {
					Location loc = puddlelocations.removeFirst();
					setLocToPuddle(loc);
				}
			}
		}.runTaskTimer(Main.getPlugin(), 20, 20);
	}
	
	public static boolean shouldPuddleStay(Location loc)
	{
		if(WeatherHandler.isDryBiome(loc) || WeatherHandler.isSnowBiome(loc)) {
			return false;
		}
		World world = loc.getWorld();
		updateWetWorlds();
		if(wetworlds.contains(world)) {
			return true;
		}
		return false;
	}
	
	public static void dryUpPuddles(World world)
	{
		ArrayList<Puddle> remove = new ArrayList<Puddle>();
		for(Puddle puddle : puddles) {
			if(puddle.getWorld().equals(world)) {
				if(puddle.getLocation().getBlock().getType().equals(Material.WATER)) {
					puddle.getLocation().getBlock().setType(Material.AIR);
				}
				remove.add(puddle);
			}
		}
		
		puddles.removeAll(remove);
	}
	
	private static void updateWetWorlds()
	{
		wetworlds.clear();
		for(WorldWeatherType wwt: currentpdlweather) {
			wetworlds.addAll(wwt.getRunningWorlds().keySet());
		}
	}
	
	public Location getLocation() {return location;}
	public World getWorld() {return location.getWorld();}
}
