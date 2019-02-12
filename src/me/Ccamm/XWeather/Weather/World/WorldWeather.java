package me.Ccamm.XWeather.Weather.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.Ccamm.XWeather.Commands;
import me.Ccamm.XWeather.LanguageLoader;
import me.Ccamm.XWeather.Main;
import me.Ccamm.XWeather.Weather.WeatherHandler;
import me.Ccamm.XWeather.Weather.Wind;
import me.Ccamm.XWeather.Weather.World.Types.CatsAndDogs;
import me.Ccamm.XWeather.Weather.World.Types.FlashFlood;
import me.Ccamm.XWeather.Weather.World.Types.HailStorm;
import me.Ccamm.XWeather.Weather.World.Types.SandStorm;
import me.Ccamm.XWeather.Weather.World.Types.ShootingStars;
import me.Ccamm.XWeather.Weather.World.Types.SnowStorm;
import me.Ccamm.XWeather.Weather.World.Types.SunShower;
import me.Ccamm.XWeather.Weather.World.Types.ThunderStorm;
import me.Ccamm.XWeather.Weather.World.Types.Windy;

public class WorldWeather implements WorldWeatherType
{
	private static HashSet<WorldWeatherType> weathertypes = new HashSet<WorldWeatherType>();
	
	protected double spawnchance;
	protected int duration;
	protected String name;
	protected String configprefix;
	protected boolean enabled;
	protected boolean rainafter;
	private int tickrate;
	protected double elytramod;
	protected double projectilemod;
	protected boolean windenabled;
	protected boolean moveplayer;
	protected boolean movemobs;
	protected double playerspeed;
	protected HashSet<World> enabledworlds = new HashSet<World>();
	protected HashMap<World, Integer> runningworlds = new HashMap<World, Integer>();
	
	public WorldWeather(String configprefix, FileConfiguration config, int tickrate)
	{
		this.configprefix = configprefix;
		this.tickrate = tickrate;
		loadConfig(config);
	}
	
	public void loadConfig(FileConfiguration config)
	{
		this.enabled = config.getBoolean(configprefix + ".Enabled");
		this.duration = config.getInt(configprefix + ".Duration")*20;
		this.spawnchance = config.getDouble(configprefix + ".SpawnChance");
		this.rainafter = config.getBoolean(configprefix + ".RainAfter");
		setEnabledWorlds(config.getStringList(configprefix + ".DisabledWorlds"));
		loadWindOptions(config);
		reloadName();
		loadMoreOptions(config);
	}
	
	private void setEnabledWorlds(List<String> disablestrings)
	{
		HashSet<World> universalworlds = WeatherHandler.getWorlds();
		HashSet<World> remove = new HashSet<World>();
		for(String s : disablestrings)
		{
			if(Bukkit.getWorld(s) == null) {
				LanguageLoader.sendMessage("ChatMessages.notworld", s, null, true);
				continue;
			} else {
				remove.add(Bukkit.getWorld(s));
			}
		}
		
		for(World uw : universalworlds) {
			if(uw.getEnvironment().equals(Environment.NORMAL) && !remove.contains(uw)) {
				enabledworlds.add(uw);
			}
		}
	}
	
	public static void setUp(FileConfiguration config)
	{
		SandStorm.setUpSandstorms(config);
		HailStorm.setUpHailStorms(config);
		SunShower.setUpSunShower(config);
		Windy.setUpWindystorms(config);
		ThunderStorm.setUpThunderstorms(config);
		SnowStorm.setUpSnowStorms(config);
		ShootingStars.setUpShootingStars(config);
		FlashFlood.setUpFlashFlood(config);
		CatsAndDogs.setUpCatsAndDogs(config);
	}
	
	protected void reloadName()
	{
		name = Main.getLanguageLoader().getLanguage().getString("WeatherTypes." + configprefix);
	}
	
	protected static void addWeatherType(WorldWeatherType weathertype)
	{
		weathertypes.add(weathertype);
	}
	
	public static void naturalStart(World world)
	{
		List<WorldWeatherType> occuringweather = new ArrayList<WorldWeatherType>();
		Random r = new Random();
		
		for(WorldWeatherType weathertype : weathertypes) {
			if(!weathertype.isEnabled()) {continue;}
			if(r.nextDouble() < weathertype.getSpawnChance() 
					&& weathertype.getEnabledWorlds().contains(world)) {
				occuringweather.add(weathertype);
			}
		}
		
		if(occuringweather.isEmpty()) {return;}
		occuringweather.get(r.nextInt(occuringweather.size())).start(world);
	}
	
	public void start(World world)
	{
		start(duration, world);
	}
	
	public void start(int dur, World world)
	{
		boolean startrunnable = false;
		if(runningworlds.isEmpty()) {startrunnable = true;}
		if(world == null) {
			for(World w : enabledworlds) {
				runningworlds.put(w, dur);
				startWeather(w, dur);
			}
		} else {
			runningworlds.put(world, dur);
			startWeather(world, dur);
		}
		String[] vars = {"<world>", 
				(world == null)?Main.getLanguageLoader().getLanguage().getString("ChatMessages.allworlds"):world.getName()};
		LanguageLoader.sendMessage("ChatMessages.startingweather", name, null, true, vars);
		
		if(!startrunnable) {return;}
		new BukkitRunnable() {
			@Override
			public void run() {
				if(runningworlds.isEmpty()) {
					cancel();
				}
				List<World> removeworld = new ArrayList<World>();
				for(World w : runningworlds.keySet()) {
					if(runningworlds.get(w) < 0) {
						removeworld.add(w);
						continue;
					}
					weatherEffect(w);
					updateWorldTime(w);
				}
				
				for(World w : removeworld) {
					stopWeather(w);
					runningworlds.remove(w);
				}

			}
			
		}.runTaskTimer(Main.getPlugin(), 0, tickrate);
	}
	
	public void updateWorldTime(World world)
	{
		int count = runningworlds.get(world);
		runningworlds.put(world, count - tickrate);
	}
	
	protected void stopWeather(World world)
	{
		endWeather(world);
	}
	
	public void stop(World world)
	{
		if(world == null) {
			for(World w : runningworlds.keySet()) {
				runningworlds.put(w, -1);
			}
		} else if(runningworlds.containsKey(world)) {
			runningworlds.put(world, -1);
		}
	}
	
	public boolean onCommandSet(CommandSender sender, String[] args)
	{
		//Need to change this so to get worlds as an option too
		if(args[1].equalsIgnoreCase(name)) {
			if(args.length == 2) {
				start(null);
				if(sender instanceof Player) {
					String[] vars = {"<world>", 
							Main.getLanguageLoader().getLanguage().getString("ChatMessages.allworlds")};
					LanguageLoader.sendMessage("ChatMessages.startingweather", name, (Player) sender, false, vars);
				}
			} else if(args.length == 3){
				try {
					start(Integer.parseInt(args[2])*20, null);
					if(sender instanceof Player) {
						String[] vars = {"<world>", 
								Main.getLanguageLoader().getLanguage().getString("ChatMessages.allworlds")};
						LanguageLoader.sendMessage("ChatMessages.startingweather", name, (Player) sender, false, vars);
					}
				} catch (Exception e) {
					if(Bukkit.getWorld(args[2]) != null) {
						start(Bukkit.getWorld(args[2]));
						if(sender instanceof Player) {
							String[] vars = {"<world>", 
									args[2]};
							LanguageLoader.sendMessage("ChatMessages.startingweather", name, (Player) sender, false, vars);
						}
					} else {
						LanguageLoader.sendMessage("ChatMessages.needint", "", null, true);	
						if(sender instanceof Player) {
							LanguageLoader.sendMessage("ChatMessages.needint", "", (Player) sender, false);	
						}
					}
				}
			} else if(args.length == 4) {
				World w = Bukkit.getWorld(args[3]);
				if(w == null) {
					LanguageLoader.sendMessage("ChatMessages.notworld", args[3], null, true);
					if(sender instanceof Player) {
						LanguageLoader.sendMessage("ChatMessages.notworld", args[3], (Player) sender, false);
					}
					return true;
				}
				
				try {
					start(Integer.parseInt(args[2])*20, w);
					if(sender instanceof Player) {
						String[] vars = {"<world>", 
								args[3]};
						LanguageLoader.sendMessage("ChatMessages.startingweather", name, (Player) sender, false, vars);
					}
				} catch (Exception e) {
					LanguageLoader.sendMessage("ChatMessages.needint", "", null, true);	
					if(sender instanceof Player) {
						LanguageLoader.sendMessage("ChatMessages.needint", "", (Player) sender, false);	
					}
				}
			}
			return true;
		} 
		return false;
	}
	
	public boolean onCommandStop(CommandSender sender, String[] args) 
	{
		if (args.length == 2) {
			if(args[1].equalsIgnoreCase(name)) {
				String[] vars = {"<world>", 
						Main.getLanguageLoader().getLanguage().getString("ChatMessages.allworlds")};
				LanguageLoader.sendMessage("ChatMessages.stopweather", name, 
						(sender instanceof Player) ? (Player) sender: (Player) null, true, vars);
				stop(null);
				return true;
			}
		} else if (args.length == 3) {
			if(!args[1].equalsIgnoreCase(name)) {
				return false;
			}
			
			World w = Bukkit.getWorld(args[2]);
			if(w == null) {
				LanguageLoader.sendMessage("ChatMessages.notworld", args[3], null, true);
				if(sender instanceof Player) {
					LanguageLoader.sendMessage("ChatMessages.notworld", args[3], (Player) sender, false);
				}
				return true;
			}
			String[] vars = {"<world>", 
					args[2]};
			LanguageLoader.sendMessage("ChatMessages.stopweather", name, 
					(sender instanceof Player) ? (Player) sender: (Player) null, true, vars);
			stop(w);
			return true;
		}
		return false;
	}
	
	public static boolean checkCommands(CommandSender sender, String[] args)
	{
		for(WorldWeatherType weather : weathertypes) {
			if(args[0].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("ChatCommands.set"))) {
				if(args.length == 1) {
					Commands.getSetUsage(sender);
					return true;
				}
				
				if(weather.onCommandSet(sender, args)) {
					return true;
				}
			} else if(args[0].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("ChatCommands.stop"))) {
				if(weather.onCommandStop(sender, args)) {
					return true;
				}
			}	
		}
		return false;
	}
	
	protected void moveEntities(Player p, int radius)
	{
		if(!windenabled) {return;}
		
		double[] movevector = Wind.getPlayerVelocity(p.getWorld(), (float) (0.0001*playerspeed));
		if(moveplayer
				&& !p.isSneaking() && !p.getGameMode().equals(GameMode.CREATIVE) 
				&& !p.getGameMode().equals(GameMode.SPECTATOR) 
				&& !p.isFlying()) {
			if(WeatherHandler.shouldMove(p, p.getLocation(), movevector)) {
				ItemStack chest = p.getInventory().getChestplate();
				boolean wearingelytra = false;
				if(chest != null) {
					wearingelytra = chest.getType().equals(Material.ELYTRA);
				}
				p.setVelocity(p.getVelocity().add(
						new Vector((wearingelytra ? elytramod:1)*movevector[0],
								(wearingelytra ? elytramod:1)*movevector[1],
								(wearingelytra ? elytramod:1)*movevector[2])));
			}
		}
		
		for(Entity e : p.getNearbyEntities(radius, radius, radius)) {
			if(e instanceof Player 
					|| (!WeatherHandler.moveNPC() && e.hasMetadata("NPC"))
					|| (!WeatherHandler.moveMinecart() && e instanceof Minecart)
					|| (!WeatherHandler.moveArmourStands() && e instanceof ArmorStand)) {
				continue;
			}
			
			if(e instanceof Mob && !movemobs) {continue;}
			//if(e instanceof Horse && e.getPassenger() != null) {continue;}
			if(e instanceof Horse && !e.getPassengers().isEmpty()) {continue;}
			
			if(WeatherHandler.shouldMove(e, e.getLocation(), movevector)) {
				e.setVelocity(e.getVelocity().add(
						new Vector(((e instanceof Projectile)? projectilemod:1)*movevector[0],
								((e instanceof Projectile)? projectilemod:1)*movevector[1],
								((e instanceof Projectile)? projectilemod:1)*movevector[2])));
			}
		}
	}
	
	public static void stopAll()
	{
		for(WorldWeatherType weather : weathertypes) {
			weather.stop(null);
		}
	}
	
	public static HashSet<WorldWeatherType> getWeathers() {return weathertypes;}
	
	public double getSpawnChance() {return spawnchance;}
	public int getDefaultDuration() {return duration;}
	public boolean isEnabled() {return enabled;}
	public String getName() {return name;}
	public String getConfigPrefix() {return configprefix;}
	public HashSet<World> getEnabledWorlds() {return enabledworlds;}
	
	protected static WorldWeatherType getWeatherType(String configname)
	{
		for(WorldWeatherType wwt : weathertypes) {
			if(wwt.getConfigPrefix().equals(configname)) {
				return wwt;
			}
		}
		return null;
	}
	
	protected void loadWindOptions(FileConfiguration config)
	{
		if(!config.isSet(configprefix + ".Wind.Enabled")) {
			windenabled = false;
			return;
		}
		playerspeed = config.getDouble(configprefix + ".Wind.MoveSpeed");
		projectilemod = config.getDouble(configprefix + ".Wind.ProjectileModifier");
		elytramod = config.getDouble(configprefix + ".Wind.ProjectileModifier");
		windenabled = config.getBoolean(configprefix + ".Wind.Enabled");
		moveplayer = config.getBoolean(configprefix + ".Wind.MovePlayer");
		movemobs = config.getBoolean(configprefix + ".Wind.MoveMobs");
	}
	
	protected void endWeather(World world) 
	{
		if(rainafter) {
			WeatherHandler.setRain(world, duration);
		} else {
			WeatherHandler.setSunny(world, duration);
		}
	}
	
	//Methods that can be overriden by other weathertypes
	protected void startWeather(World world, int dur) {}
	protected void weatherEffect(World world) {}
	protected void loadMoreOptions(FileConfiguration config) {}
}
