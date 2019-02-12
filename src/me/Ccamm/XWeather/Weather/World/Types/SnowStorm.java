package me.Ccamm.XWeather.Weather.World.Types;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Snow;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Ccamm.XWeather.Weather.WeatherHandler;
import me.Ccamm.XWeather.Weather.Wind;
import me.Ccamm.XWeather.Weather.World.WorldWeather;

public class SnowStorm extends WorldWeather 
{
	private static SnowStorm snowstorm = null;
	private static int particlecount;
	private static int playerradius;
	private static double speed;
	private static Material[] nosnow = {Material.WATER, Material.LAVA, Material.ICE};
	private static BlockData snow1 = Bukkit.getServer().createBlockData(Material.SNOW, "[layers=1]");
	private static boolean cansnow;
	private static int tickspersnow;
	
	private SnowStorm(FileConfiguration config)
	{
		super("SnowStorm", config, 1);
		loadConfig(config);
		WorldWeather.addWeatherType(this);
	}
	
	public static SnowStorm setUpSnowStorms(FileConfiguration config)
	{
		if(snowstorm == null) {
			snowstorm = new SnowStorm(config);
		} else {
			snowstorm.loadConfig(config);
			snowstorm.reloadName();
		}
		return snowstorm;
	}
	
	@Override
	public void loadMoreOptions(FileConfiguration config)
	{
		particlecount = config.getInt("SnowStorm.ParticleCount");
		speed = config.getDouble("SnowStorm.ParticleSpeed");
		playerradius = config.getInt("SnowStorm.RadiusAroundPlayer");
		cansnow = config.getBoolean("SnowStorm.SnowBuildUp.Snow");
		tickspersnow = config.getInt("SnowStorm.SnowBuildUp.Ticks");
	}
	
	@Override
	protected void weatherEffect(World w)
	{
		for(Player p : w.getPlayers()) {
			if(!WeatherHandler.isSnowBiome(p.getLocation())) {
				continue;
			}
			if(WeatherHandler.locationIsProtected(p.getLocation())) {continue;}
			
			spawnParticles(p);
			moveEntities(p, playerradius);
			if(cansnow && runningworlds.get(w) % tickspersnow == 0) {
				addLayer(p);
			}
		}
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setRain(w, dur);
	}
	
	private void spawnParticles(Player p)
	{
		Random r = new Random();
		double dx, dy, dz;
		Location loc = p.getLocation().clone();
		double v[] = Wind.getParticleVelocity(p.getWorld(), -5.0);
		
		for(int i = 0; i < particlecount; i++) {
			dx = playerradius*r.nextDouble()*(r.nextBoolean()?1:-1);
			dy = playerradius*r.nextDouble()*(r.nextBoolean()?1:-0.5);
			dz = playerradius*r.nextDouble()*(r.nextBoolean()?1:-1);
			
			if(loc.getY() + dy < 0) {dy = 0;}
			if(loc.getY() + dy > p.getWorld().getMaxHeight()) {dy = p.getWorld().getMaxHeight() - loc.getY();}
			
			loc.add(dx, dy, dz);
			//loc.setY(256);
			if(WeatherHandler.isSnowBiome(loc) && loc.getBlockY() >= WeatherHandler.getHighestY(loc) && WeatherHandler.isLocationLoaded(loc)) {
				p.spawnParticle(Particle.CLOUD, loc, 0, v[0],v[1],v[2], speed);
			}
			loc.subtract(dx, dy, dz);
		}
	}
	
	private void addLayer(Player p)
	{
		if(!WeatherHandler.isSnowBiome(p.getLocation())) {return;}
		Random r = new Random();
		Location loc = p.getLocation().clone();
		loc.add(playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1), 
				0, 
				playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1));
		loc.setY(WeatherHandler.getHighestY(loc));
		if(!WeatherHandler.isSnowBiome(loc) || !WeatherHandler.isLocationLoaded(loc)) {
			addLayer(p);
		} else {
			snowLayer(p, loc);
		}
	}
	
	private void snowLayer(Player p, Location loc)
	{
		Block b = loc.clone().subtract(0,1,0).getBlock();
		for(Material ns : nosnow) {
			if(b.getType().equals(ns)) {
				addLayer(p);
				return;
			}
		}
		
		if(loc.getBlock().getType().equals(Material.SNOW)) {
			updateSnowLvl(loc.getBlock());
		}
	}
	
	private void setSnowAbove(Location loc)
	{
		if(loc.getWorld().getBlockAt(loc).getType() != Material.AIR) {
			return;
		}
		loc.getBlock().setBlockData(snow1);
	}
	
	private void updateSnowLvl(Block b)
	{
		Snow s = (Snow) b.getBlockData();
		if(s.getLayers() == s.getMaximumLayers()) {
			setSnowAbove(b.getLocation());
		} else {
			s.setLayers(s.getLayers() + 1);
			b.setBlockData(s);
		}
	}
	
	/*For Legacy
	@SuppressWarnings("deprecation")
	private void setSnowAbove(Location loc)
	{
		if(loc.getWorld().getBlockAt(loc).getType() != Material.AIR) {
			return;
		}
		Block b = loc.getBlock();
		BlockState bs = b.getState();
		bs.setType(Material.SNOW);
		bs.getData().setData((byte) 1);
		bs.update();
	}
	
	@SuppressWarnings("deprecation")
	private void updateSnowLvl(Block b)
	{
		BlockState bs = b.getState();
		if(b.getData() == 8) {
			setSnowAbove(b.getLocation());
		} else {
			bs.getData().setData((byte) (b.getData() + 1));
			bs.update();
		}
	}
	 */
}
