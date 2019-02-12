package me.Ccamm.XWeather;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery; 

public class WorldGuardManager 
{
	private static WorldGuardManager wgm = null;
	private static Flag<?> NO_WEATHER;
	
	private WorldGuardManager()
	{	
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		NO_WEATHER = new StateFlag("no-xweather", true);
		try {
			registry.register(NO_WEATHER);
		} catch (FlagConflictException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static WorldGuardPlugin getWorldGuard() 
	{
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }

	    return (WorldGuardPlugin) plugin;
	}
	
	public static WorldGuardManager setUp()
	{
		if(wgm == null) {
			wgm = new WorldGuardManager();
		}
		return wgm;
	}
	
	public static boolean locationIsProtected(Location loc) throws NoClassDefFoundError
	{
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
		for(ProtectedRegion region : set) {
			if(region.getFlag(NO_WEATHER) != null) {
				return true;
			}
		}
		return false;
	}
}

/*For Legacy
public class WorldGuardManager 
{
	private static WorldGuardManager wgm = null;
	private static Flag<?> NO_WEATHER;
	
	private WorldGuardManager()
	{	
		FlagRegistry registry = getWorldGuard().getFlagRegistry();
		NO_WEATHER = new StateFlag("no-xweather", true);
		try {
			registry.register(NO_WEATHER);
		} catch (FlagConflictException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static WorldGuardPlugin getWorldGuard() 
	{
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }

	    return (WorldGuardPlugin) plugin;
	}
	
	public static WorldGuardManager setUp()
	{
		if(wgm == null) {
			wgm = new WorldGuardManager();
		}
		return wgm;
	}
	
	public static boolean locationIsProtected(Location loc) throws NoClassDefFoundError
	{
		ApplicableRegionSet set = WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(loc);
		for(ProtectedRegion region : set) {
			if(region.getFlag(NO_WEATHER) != null) {
				return true;
			}
		}
		return false;
	}
}
*/