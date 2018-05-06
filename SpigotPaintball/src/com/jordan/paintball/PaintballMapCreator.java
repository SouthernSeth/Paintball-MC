package com.jordan.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

public class PaintballMapCreator implements Listener {

	public ArrayList<String> creators = new ArrayList<String>();
	public ArrayList<String> naming = new ArrayList<String>();
	public ArrayList<String> settingflags = new ArrayList<String>();
	public HashMap<String, MapSettings> containers = new HashMap<String, MapSettings>();

	public SpigotPaintball paintball = null;

	public PaintballMapCreator(SpigotPaintball paintball) {
		this.paintball = paintball;
	}

	public boolean registerCreator(Player player) {
		for (int i = 0;i<creators.size();i++) {
			if (creators.get(i).equalsIgnoreCase(player.getDisplayName())) {
				return false;
			}
		}
		player.getInventory().clear();
		givePlayerTeleportingCompass(player);
		givePlayerMapCreationTools(player);
		creators.add(player.getDisplayName());
		return true;
	}

	public boolean unregisterCreator(Player player) {
		for (int i = 0;i<creators.size();i++) {
			if (creators.get(i).equalsIgnoreCase(player.getDisplayName())) {
				creators.remove(i);
				player.getInventory().clear();
				return true;
			}
		}
		return false;
	}

	public void givePlayerTeleportingCompass(Player player) {
		ItemStack item = new ItemStack(Material.COMPASS, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Quick Teleport");
		meta.setLore(new ArrayList<String>());
		item.setItemMeta(meta);
		player.getInventory().setItem(0, item);
	}

	public void givePlayerMapCreationTools(Player player) {
		Wool redwool = new Wool();
		redwool.setColor(DyeColor.RED);
		ItemStack red = redwool.toItemStack(1);
		ItemMeta redmeta = red.getItemMeta();
		redmeta.setDisplayName(ChatColor.RED + "Set Red Team Spawn");
		redmeta.setLore(new ArrayList<String>());
		red.setItemMeta(redmeta);

		Wool bluewool = new Wool();
		bluewool.setColor(DyeColor.BLUE);
		ItemStack blue = bluewool.toItemStack(1);
		ItemMeta bluemeta = blue.getItemMeta();
		bluemeta.setDisplayName(ChatColor.BLUE + "Set Blue Team Spawn");
		bluemeta.setLore(new ArrayList<String>());
		blue.setItemMeta(bluemeta);

		ItemStack lobby = new ItemStack(Material.WOOL, 1);
		ItemMeta lobbymeta = lobby.getItemMeta();
		lobbymeta.setDisplayName(ChatColor.WHITE + "Set Lobby Spawn");
		lobbymeta.setLore(new ArrayList<String>());
		lobby.setItemMeta(lobbymeta);

		ItemStack name = new ItemStack(Material.NAME_TAG, 1);
		ItemMeta namemeta = name.getItemMeta();
		namemeta.setDisplayName(ChatColor.GREEN + "Set Map Name");
		namemeta.setLore(new ArrayList<String>());
		name.setItemMeta(namemeta);

		ItemStack ctf = new ItemStack(Material.BANNER);
		ItemMeta ctfmeta = ctf.getItemMeta();
		ctfmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Capture the Flag");
		ctfmeta.setLore(new ArrayList<String>());
		ctf.setItemMeta(ctfmeta);

		ItemStack koth = new ItemStack(Material.BEACON);
		ItemMeta kothmeta = koth.getItemMeta();
		kothmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "King of the Hill");
		kothmeta.setLore(new ArrayList<String>());
		koth.setItemMeta(kothmeta);

		ItemStack saveTool = new ItemStack(Material.DIAMOND);
		ItemMeta savemeta = saveTool.getItemMeta();
		savemeta.setDisplayName(ChatColor.GOLD + "Save Map");
		savemeta.setLore(new ArrayList<String>());
		saveTool.setItemMeta(savemeta);

		player.getInventory().setItem(1, name);
		player.getInventory().setItem(2, lobby);
		player.getInventory().setItem(3, red);
		player.getInventory().setItem(4, blue);
		player.getInventory().setItem(5, ctf);
		player.getInventory().setItem(6, koth);
		player.getInventory().setItem(7, saveTool);
	}

	@EventHandler
	public void onPlayerClickInventory(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			for (int i = 0;i<creators.size();i++) {
				if (creators.get(i).equalsIgnoreCase(player.getDisplayName())) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (!creators.contains(event.getPlayer().getDisplayName())) {
			return;
		}

		for (int i = 0;i<naming.size();i++) {
			if (naming.get(i).equalsIgnoreCase(event.getPlayer().getDisplayName())) {
				setMapName(event.getPlayer(), event.getMessage());
				naming.remove(i);
				event.getPlayer().sendMessage(ChatColor.GREEN + "Map name set to: " + event.getMessage());
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!creators.contains(event.getPlayer().getDisplayName())) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
			Player player = event.getPlayer();
			if (settingflags.contains(event.getPlayer().getDisplayName())) {
				MapSettings settings = new MapSettings();

				ArrayList<String> players = new ArrayList<String>(containers.keySet());
				for (int i = 0;i<players.size();i++) {
					if (players.get(i).equalsIgnoreCase(player.getDisplayName())) {
						settings = containers.get(player.getDisplayName());
					}
				}

				if (settings.CTF_BLUE_FLAG != null && settings.CTF_RED_FLAG != null) {
					settingflags.remove(player.getDisplayName());
					player.sendMessage(ChatColor.GREEN + "Flags set!");
				} else {
					if (settings.CTF_RED_FLAG == null) {
						player.sendMessage(ChatColor.GREEN + "Red flag set!");
						settings.CTF_RED_FLAG = locationToString(event.getPlayer().getLocation());
						settings.GAMEMODE = "capture_the_flag";
						containers.put(player.getDisplayName(), settings);
						event.setCancelled(true);
					} 
				}
			}
		}

		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
			Player player = event.getPlayer();
			if (settingflags.contains(event.getPlayer().getDisplayName())) {
				MapSettings settings = new MapSettings();

				ArrayList<String> players = new ArrayList<String>(containers.keySet());
				for (int i = 0;i<players.size();i++) {
					if (players.get(i).equalsIgnoreCase(player.getDisplayName())) {
						settings = containers.get(player.getDisplayName());
					}
				}

				if (settings.CTF_BLUE_FLAG != null && settings.CTF_RED_FLAG != null) {
					settingflags.remove(player.getDisplayName());
					player.sendMessage(ChatColor.GREEN + "Flags set!");
				} else {
					if (settings.CTF_BLUE_FLAG == null) {
						player.sendMessage(ChatColor.GREEN + "Blue flag set!");
						settings.CTF_BLUE_FLAG = locationToString(event.getPlayer().getLocation());
						settings.GAMEMODE = "capture_the_flag";
						containers.put(player.getDisplayName(), settings);
						event.setCancelled(true);
					} 
				}
			}
		}

		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS) {
				if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Quick Teleport")) {
					Set<Material> set = null;
					try {
						Block block = event.getPlayer().getTargetBlock(set, 100);
						Location loc = block.getLocation();
						if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
							loc.setY(loc.getY() - 1);
						} else {
							loc.setY(loc.getY() + 1);
						}
						event.getPlayer().teleport(loc);
						event.setCancelled(true);
					} catch (Exception e) {
					}
				}
			}
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.NAME_TAG && event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Set Map Name")) {
				Player plr = event.getPlayer();
				plr.sendMessage(ChatColor.GREEN + "Please type the name of the map");
				naming.add(plr.getDisplayName());
				event.setCancelled(true);
			} else if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.WHITE + "Set Lobby Spawn")) {
				setLobbySpawn(event.getPlayer(), event.getPlayer().getLocation());
				event.getPlayer().sendMessage(ChatColor.GREEN + "Lobby Spawn Set!");
				event.setCancelled(true);
			} else if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Set Red Team Spawn")) {
				setRedTeamSpawn(event.getPlayer(), event.getPlayer().getLocation());
				event.getPlayer().sendMessage(ChatColor.GREEN + "Red Team Spawn Set!");
				event.setCancelled(true);
			} else if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.BLUE + "Set Blue Team Spawn")) {
				setBlueTeamSpawn(event.getPlayer(), event.getPlayer().getLocation());
				event.getPlayer().sendMessage(ChatColor.GREEN + "Blue Team Spawn Set!");
				event.setCancelled(true);
			} else if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.LIGHT_PURPLE + "Capture the Flag")) {
				startFlagSetProcess(event.getPlayer());
				event.setCancelled(true);
			} else if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.LIGHT_PURPLE + "King of the Hill")) {
				//Not ready
			} else if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GOLD + "Save Map")) {
				saveMap(event.getPlayer());
				unregisterCreator(event.getPlayer());
				event.setCancelled(true);
			}
		}
	}

	public void startFlagSetProcess(Player player) {
		if (settingflags.contains(player.getDisplayName())) {
		} else {
			player.sendMessage(ChatColor.GREEN + "Right Click to set the red team flag\nLeft Click to set the blue team flag!");
			settingflags.add(player.getDisplayName());
		}
	}

	public String locationToString(Location loc) {
		String world = loc.getWorld().getName();
		String x = String.valueOf(loc.getX());
		String y = String.valueOf(loc.getY());
		String z = String.valueOf(loc.getZ());
		String yaw = String.valueOf(loc.getYaw());
		String pitch = String.valueOf(loc.getPitch());
		return world+","+x+","+y+","+z+","+yaw+","+pitch;
	}

	public void saveMap(Player player) {
		MapSettings settings = new MapSettings();

		ArrayList<String> players = new ArrayList<String>(containers.keySet());
		for (int i = 0;i<players.size();i++) {
			if (players.get(i).equalsIgnoreCase(player.getDisplayName())) {
				settings = containers.get(player.getDisplayName());
			}
		}

		for (int j = 0;j<paintball.games.size();j++) {
			if (paintball.games.get(j).map_name.equalsIgnoreCase(settings.MAPNAME)) {
				player.sendMessage(ChatColor.RED + "Please rename your map because that name has already been used!");
				return;
			}
		}

		for (int i = 0;i<players.size();i++) {
			if (players.get(i).equalsIgnoreCase(player.getDisplayName())) {
				MapSettings container = containers.get(player.getDisplayName());

				container.WEATHER = "clear";
				paintball.mapConfig.saveMap(container);

				player.sendMessage(ChatColor.GREEN + "Map saved!");
				PaintballGame pbgame = new PaintballGame(container.MAPNAME, container.GAMEMODE, container.WEATHER, container.LOBBY_SPAWN, container.BLUE_TEAM_SPAWN, container.RED_TEAM_SPAWN, container.CTF_RED_FLAG, container.CTF_BLUE_FLAG, container.DOMINATION_POINT);
				SpigotPaintball.getInstance().getServer().getPluginManager().registerEvents(pbgame, SpigotPaintball.getInstance());
				paintball.games.add(pbgame);
				return;
			}
		}
	}

	public void setMapName(Player player, String name) {
		MapSettings settings = new MapSettings();

		ArrayList<String> players = new ArrayList<String>(containers.keySet());
		for (int i = 0;i<players.size();i++) {
			if (players.get(i).equalsIgnoreCase(player.getDisplayName())) {
				settings = containers.get(player.getDisplayName());
			}
		}

		settings.MAPNAME = name;
		containers.put(player.getDisplayName(), settings);
	}

	public void setLobbySpawn(Player player, Location loc) {
		MapSettings settings = new MapSettings();

		ArrayList<String> players = new ArrayList<String>(containers.keySet());
		for (int i = 0;i<players.size();i++) {
			if (players.get(i).equalsIgnoreCase(player.getDisplayName())) {
				settings = containers.get(player.getDisplayName());
			}
		}

		settings.LOBBY_SPAWN = locationToString(loc);
		containers.put(player.getDisplayName(), settings);
	}

	public void setRedTeamSpawn(Player player, Location loc) {
		MapSettings settings = new MapSettings();

		ArrayList<String> players = new ArrayList<String>(containers.keySet());
		for (int i = 0;i<players.size();i++) {
			if (players.get(i).equalsIgnoreCase(player.getDisplayName())) {
				settings = containers.get(player.getDisplayName());
			}
		}

		settings.RED_TEAM_SPAWN = locationToString(loc);
		containers.put(player.getDisplayName(), settings);
	}

	public void setBlueTeamSpawn(Player player, Location loc) {
		MapSettings settings = new MapSettings();

		ArrayList<String> players = new ArrayList<String>(containers.keySet());
		for (int i = 0;i<players.size();i++) {
			if (players.get(i).equalsIgnoreCase(player.getDisplayName())) {
				settings = containers.get(player.getDisplayName());
			}
		}

		settings.BLUE_TEAM_SPAWN = locationToString(loc);
		containers.put(player.getDisplayName(), settings);
	}
}
