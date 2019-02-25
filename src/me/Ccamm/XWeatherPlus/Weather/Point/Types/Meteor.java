package me.Ccamm.XWeatherPlus.Weather.Point.Types;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.Ccamm.XWeatherPlus.LanguageLoader;
import me.Ccamm.XWeatherPlus.Main;
import me.Ccamm.XWeatherPlus.Weather.ArmourStandMaker;
import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.World.Types.MeteorShower;

public class Meteor 
{
	private static HashSet<Meteor> meteors = new HashSet<Meteor>();
	private static double firepartchance = 0.2;
	private static double smallchange = 0.1;
	
	private HashSet<ArmourStandMaker> meteorparts = new HashSet<ArmourStandMaker>();
	private ArmourStandMaker followed = null;
	private float explosionsize;
	private boolean fire;
	private int count = 0;
	
	public Meteor(Location location, float explosionsize, boolean firemeteor)
	{
		this.explosionsize = explosionsize;
		this.fire = firemeteor;
		Location loc = location.clone().subtract(0,2,0);
		createMeteorParts(loc);
		meteors.add(this);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				Location loc = followed.getLocation();
				if(count % 5 == 0) {
					if(loc.getBlockY() <= WeatherHandler.getHighestY(loc) + 2) {
						removeParts();
						loc.getWorld().createExplosion(loc, explosionsize, fire);
						cancel();
					}
				}
				spawnParticles(loc);
				count++;
			}
		}.runTaskTimer(Main.getPlugin(), 0, 1);
	}
	
	private void spawnParticles(Location location) {
		Location loc = location.clone();
		Random r = new Random();
		double dx,dz;
		for(double x = 0; x <= 0.5; x += 0.25) {
			for(double z = 0; z <= 0.5; z += 0.25) {
				for(Player p : loc.getWorld().getPlayers()) {
					if(p.getLocation().distance(loc) <= 100) {
						dx = x + smallchange*r.nextDouble()*(r.nextBoolean() ? 1:-1);
						dz = z + smallchange*r.nextDouble()*(r.nextBoolean() ? 1:-1);
						loc.setX(location.getX() + dx);
						loc.setY(location.getY() + 5*r.nextDouble());
						loc.setZ(location.getZ() + dz);
						p.spawnParticle((r.nextDouble() <= firepartchance ? Particle.FLAME : Particle.SMOKE_LARGE), 
								loc, 0, 0, 0, 0);
					}
				}
			}
		}
	}
	
	private void createMeteorParts(Location location)
	{
		ArmourStandMaker asm;
		Location loc = location.clone();
		for(double y = -0.5; y <= 0.5; y+=0.5) {
			for(double x = -0.25; x <= 0.25; x+=0.5) {
				for(double z = -0.25; z <= 0.25; z+=0.5) {
					loc.setX(location.getX() + x);
					loc.setY(location.getY() + y);
					loc.setZ(location.getZ() + z);
					asm = new ArmourStandMaker(loc, Material.MAGMA_BLOCK, false, false, true, "METEOR");
					if(followed == null) {
						followed = asm;
					}
					meteorparts.add(asm);
				}
			}
		}
		loc.setY(location.getY() - 0.75);
		loc.setX(location.getX());
		loc.setZ(location.getZ());
		meteorparts.add(new ArmourStandMaker(loc, Material.MAGMA_BLOCK, false, false, true, "METEOR"));
		loc.setY(location.getY() + 0.75);
		meteorparts.add(new ArmourStandMaker(loc, Material.MAGMA_BLOCK, false, false, true, "METEOR"));
		loc.setY(location.getY() + 0.25);
		loc.setX(location.getX() - 0.5);
		meteorparts.add(new ArmourStandMaker(loc, Material.MAGMA_BLOCK, false, false, true, "METEOR"));
		loc.setX(location.getX() + 0.5);
		meteorparts.add(new ArmourStandMaker(loc, Material.MAGMA_BLOCK, false, false, true, "METEOR"));
		loc.setX(location.getX());
		loc.setZ(location.getZ() - 0.5);
		meteorparts.add(new ArmourStandMaker(loc, Material.MAGMA_BLOCK, false, false, true, "METEOR"));
		loc.setZ(location.getZ() + 0.5);
		meteorparts.add(new ArmourStandMaker(loc, Material.MAGMA_BLOCK, false, false, true, "METEOR"));
	}
	
	private void removeParts()
	{
		for(ArmourStandMaker asm : meteorparts) {
			asm.delete();
		}
	}
	
	public static boolean onCommandSet(CommandSender sender, String[] args)
	{
		if(args[1].equalsIgnoreCase(Main.getLanguageLoader().getLanguage().getString("WeatherTypes.Meteor"))) {
			Player p = null;
			Location l = null;
			if(sender instanceof Player) {
				/*
				 * Random r = new Random();
				 * l = ((Player) sender).getLocation().add(r.nextInt(15), 0, r.nextInt(15));
				 */
				p = (Player) sender;
				l = ((Player) sender).getTargetBlock(null, 200).getLocation();
				l.setY(l.getWorld().getMaxHeight());
				new Meteor(l, 
						MeteorShower.getExplosionSize(),
						MeteorShower.isFire());
			} else if(!Bukkit.getOnlinePlayers().isEmpty()) {
				Random r = new Random();
				l = Bukkit.getOnlinePlayers().iterator().next().getLocation().add(
						r.nextInt(MeteorShower.getSpawnRadius()), 0, r.nextInt(MeteorShower.getSpawnRadius()));
				l.setY(l.getWorld().getMaxHeight());
				new Meteor(l, 
						MeteorShower.getExplosionSize(),
						MeteorShower.isFire());
			}
			LanguageLoader.sendMessage("ChatMessages.singlemeteor", locationString(l), p, true);
			return true;
		}
		return false;
	}
	
	private static String locationString(Location location)
	{
		return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
	}
	
	public float getExplosion() {return explosionsize;}
	public boolean isFire() {return fire;}
}
