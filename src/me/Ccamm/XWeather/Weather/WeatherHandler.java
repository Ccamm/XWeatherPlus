package me.Ccamm.XWeather.Weather;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.Ccamm.XWeather.LanguageLoader;
import me.Ccamm.XWeather.Main;
import me.Ccamm.XWeather.WorldGuardManager;
import me.Ccamm.XWeather.Weather.Point.PointWeather;
import me.Ccamm.XWeather.Weather.World.WorldWeather;

public class WeatherHandler 
{
	private static WeatherHandler weatherhandler = null;

	private static HashSet<World> worlds = new HashSet<World>();
	private static int weatherinterval;
	private static double walldistance;
	private static boolean universal;
	private static boolean noempty;
	private static boolean movenpc;
	private static boolean moveminecart;
	private static boolean movearmourstand;
	private static boolean loadchunks;
	
	private WeatherHandler(FileConfiguration config)
	{
		loadConfig(config);
		new BukkitRunnable() {
			@Override
			public void run() {
				for(World world : worlds) {
					if(!universal && noempty && world.getPlayers().isEmpty()) {continue;}
					if(universal && noempty && Bukkit.getOnlinePlayers().isEmpty()) {break;}
					WorldWeather.naturalStart(universal ? null : world);
					PointWeather.naturalStart(universal ? null : world);
					if(universal) {break;}
				}
			}
		}.runTaskTimer(Main.getPlugin(), WeatherHandler.getWeatherInterval(), WeatherHandler.getWeatherInterval());
	}
	
	public static void loadConfig(FileConfiguration config) 
	{
		setWorlds(config);
		weatherinterval = config.getInt("WeatherControl.WeatherInterval")*20;
		universal = config.getBoolean("WeatherControl.UniversalWeather");
		noempty = config.getBoolean("WeatherControl.NoWeatherInEmpty");
		movenpc = config.getBoolean("WeatherControl.Wind.MoveNPC");
		walldistance = config.getDouble("WeatherControl.Wind.WallDistance");
		moveminecart = config.getBoolean("WeatherControl.Wind.MoveMinecart");
		movearmourstand = config.getBoolean("WeatherControl.Wind.MoveArmorStands");
		loadchunks = config.getBoolean("WeatherControl.LoadChunks");
		Wind.loadWind(config);
		WorldWeather.setUp(config);
		PointWeather.setUp(config);
	}
	
	public static WeatherHandler setUpHandler(FileConfiguration config)
	{
		if(weatherhandler == null) {
			weatherhandler = new WeatherHandler(config);
		}
		return weatherhandler;
	}
	
	private static void setWorlds(FileConfiguration config)
	{
		boolean blacklist = config.getBoolean("WeatherControl.Blacklist");
		if(blacklist) {
			blacklistWorlds(config);
		} else {
			whitelistWorlds(config);
		}
	}
	
	private static void whitelistWorlds(FileConfiguration config)
	{
		List<String> stringwhitelist = config.getStringList("WeatherControl.Worlds");
		for(String s : stringwhitelist) {
			if(Bukkit.getWorld(s) == null) {
				LanguageLoader.sendMessage("ChatMessages.notworld", s, null, true);
				continue;
			} else {
				worlds.add(Bukkit.getWorld(s));
			}
		}
	}
	
	private static void blacklistWorlds(FileConfiguration config)
	{
		HashSet<World> blacklist = new HashSet<World>();
		List<String> stringblacklist = config.getStringList("WeatherControl.Worlds");
		for(String s : stringblacklist) {
			if(Bukkit.getWorld(s) == null) {
				LanguageLoader.sendMessage("ChatMessages.notworld", s, null, true);
				continue;
			}
			blacklist.add(Bukkit.getWorld(s));
		}
		
		for(World w : Bukkit.getWorlds()) {
			if(w.getEnvironment().equals(Environment.NORMAL) && !blacklist.contains(w)) {
				LanguageLoader.sendMessage("ChatMessages.worldload", w.getName(), null, true);
				worlds.add(w);
			}
		}
	}
	
	public static void stopAll()
	{
		WorldWeather.stopAll();
		PointWeather.stopAll();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				for(World w : worlds) {
					setSunny(w, weatherinterval);
				}
			}
		}.runTaskLater(Main.getPlugin(), 20);
	}
	
	public static HashSet<World> getWorlds() {return worlds;}
	
	public static void setSunny(World w, Integer duration)
	{
		w.setStorm(false);
		w.setWeatherDuration(duration);
	}
	
	public static void setRain(World w, Integer duration)
	{
		w.setStorm(true);
		w.setWeatherDuration(duration);
	}
	
	public static void setThundering(World w, Integer duration)
	{
		w.setThundering(true);
		w.setThunderDuration(duration);
	}
	
	public static double[] normalisedvector(Double a, Double b, Double c)
	{
		double n = Math.sqrt(a*a + b*b + c*c);
		double[] v = {a/n, b/n, c/n};
		return v;
	}
	
	public static double[] normalisedvector(double[] v)
	{
		double n = Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
		double[] vel = {v[0]/n, v[1]/n, v[2]/n};
		return vel;
	}
	
	public static double[] vector3D(Double a, Double b, Double c, Float amplitude)
	{
		double[] v = normalisedvector(a,b,c);
		for(int i = 0; i < 3; i++) {
			v[i] = v[i]*amplitude;
		}
		return v;
	}
	
	public static Boolean isDesert(Location loc)
	{		
		Biome[] desert = {Biome.DESERT, Biome.DESERT_HILLS, Biome.DESERT_LAKES};
		Biome bloc = loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
		for(Biome b : desert) {
			if(b.equals(bloc)) {
				return true;
			}
		}
		return false;
	}
	
	public static Boolean isDryBiome(Location loc)
	{
		if(isDesert(loc)) {
			return true;
		}
		
		//{Biome.SAVANNA, Biome.MESA, Biome.MESA_CLEAR_ROCK, Biome.MESA_ROCK, Biome.SAVANNA_ROCK};
		Biome[] otherdry = {Biome.SAVANNA, Biome.SAVANNA_PLATEAU, Biome.SHATTERED_SAVANNA, 
				Biome.SHATTERED_SAVANNA_PLATEAU, Biome.BADLANDS, Biome.BADLANDS_PLATEAU};
		Biome bloc = loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
		for(Biome b : otherdry) {
			if(b.equals(bloc)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean shouldMove(Entity e, Location l, double[] winddir)
	{
		if(l.getBlockY() < WeatherHandler.getHighestY(l)) {return false;}
		return moveMethod(e, l, winddir, (float) walldistance);
	}
	
	private static boolean moveMethod(Entity e, Location loc, double[] winddir, float distance)
	{
		if(distance <= 0) {
			return true;
		}
		double[] dir = WeatherHandler.vector3D(winddir[0], 
				winddir[1], 
				winddir[2], (float) distance);
		Location l = loc.clone();
		if(!l.add(-dir[0], e.getHeight(), -dir[2]).getBlock().getType().equals(Material.AIR)) {return false;}
		
		distance -= 0.5;
		return moveMethod(e,loc,winddir,distance);
	}
	
	public static boolean isSnowBiome(Location loc)
	{
		/*For Legacy
		Biome[] snowbiomes = {Biome.ICE_MOUNTAINS, Biome.ICE_FLATS, Biome.COLD_BEACH,
				Biome.TAIGA_COLD, Biome.TAIGA_COLD_HILLS, Biome.FROZEN_OCEAN, Biome.FROZEN_RIVER};
		 */
		Biome[] snowbiomes = {Biome.SNOWY_BEACH, Biome.SNOWY_MOUNTAINS, Biome.SNOWY_TAIGA, Biome.SNOWY_TAIGA_HILLS, Biome.SNOWY_TAIGA_MOUNTAINS,
				Biome.SNOWY_TUNDRA, Biome.FROZEN_OCEAN, Biome.FROZEN_RIVER, Biome.DEEP_FROZEN_OCEAN, Biome.ICE_SPIKES, Biome.MOUNTAINS, Biome.MOUNTAIN_EDGE};
		Biome bloc = loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
		
		for(Biome b : snowbiomes) {
			if(bloc.equals(b)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isLocationLoaded(Location loc)
	{
		if(loadchunks) {
			return true;
		}
		return loc.getWorld().isChunkLoaded(loc.getBlockX()/16, loc.getBlockZ()/16);
	}
	
	public static boolean locationIsProtected(Location loc)
	{
		try {
			return WorldGuardManager.locationIsProtected(loc);
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}
	
	public static Player getPlayerInValidWorld()
	{
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(validWorld(p.getWorld())) {
				return p;
			}
		}
		return null;
	}
	
	public static boolean validWorld(World w)
	{
		for(World ww : worlds) {
			if(ww.getName().equals(w.getName())) {return true;}
		}
		return false;
	}
	
	public static int getHighestY(Location loc)
	{
		Location l = loc.clone();
		int check = loc.getWorld().getMaxHeight()-1;
		while(check >= 0) {
			l.setY(check);
			if(l.getBlock().getType() != Material.AIR) {
				return check + 1;
			}
			check--;
		}
		int result = loc.getWorld().getHighestBlockYAt(loc);
		return result;
	}
	
	public static boolean moveNPC() {return movenpc;}
	public static boolean moveMinecart() {return moveminecart;}
	public static boolean moveArmourStands() {return movearmourstand;}
	
	public static int getWeatherInterval()
	{
		return weatherinterval;
	}
}
