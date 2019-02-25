package me.Ccamm.XWeatherPlus.Weather.World.Types;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.Ccamm.XWeatherPlus.Main;
import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.World.WorldWeather;

public class AcidRain extends WorldWeather
{
	private static AcidRain acidrain = null;
	
	private static int particlecount;
	private static int playerradius;
	private static double poisonradius;
	private static ArrayList<PotionEffect> potioneffects = new ArrayList<PotionEffect>();
	
	private AcidRain(FileConfiguration config)
	{
		super("AcidRain", config, 1);
		WorldWeather.addWeatherType(this);
	}
	
	public static AcidRain setUpAcidRain(FileConfiguration config)
	{
		if(acidrain == null) {
			acidrain = new AcidRain(config);
		} else {
			acidrain.loadConfig(config);
			acidrain.reloadName();
		}
		return acidrain;
	}
	
	@Override
	protected void loadMoreOptions(FileConfiguration config)
	{
		particlecount = config.getInt("AcidRain.ParticleCount");
		playerradius = config.getInt("AcidRain.RadiusAroundPlayer");
		poisonradius = config.getDouble("AcidRain.PoisonRadius");
		loadPotionEffects(config);
	}
	
	private void loadPotionEffects(FileConfiguration config)
	{
		ConfigurationSection cnfgsec = config.getConfigurationSection("AcidRain.PoisonEffects");
		if(cnfgsec.getKeys(false).isEmpty()) {
			return;
		}
		int[] durandamp = {0,0};
		PotionEffectType pet;
		for(String s : cnfgsec.getKeys(false)) {
			try {
				pet = PotionEffectType.getByName(s);
				durandamp[0] = config.getInt("AcidRain.PoisonEffects." + s + ".Duration")*20;
				durandamp[1] = config.getInt("AcidRain.PoisonEffects." + s + ".Amplitude")-1;
				potioneffects.add(new PotionEffect(pet, durandamp[0], durandamp[1]));
			} catch (Exception e) {
				Bukkit.getServer().getLogger().warning(Main.getPrefix() + 
						"In AcidRain, PotionEffect " + s + " is set up incorrectly!");
			}
		}
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
			if(WeatherHandler.locationIsProtected(p.getLocation())
					|| WeatherHandler.isDryBiome(p.getLocation())
					|| WeatherHandler.isSnowBiome(p.getLocation())) {continue;}
			spawnAcid(p);
		}
	}
	
	private void acidDrop(Location loc)
	{
		loc.getWorld().spawnParticle(Particle.SLIME, loc, 5);
		loc.getWorld().playSound(loc, Sound.ENTITY_SLIME_SQUISH_SMALL, (float) 0.4, 0);
		infectLocation(loc);
	}
	
	private void infectLocation(Location loc) {
		LivingEntity le;
		for(Entity e : loc.getWorld().getNearbyEntities(loc, poisonradius, poisonradius, poisonradius)) {
			if(!(e instanceof LivingEntity) 
					|| (e instanceof Minecart && WeatherHandler.moveMinecart())
					|| (e instanceof ArmorStand && WeatherHandler.moveArmourStands())
					|| (e.hasMetadata("NPC") && WeatherHandler.moveNPC())) {
				continue;
			}
			le = (LivingEntity) e;
			for(PotionEffect pe : potioneffects) {
				le.addPotionEffect(pe);
			}
		}
	}
	
	private void spawnAcid(Player p)
	{
		Random r = new Random();
		Location loc = p.getLocation().clone();
		double dx, dz;
		
		for(int i = 0; i < particlecount; i++) {
			dx = playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			dz = playerradius*r.nextDouble()*(r.nextBoolean() ? 1:-1);
			loc.add(dx, 0, dz);
			loc.setY(WeatherHandler.getHighestY(loc));
			if(!WeatherHandler.isLocationLoaded(loc)
					|| WeatherHandler.isDryBiome(loc)
					|| WeatherHandler.locationIsProtected(loc)
					|| WeatherHandler.isSnowBiome(loc)) {
				continue;
			}
			acidDrop(loc);
		}
	}
}
