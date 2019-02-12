package me.Ccamm.XWeather;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import me.Ccamm.XWeather.Weather.WeatherHandler;
import me.Ccamm.XWeather.Weather.World.Types.HailStorm;

public class Events implements Listener
{
	HashSet<UUID> hailstonedeaths = new HashSet<UUID>();
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		if(p.hasPermission("xweather.admin") || p.isOp()) {
			Updater update = Main.getUpdater();
			update.checkForUpdates(p);
		}
	}
	
	@EventHandler
	public void onHailstoneHit(ProjectileHitEvent e)
	{
		if(!HailStorm.canDamage()) {return;}
		
		Projectile proj =  e.getEntity();
		Entity hit = e.getHitEntity();
		if(hit instanceof LivingEntity && hit instanceof Damageable 
				|| hit instanceof LivingEntity && hit instanceof Damageable && (!WeatherHandler.moveNPC() && hit.hasMetadata("NPC"))) {
			if(proj instanceof Snowball && proj.hasMetadata("hailstone")) {
				if(hit instanceof Player) {
					Player p = (Player) hit;
					if(p.getHealth()-HailStorm.getDamage() <= 0) {
						hailstonedeaths.add(p.getUniqueId());
					}
				}
				((Damageable) hit).damage(HailStorm.getDamage(), proj);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		Player p = e.getEntity();
		if(hailstonedeaths.contains(p.getUniqueId())) {
			hailstonedeaths.remove(p.getUniqueId());
			e.setDeathMessage(Main.getLanguageLoader().lineInterpreter(
					Main.getLanguageLoader().getLanguage().getString("ChatMessages.hailstonedeath"), 
					p.getName()));
		}
	}
	
	@EventHandler
	public void onHailstoneHitEvent(ProjectileHitEvent e) {
		if(!(e.getEntity() instanceof Snowball)) {return;}
		if(!HailStorm.makeSound()) {return;}
		Snowball sb = (Snowball) e.getEntity();
		if(sb.hasMetadata("hailstone")) {
			sb.getWorld().playSound(sb.getLocation(), Sound.BLOCK_STONE_HIT, (float) 0.15, 0);
		}
	}
}
