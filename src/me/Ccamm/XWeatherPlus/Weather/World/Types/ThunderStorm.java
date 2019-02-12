package me.Ccamm.XWeatherPlus.Weather.World.Types;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.Ccamm.XWeatherPlus.Weather.WeatherHandler;
import me.Ccamm.XWeatherPlus.Weather.World.WorldWeather;

public class ThunderStorm extends WorldWeather
{
	private static int playerradius;
	private static double lightningchance;
	private static int metalradius;
	private static boolean metalattract;
	private static ThunderStorm thunderstorm = null;
	
	private ThunderStorm(FileConfiguration config) 
	{
		super("ThunderStorm", config, 60);
		loadConfig(config);
		WorldWeather.addWeatherType(this);
	}
	
	public static ThunderStorm setUpThunderstorms(FileConfiguration config)
	{
		if(thunderstorm == null) {
			thunderstorm = new ThunderStorm(config);
		} else {
			thunderstorm.loadConfig(config);
			thunderstorm.reloadName();
		}
		return thunderstorm;
	}
	
	@Override
	public void loadMoreOptions(FileConfiguration config)
	{
		playerradius = config.getInt("ThunderStorm.RadiusAroundPlayer");
		lightningchance = config.getDouble("ThunderStorm.LightningChance");
		metalradius = config.getInt("ThunderStorm.MetalRadiusAroundPlayer");
		metalattract = config.getBoolean("ThunderStorm.MetalAttractLightning");
	}
	
	@Override
	protected void weatherEffect(World w)
	{
		Random r = new Random();
		if(r.nextDouble() <= lightningchance && !w.getPlayers().isEmpty()) {
			Location l;
			Player p = w.getPlayers().iterator().next();
			
			if(isWearingMetal(p) && metalattract) {
				l = p.getLocation().add(
						r.nextInt(metalradius)*((r.nextBoolean())? 1:-1), 0, r.nextInt(metalradius)*((r.nextBoolean())? 1:-1));
			} else {
				l = p.getLocation().add(
						r.nextInt(playerradius)*((r.nextBoolean())? 1:-1), 0, r.nextInt(playerradius)*((r.nextBoolean())? 1:-1));
			}
			l.setY(WeatherHandler.getHighestY(l));
			if(WeatherHandler.locationIsProtected(l) || !WeatherHandler.isLocationLoaded(l)) {return;}
			l.getWorld().strikeLightning(l);
		}
	}
	
	@Override
	protected void startWeather(World w, int dur)
	{
		WeatherHandler.setRain(w, dur);
		WeatherHandler.setThundering(w, dur);
	}
	
	private boolean isWearingMetal(Player p)
	{
		Material[] metalarmour = {Material.CHAINMAIL_HELMET, Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS,
				Material.IRON_BOOTS, Material.IRON_CHESTPLATE, Material.IRON_HELMET, Material.IRON_LEGGINGS,
				Material.GOLDEN_BOOTS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET, Material.GOLDEN_LEGGINGS};
		
		PlayerInventory i = p.getInventory();
		if(i.getArmorContents().length >= 1) {
			for(ItemStack is : i.getArmorContents()) {
				if(is == null) {continue;}
				for(Material m : metalarmour) {
					if(is.getType().equals(m)) {
						return true;
					}
				}
			}
		}
		
		Material[] metalitems = {Material.IRON_AXE, Material.IRON_BARS, Material.IRON_BLOCK, Material.IRON_DOOR, Material.IRON_HOE,
				Material.IRON_HORSE_ARMOR, Material.IRON_INGOT, Material.IRON_NUGGET, Material.IRON_PICKAXE, Material.IRON_SHOVEL,
				Material.IRON_SWORD, Material.IRON_TRAPDOOR, Material.GOLD_BLOCK, Material.GOLD_INGOT, Material.GOLD_NUGGET, 
				Material.GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.GOLDEN_AXE, Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, 
				Material.GOLDEN_SHOVEL, Material.GOLDEN_SWORD};
		
		/*
		Material[] metalitems = {Material.IRON_AXE, Material.IRON_BLOCK, Material.IRON_DOOR, Material.IRON_HOE,
				Material.IRON_BARDING, Material.IRON_INGOT, Material.IRON_PLATE, Material.IRON_PICKAXE, Material.IRON_SPADE,
				Material.IRON_SWORD, Material.IRON_TRAPDOOR, Material.GOLD_BLOCK, Material.GOLD_INGOT, Material.GOLD_NUGGET, 
				Material.GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.GOLD_AXE, Material.GOLD_HOE, Material.GOLD_PICKAXE, 
				Material.GOLD_SPADE, Material.GOLD_SWORD};
		 */
		
		for(Material m : metalitems) {
			if(i.getItemInMainHand().getType() == m) {
				return true;
			} else if(i.getItemInOffHand().getType() == m) {
				return true;
			}
		}
		
		for(Material m : metalarmour) {
			if(i.getItemInMainHand().getType() == m) {
				return true;
			} else if(i.getItemInOffHand().getType() == m) {
				return true;
			}
		}
		
		return false;
	}
}
