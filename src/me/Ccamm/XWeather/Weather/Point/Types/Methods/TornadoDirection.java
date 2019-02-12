package me.Ccamm.XWeather.Weather.Point.Types.Methods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import me.Ccamm.XWeather.Main;
import me.Ccamm.XWeather.Weather.WeatherHandler;
import me.Ccamm.XWeather.Weather.Point.Types.Tornado;

public class TornadoDirection 
{
	private static double maxspeed = 0.18;
	private static HashSet<Material> ignoredblocks = new HashSet<Material>();
	private static int loadspeed;
	private static double changechance;
	
	private int count = 0;
	private double[] vt;
	private double speed;
	private int i = 1;
	private List<double[]> location = new ArrayList<double[]>();
	private List<double[]> velocity = new ArrayList<double[]>();
	private boolean done = false;
	private World w;
	
	public TornadoDirection(Location loc, int duration)
	{
		setUpVelocity();
		this.w = loc.getWorld();
		double[] initialloc = {loc.getX(), loc.getY(), loc.getZ()};
		double[] initialvel = vt;
		location.add(initialloc);
		velocity.add(initialvel);
		setPath(duration);
	}
	
	public double[] getVelocity()
	{
		return velocity.get(count);
	}
	
	public void nextStep()
	{
		count++;
	}
	
	public double getSpeed()
	{
		return speed;
	}
	
	public void updateLocation(Location loc)
	{
		loc.setX(location.get(count)[0]);
		loc.setY(location.get(count)[1]);
		loc.setZ(location.get(count)[2]);
		nextStep();
	}
	
	public double[] getCurrentVelocity()
	{
		if(count >= velocity.size()) {
			return vt;
		}
		return WeatherHandler.normalisedvector(velocity.get(count));
	}
	
	private void setPath(int duration)
	{
		new BukkitRunnable() {
			 @Override
			 public void run() {
				 Random r = new Random();
				 Location loc;
				 for(int j = 0; j < loadspeed; j++) {
					 if(r.nextDouble() <= changechance) {
						 setUpVelocity();
					 }
					 update(i);
					 i++;
					 loc = new Location(w, location.get(i-1)[0], location.get(i-1)[1], location.get(i-1)[2]);
					 if(i > duration/2 
							 || WeatherHandler.locationIsProtected(loc)
							 || !WeatherHandler.isLocationLoaded(loc)) {
						 done = true;
						 cancel();
					 }
				 }
			 }
		}.runTaskTimer(Main.getPlugin(), 0, 1);
		/*for(int i = 1; i <= duration/2; i++) {
			update(i);
		}*/
	}
	
	public boolean finishedPoint(int point)
	{
		if(2*(i-2) < point) {
			return true;
		}
		return false;
	}
	
	private void update(int i)
	{
		updateVelocity(i);
		double[] preloc = location.get(i-1);
		double[] v = velocity.get(i);
		double[] nextloc = {preloc[0] + v[0], preloc[1] + v[1], preloc[2] + v[2]};

		nextloc[1] = getNextY(nextloc[0], nextloc[1], nextloc[2]);
		location.add(nextloc);
	}
	
	private double getNextY(double x, double y, double z)
	{
		Location l = new Location(w,x,y,z);
		if(y <= 0 || y >= l.getWorld().getMaxHeight()) {return y;}
		Block b = l.getBlock();
		
		if(ignoredblocks.contains(b.getType())) {
			return y;
		}
		
		if(b.getType() != Material.AIR) {
			return getNextY(x, y+1, z);
		}
		l.setY(y-1);
		b = l.getBlock();
		if(b.getType() == Material.AIR) {
			return getNextY(x, y-1, z);
		}
		
		return y;
	}
	
	private void updateVelocity(int i)
	{
		double[] prev = velocity.get(i-1);
		double[] newv = {prev[0] + 0.05*(vt[0]-prev[0]), prev[1] + 0.05*(vt[1]-prev[1]), prev[2] + 0.05*(vt[2]-prev[2])};
		velocity.add(WeatherHandler.normalisedvector(newv));
	}
	
	private void setUpVelocity()
	{
		Random r = new Random();
		double[] vel = {r.nextDouble()*(r.nextBoolean() ? 1:-1), 0, r.nextDouble()*(r.nextBoolean() ? 1:-1)};
		this.speed = maxspeed; //*r.nextDouble();
		this.vt = WeatherHandler.normalisedvector(speed*vel[0], speed*vel[1], speed*vel[2]);
	}
	
	public static void setUp()
	{
		ignoredblocks.add(Material.ACACIA_LEAVES);
		ignoredblocks.add(Material.BIRCH_LEAVES);
		ignoredblocks.add(Material.DARK_OAK_LEAVES);
		ignoredblocks.add(Material.JUNGLE_LEAVES);
		ignoredblocks.add(Material.OAK_LEAVES);
		ignoredblocks.add(Material.SPRUCE_LEAVES);
		ignoredblocks.add(Material.ACACIA_WOOD);
		ignoredblocks.add(Material.BIRCH_WOOD);
		ignoredblocks.add(Material.DARK_OAK_WOOD);
		ignoredblocks.add(Material.JUNGLE_WOOD);
		ignoredblocks.add(Material.OAK_WOOD);
		ignoredblocks.add(Material.SPRUCE_WOOD);
		/* For Legacy
		ignoredblocks.add(Material.WOOD);
		ignoredblocks.add(Material.LEAVES);
		ignoredblocks.add(Material.LEAVES_2);
		*/
		loadspeed = Tornado.getLoadSpeed();
		changechance = Tornado.getChangeChance();
	}
	
	public boolean isDone() 
	{
		return done;
	}
}
