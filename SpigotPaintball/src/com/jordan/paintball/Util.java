package com.jordan.paintball;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Util {
	
	public static Location stringToLocation(String str) {
		String[] split = str.split(",");
		String world = split[0];
		double x = Double.valueOf(split[1]);
		double y = Double.valueOf(split[2]);
		double z = Double.valueOf(split[3]);
		float yaw = Float.valueOf(split[4]);
		float pitch = Float.valueOf(split[5]);
		Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
		return loc;
	}
	
	public static int getDistance(Location loc1, Location loc2) {
		int loc1X = loc1.getBlockX();
		int loc1Y = loc1.getBlockY();
		int loc1Z = loc1.getBlockZ();
		int loc2X = loc2.getBlockX();
		int loc2Y = loc2.getBlockY();
		int loc2Z = loc2.getBlockZ();
		int distance = (int) Math.sqrt((loc2X - loc1X) ^ 2 + (loc2Y - loc1Y) ^ 2 + (loc2Z - loc1Z) ^ 2);
		return distance;
	}

}
