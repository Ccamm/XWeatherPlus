package me.Ccamm.XWeatherPlus.Weather.World;

import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public interface WorldWeatherType 
{
	void start(World world);
	void start(int dur, World world);
	void stop(World world);
	boolean onCommandSet(CommandSender sender, String[] args);
	boolean onCommandStop(CommandSender sender, String[] args);
	double getSpawnChance();
	int getDefaultDuration();
	boolean isEnabled();
	void loadConfig(FileConfiguration config);
	String getName();
	String getConfigPrefix();
	HashSet<World> getEnabledWorlds();
}
