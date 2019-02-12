package me.Ccamm.XWeatherPlus.Weather.Point.Types;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.Ccamm.XWeatherPlus.Commands;
import me.Ccamm.XWeatherPlus.LanguageLoader;
import me.Ccamm.XWeatherPlus.Main;
import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.Point.PointWeather;
import me.Ccamm.XWeatherPlus.Weather.Point.Types.Methods.TornadoDirection;

public class Tornado extends PointWeather
{
	private static int duration;
	private static int height;
	private static int r1;
	private static int r2;
	private static int suckr;
	private static double spawnchance;
	private static int loadspeed;
	private static int spawnradius;
	private static double changechance;
	private static boolean worldspawn = true;
	private static int playerview;
	private static boolean isenabled;
	private static double dp;
	private static double rdp;
	private static double ddy;
	private static double rddy;
	private static String name;
	private static HashSet<World> enabledworlds = new HashSet<World>();
	private static boolean suckenabled;
	private static boolean suckplayer;
	private static boolean suckmobs;
	private static boolean canfire;
	private static double firechance;
	private static double setblockfire;
	private static double entityfiredistance;
	private static HashSet<Material> breakablematerials = new HashSet<Material>();
	private static boolean destroyblocks;
	private static int destroyradius;
	private static int maxdestroy;
	
	private Player activator = null;
	private TornadoDirection td;
	private boolean onfire = false;
	
	public Tornado(Location loc, int dur)
	{
		super(loc, 2, dur);
		Location l = loc.clone();
		l.setY(WeatherHandler.getHighestY(l));
		this.location = l;
		this.td = new TornadoDirection(location, dur);
		weatherevents.add(this);

		start();
	}
	
	public Tornado(Location loc)
	{
		super(loc, 2, duration);
		Location l = loc.clone();
		l.setY(WeatherHandler.getHighestY(l));
		this.location = l;
		this.td = new TornadoDirection(location, duration);
		weatherevents.add(this);
		
		start();
	}
	
	public Tornado(Location loc, int dur, Player p)
	{
		super(loc, 2, dur);
		Location l = loc.clone();
		l.setY(WeatherHandler.getHighestY(l));
		this.location = l;
		this.activator = p;
		this.td = new TornadoDirection(location, dur);
		weatherevents.add(this);

		start();
	}
	
	public Tornado(Location loc, Player p)
	{
		super(loc, 2, duration);
		Location l = loc.clone();
		l.setY(WeatherHandler.getHighestY(l));
		this.location = l;
		this.activator = p;
		this.td = new TornadoDirection(location, duration);
		weatherevents.add(this);
		
		start();
	}
	
	public static void loadConfig(FileConfiguration config)
	{
		duration = config.getInt("Tornado.Duration")*20;
		height = config.getInt("Tornado.Height");
		r1 = config.getInt("Tornado.BottomRadius");
		r2 = config.getInt("Tornado.TopRadius");
		suckr = config.getInt("Tornado.SuckIn.Radius");
		spawnchance = config.getDouble("Tornado.SpawnChance");
		loadspeed = config.getInt("Tornado.LoadSpeed");
		spawnradius = config.getInt("Tornado.SpawnRadius");
		changechance = config.getDouble("Tornado.ChangeDirectionChance");
		worldspawn = config.getBoolean("Tornado.VisualEffects.SpawnParticlesInWorld");
		playerview = config.getInt("Tornado.VisualEffects.DistanceFromPlayers");
		isenabled = config.getBoolean("Tornado.Enabled");
		dp = config.getDouble("Tornado.ParticleControl.DegreesBetweenParticles");
		rdp = config.getDouble("Tornado.ParticleControl.RandomDegreesBetweenParticles");
		ddy = config.getDouble("Tornado.ParticleControl.DistanceToNextCircle");
		rddy = config.getDouble("Tornado.ParticleControl.RandomDistanceToNextCircle");
		name = Main.getLanguageLoader().getLanguage().getString("WeatherTypes.Tornado");
		suckenabled = config.getBoolean("Tornado.SuckIn.Enabled");
		suckplayer = config.getBoolean("Tornado.SuckIn.MovePlayer");
		suckmobs = config.getBoolean("Tornado.SuckIn.MoveMobs");
		canfire = config.getBoolean("Tornado.Fire.CanCatchOnFire");
		setblockfire = config.getDouble("Tornado.Fire.ChanceBlockOnFire");
		entityfiredistance = config.getDouble("Tornado.Fire.EntityOnFire");
		firechance = config.getDouble("Tornado.Fire.FlameParticlePercent");
		setEnabledWorlds(config.getStringList("Tornado.DisabledWorlds"));
		destroyblocks = config.getBoolean("Tornado.SuckIn.Blocks.DestroyBlocks");
		destroyradius = config.getInt("Tornado.SuckIn.Blocks.BlockDestroyRadius");
		maxdestroy = config.getInt("Tornado.SuckIn.Blocks.MaxDestroyedBlocks");
		loadBreakableMaterials(config);
		TornadoDirection.setUp();
	}
	
	private static void loadBreakableMaterials(FileConfiguration config)
	{
		List<String> materialnames;
		if(is13Plus()) {
			materialnames = config.getStringList("Tornado.SuckIn.Blocks.BlockDestroyLists.13+");
			setUpBreakableArray(materialnames);
		} else {
			materialnames = config.getStringList("Tornado.SuckIn.Blocks.BlockDestroyLists.Legacy");
			setUpBreakableArray(materialnames);
		}
	}
	
	private static void setUpBreakableArray(List<String> materialnames)
	{
		for(String matname : materialnames) {
			try {
				breakablematerials.add(Material.getMaterial(matname));
			} catch(Exception e) {
				Bukkit.getServer().getLogger().warning(Main.getPrefix() + "There is no material name by " + matname);
			}
		}
	}
	
	private static boolean is13Plus()
	{
		String version = Bukkit.getBukkitVersion();
		for(int i = 13; i <= 20; i++) {
			if(version.contains("1." + Integer.toString(i))) {
				return true;
			}
		}
		return false;
	}
	
	private static void setEnabledWorlds(List<String> disablestrings)
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
	
	@Override
	public void weatherStart()
	{
		LanguageLoader.sendMessage("ChatMessages.loadingtornado", locationString(), activator, true);
	}
	
	@Override
	public boolean weatherEffect()
	{
		if(td.isDone()) {
			LanguageLoader.sendMessage("ChatMessages.doneloadingtornado", locationString(), activator, true);
			startTornado(totaldur);
			return true;
		}
		return false;
	}
	
	public void startTornado(int dur)
	{
		currentduration = 0;
		
		new BukkitRunnable() {
			 @Override
			 public void run() {
				 if(td.finishedPoint(currentduration) || currentlyrunning == false) {
					 stop();
					 cancel();
				 }
				 checkForFire();
				 spawnParticles();
				 suckEntities();
				 destroyBlocks();
				 td.updateLocation(location);
				 currentduration += 2;
			 }
		}.runTaskTimer(Main.getPlugin(), 0, 2);
	}
	
	private void destroyBlocks()
	{
		if(!destroyblocks) {return;}
		Block b;
		Location loc = location.clone();
		Location origin = location.clone().add(0,height/2,0);
		int destroyedcount = 0;
		for(int i = 1; i <= destroyradius && destroyedcount <= maxdestroy; i++) {
			for(int x = -i; x <=i && destroyedcount <= maxdestroy; x++) {
				for(int z = -i; z <= i && destroyedcount <= maxdestroy; z++) {
					if((x >= -(i-1) && x <= i-1) && (z >= -(i-1) && z <= i-1)) {continue;}
					loc.add(x,0,z);
					loc.setY(loc.getWorld().getHighestBlockYAt(loc)-1);
					
					if(WeatherHandler.locationIsProtected(loc) && origin.distance(loc) <= destroyradius) {
						loc.subtract(x,0,z);
						continue;
					}
					
					b = loc.getBlock();
					if(breakablematerials.contains(b.getType())) {
						b.breakNaturally();
						destroyedcount++;
					}
				}
			}
		}
	}
	
	private void checkForFire()
	{
		if(!canfire) {return;}
		
		if(onfire) {
			Random r = new Random();
			if(r.nextDouble() <= setblockfire) {
				Block b = location.getBlock();
				b.setType(Material.FIRE);
			}
			return;
		}
		
		if(shouldGoOnFire()) {
			onfire = true;
		}
	}
	
	private boolean shouldGoOnFire()
	{
		Location loc = location.clone().subtract(0,2,0);
		for(int i = 0; i <= 4 ; i++) {
			loc.add(0,i,0);
			//Remember STATIONARY LAVA for this bit too.
			if(loc.getBlock().getType().equals(Material.FIRE) 
					|| loc.getBlock().getType().equals(Material.LAVA)
					|| loc.getBlock().getType().equals(Material.MAGMA_BLOCK)) {
				return true;
			}
		}
		return false;
	}
	
	private void suckEntities() 
	{
		if(!suckenabled) {return;}
		double[] v;
		location.add(0, height/2, 0);
		for(Entity e : location.getWorld().getNearbyEntities(location, suckr, suckr, suckr)) {
			if((!WeatherHandler.moveNPC() && e.hasMetadata("NPC"))
					|| (!WeatherHandler.moveMinecart() && e instanceof Minecart)
					|| (!WeatherHandler.moveArmourStands() && e instanceof ArmorStand)) {continue;}
			if(e instanceof Player && !suckplayer) {continue;}
			if(!(e instanceof Player) && e instanceof LivingEntity && !suckmobs) {continue;}
			if(e.getLocation().getBlockY() >= WeatherHandler.getHighestY(e.getLocation())) {
				v = getDirectedVec(e.getLocation());
				e.setVelocity(e.getVelocity().add(new Vector(v[0],v[1],v[2])));
				if(canfire && onfire && location.distance(e.getLocation())<= entityfiredistance) {
					e.setFireTicks(20);
				}
			}
		}
		location.subtract(0, height/2, 0);
	}
	
	private double[] getDirectedVec(Location eloc) 
	{
		Location diff = location.clone().subtract(eloc);
		double suckm = getSuckMag(location, eloc);
		if(location.distance(eloc) <= r1) {
			return WeatherHandler.vector3D(0.0, 1.0, 0.0, (float) 1.0);
		}
		return WeatherHandler.vector3D(diff.getX(), diff.getY(), diff.getZ(), (float) suckm);
	}
	
	private double getSuckMag(Location loc, Location eloc)
	{
		return (double) 0.25*suckr/loc.distance(eloc);
	}
	
	private void spawnParticles()
	{
		Random r = new Random();
		double rchange, ychange;
		double dx;
		double dz;
		double[] v = td.getCurrentVelocity();
		double speed = td.getSpeed();
		for(double i = 0; i < 360; i += dp) {
			for(double dy = 0; dy <= height && dy <= (double) height/20*currentduration; dy += ddy) {
				rchange = rdp*r.nextDouble()*(r.nextBoolean() ? 1:-1);
				ychange = rddy*r.nextDouble()*(r.nextBoolean() ? 1:-1);
				dx = currentRadius(dy + ychange)*Math.cos(Math.toRadians(i + rchange));
				dz = currentRadius(dy + ychange)*Math.sin(Math.toRadians(i + rchange));
				location.add(dx,dy + ychange,dz);
				
				if(worldspawn == true) {
					
					if(onfire && r.nextDouble() <= firechance) {
						location.getWorld().spawnParticle(Particle.FLAME, location,  0, v[0], v[1], v[2], 4*speed);
					} else {
						location.getWorld().spawnParticle(Particle.CLOUD, location,  0, v[0], v[1], v[2], 4*speed);
					}
				} else {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(location.distance(p.getLocation()) <= playerview) {
							if(onfire && r.nextDouble() <= firechance) {
								p.spawnParticle(Particle.FLAME, location,  0, v[0], v[1], v[2], 4*speed);
							} else {
								p.spawnParticle(Particle.CLOUD, location,  0, v[0], v[1], v[2], 4*speed);
							}
						}
						
					}
				}
				location.subtract(dx, dy + ychange, dz);
			}
		}
	}
	
	private double currentRadius(double y)
	{
		double radius = (double)(((double)(r2-r1))/((double)height))*y + r1;
		return radius;
	}
	
	public static boolean onCommandSet(CommandSender sender, String[] args)
	{
		if(args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.Tornado"))
				|| args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.FireTornado"))) {
			if(args.length == 2) {
				if(sender instanceof Player) {
					/*
					Random r = new Random();
					Location l = ((Player) sender).getLocation().add(r.nextInt(15), 0, r.nextInt(15));
					Tornado t = new Tornado(l, (Player) sender);
					 */
					Tornado t = new Tornado(((Player) sender).getTargetBlock(null, 200).getLocation(), (Player) sender);
					if(args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.FireTornado"))) {
						t.setFire(true);
					}
				} else {
					if(!Bukkit.getOnlinePlayers().isEmpty()) {
						Random r = new Random();
						Location l = Bukkit.getOnlinePlayers().iterator().next().getLocation().add(
								r.nextInt(Tornado.getSpawnRadius()), 0, r.nextInt(Tornado.getSpawnRadius()));
						Tornado t = new Tornado(l);
						if(args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.FireTornado"))) {
							t.setFire(true);
						}
					} else {
						String msg = Main.getLanguageLoader().lineInterpreter(
								Main.getLanguageLoader().getLanguage().getString("ChatMessages.noonlinenotornado"), 
								"");
						Commands.sendMessage(msg, sender);
					}
				}
			} else {
				try {
					if(sender instanceof Player) {
						/*
						Random r = new Random();
						Location l = ((Player) sender).getLocation().add(r.nextInt(15), 0, r.nextInt(15));
						Tornado t = new Tornado(l, (Player) sender);
						 */
						Tornado t  = new Tornado(((Player) sender).getTargetBlock(null, 200).getLocation(), Integer.parseInt(args[2])*20, (Player) sender);
						if(args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.FireTornado"))) {
							t.setFire(true);
						}
					} else {
						if(!Bukkit.getOnlinePlayers().isEmpty()) {
							Random r = new Random();
							Location l = Bukkit.getOnlinePlayers().iterator().next().getLocation().add(
									r.nextInt(Tornado.getSpawnRadius()), 0, r.nextInt(Tornado.getSpawnRadius()));
							Tornado t = new Tornado(l, Integer.parseInt(args[2])*20);
							if(args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.FireTornado"))) {
								t.setFire(true);
							}
						} else {
							String msg = Main.getLanguageLoader().lineInterpreter(
									Main.getLanguageLoader().getLanguage().getString("ChatMessages.noonlinenotornado"), 
									"");
							Commands.sendMessage(msg, sender);
						}
					}
				} catch (Exception e){
					String msg = Main.getLanguageLoader().lineInterpreter(Main.getLanguageLoader().getLanguage().getString("ChatMessages.needint"), "");
					Commands.sendMessage(msg, sender);
				}
			}
			return true;
		}
		return false;
	}
	
	public static boolean onCommandStop(CommandSender sender, String[] args) 
	{
		if(args.length != 2) {return false;}
		if(args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.Tornado"))
				|| args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.FireTornado"))) {
			String msg = Main.getLanguageLoader().lineInterpreter(
					Main.getLanguageLoader().getLanguage().getString("ChatMessages.stopweather"), 
					Main.getLanguageLoader().getLanguage().getString("WeatherTypes.Tornado"));
			Commands.sendMessage(msg, sender);
			PointWeather.stopWeather(Tornado.class);
			return true;
		} 
		return false;
	}
	
	public static void naturalStart(World world)
	{
		Random r = new Random();
		if(r.nextDouble() < spawnchance && isenabled && enabledworlds.contains(world)) {
			if((world.equals(null) ? Bukkit.getOnlinePlayers().isEmpty() : world.getPlayers().isEmpty())) {return;}
			Player p = (world.equals(null) 
					? getPlayerInValidWorld() : world.getPlayers().iterator().next());
			if(p.equals(null)) {return;}
			Location l = p.getLocation().add(r.nextInt(spawnradius), 0, r.nextInt(spawnradius));
			new Tornado(l);
		}
	}
	
	private static Player getPlayerInValidWorld()
	{
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(enabledworlds.contains(p.getWorld())) {
				return p;
			}
		}
		return null;
	}
	
	public static String getName()
	{
		return name;
	}
	
	public static boolean isEnabled() 
	{
		return isenabled;
	}
	
	public static double getSpawnChance()
	{
		return spawnchance;
	}
	
	public static int getLoadSpeed()
	{
		return loadspeed;
	}
	
	public static int getSpawnRadius()
	{
		return spawnradius;
	}
	
	public static double getChangeChance()
	{
		return changechance;
	}
	
	public void setFire(boolean fire)
	{
		onfire = fire;
	}
	
	/*For Legacy
	if(args[1].equalsIgnoreCase(language.getString("WeatherTypes.Tornado"))) {
		if(args.length == 2) {
			if(sender instanceof Player) {
				Random r = new Random();
				Location l = ((Player) sender).getLocation().add(r.nextInt(15), 0, r.nextInt(15));
				new Tornado(l, (Player) sender);
			} else {
				if(!Bukkit.getOnlinePlayers().isEmpty()) {
					Random r = new Random();
					Location l = Bukkit.getOnlinePlayers().iterator().next().getLocation().add(
							r.nextInt(Tornado.getSpawnRadius()), 0, r.nextInt(Tornado.getSpawnRadius()));
					new Tornado(l);
				} else {
					String msg = Main.getLanguageLoader().lineInterpreter(language.getString("ChatMessages.noonlinenotornado"), 
							"");
					sendMessage(msg, sender);
				}
			}
		} else {
			try {
				if(sender instanceof Player) {
					Random r = new Random();
					Location l = ((Player) sender).getLocation().add(r.nextInt(15), 0, r.nextInt(15));
					new Tornado(l, (Player) sender);
				} else {
					if(!Bukkit.getOnlinePlayers().isEmpty()) {
						Random r = new Random();
						Location l = Bukkit.getOnlinePlayers().iterator().next().getLocation().add(
								r.nextInt(Tornado.getSpawnRadius()), 0, r.nextInt(Tornado.getSpawnRadius()));
						new Tornado(l, Integer.parseInt(args[2])*20);
					} else {
						String msg = Main.getLanguageLoader().lineInterpreter(language.getString("ChatMessages.noonlinenotornado"), 
								"");
						sendMessage(msg, sender);
					}
				}
			} catch (Exception e){
				String msg = Main.getLanguageLoader().lineInterpreter(language.getString("ChatMessages.needint"), "");
				sendMessage(msg, sender);
			}
		}
		return true;
	}
   */
}
