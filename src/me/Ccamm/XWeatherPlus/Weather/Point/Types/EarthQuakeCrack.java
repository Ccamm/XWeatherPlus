package me.Ccamm.XWeatherPlus.Weather.Point.Types;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.Ccamm.XWeatherPlus.Main;
import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;

public class EarthQuakeCrack 
{
	private static int maxwidth;
	private static int minwidth;
	private static int maxradius;
	private static int minradius;
	private static int lavaheight;
	private static int destroyheight;
	private static int startheight;
	private static int removebelow;
	private static int removeabove;
	
	private Location start1;
	private int height;
	private double[] perpnorm;
	private double[] parnorm;
	private int width;
	private double length;
	private Integer maxheight = null;
	private double currentperp = 0;
	private double currentpar = 0;
	private Location loc;
	private double[] origin;
	private boolean cancelled = false;
	
	public EarthQuakeCrack(Location start)
	{
		Random r = new Random();
		this.width = r.nextInt(maxwidth + 1 - minwidth) + minwidth;
		this.start1 = start.clone();
		this.height = startheight;
		start1.setY(0);
		Location finish1 = start.clone().add(
				((maxradius-minradius)*r.nextDouble()+minradius)*(r.nextBoolean() ? 1:-1),
				0,
				((maxradius-minradius)*r.nextDouble()+minradius)*(r.nextBoolean() ? 1:-1));
		finish1.setY(0);
		settingOtherConditions(finish1);
		loc = start1.clone();
		origin = getOrigin();
		startDestruction();
	}
	
	private double[] getOrigin()
	{
		double[] result = {start1.getX(), start1.getY(), start1.getZ()};
		return result;
	}
	
	public static void setupEarthQuakes(FileConfiguration config) 
	{
		maxwidth = config.getInt("EarthQuake.Crack.MaxWidth");
		minwidth = config.getInt("EarthQuake.Crack.MinWidth");
		maxradius = config.getInt("EarthQuake.Crack.MaxRadius");
		minradius = config.getInt("EarthQuake.Crack.MinRadius");
		lavaheight = config.getInt("EarthQuake.Crack.LavaHeight");
		destroyheight = config.getInt("EarthQuake.Crack.DestroyHeight");
		startheight = config.getInt("EarthQuake.Crack.StartHeight");
		removebelow = config.getInt("EarthQuake.Crack.BelowBlocksRemoved");
		removeabove = config.getInt("EarthQuake.Crack.AboveBlocksRemoved");
	}
	
	private void settingOtherConditions(Location finish1)
	{
		Vector v = start1.toVector().subtract(finish1.toVector());
		double[] change = WeatherHandler.vector3D(
				(double) -v.getZ(), 
				(double) 0, 
				(double) v.getX(), 
				(float) width);
		this.length = v.length();
		this.perpnorm = WeatherHandler.normalisedvector(change);
		this.parnorm = WeatherHandler.normalisedvector(v.getX(),0.0,v.getZ());
	}
	
	private void startDestruction()
	{
		new BukkitRunnable() {
			@Override
			public void run() {
				if(cancelled) {
					cancel();
					return;
				}
				if(height <= lavaheight) {
					underneathChanges(Material.LAVA);
				} else if(height <= destroyheight) {
					underneathChanges(Material.AIR);
				} else {
					aboveChanges();
				}
			}
		}.runTaskTimer(Main.getPlugin(), 0, 1);
	}
	
	private void underneathChanges(Material material)
	{
		for(int i = 0; i < removebelow; i++) {
			/*loc = start1.clone().add(
					currentperp*perpnorm[0] + currentpar*parnorm[0], 
					0, 
					currentperp*perpnorm[2] + currentpar*parnorm[2]);*/
			loc.setX(origin[0] + currentperp*perpnorm[0] + currentpar*parnorm[0]);
			loc.setZ(origin[2] + currentperp*perpnorm[2] + currentpar*parnorm[2]);
			loc.setY(height);
			
			if(!WeatherHandler.locationIsProtected(loc) 
					&& WeatherHandler.isLocationLoaded(loc)
					&& !cancelled) {
				if(!loc.getBlock().getType().equals(Material.BEDROCK)) {
					loc.getBlock().setType(material);
					loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 0, 0, 0, 0);
				}
			}
			currentperp+=0.75;
			if(currentperp > width) {
				currentperp = 0;
				currentpar+=0.75;
				if(currentpar > length) {
					currentpar = 0;
					height++;
				}
			}
		}
	}
	
	private void aboveChanges()
	{
		for(int i = 0; i < removeabove; i++) {
			/*loc = start1.clone().add(
					currentperp*perpnorm[0] + currentpar*parnorm[0], 
					0, 
					currentperp*perpnorm[2] + currentpar*parnorm[2]);*/
			loc.setX(origin[0] + currentperp*perpnorm[0] + currentpar*parnorm[0]);
			loc.setZ(origin[2] + currentperp*perpnorm[2] + currentpar*parnorm[2]);
			loc.setY(height);
			if(maxheight == null) {
				maxheight = WeatherHandler.getHighestY(loc);
			}
			if(!WeatherHandler.locationIsProtected(loc) 
					&& WeatherHandler.isLocationLoaded(loc)
					&& !cancelled) {
				if(!loc.getBlock().getType().equals(Material.BEDROCK)
					&& !loc.getBlock().getType().equals(Material.WATER)
					&& !loc.getBlock().getType().equals(Material.LAVA)) {
					loc.getWorld().spawnFallingBlock(loc, loc.getBlock().getBlockData());
					loc.getBlock().setType(Material.AIR);
					loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 0, 0, 0, 0);
				}
			}
			height++;
			if(height >= maxheight) {
				height = destroyheight + 1;
				maxheight = null;
				currentperp+=0.75;
				if(currentperp > width) {
					currentperp = 0;
					currentpar+=0.75;
					if(currentpar > length) {
						cancelled = true;
						return;
					}
				}
			}
		}
	}
}
