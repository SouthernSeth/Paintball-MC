package com.jordan.paintball;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import mysqlwrapper.mysql.MySQL;
import mysqlwrapper.sqlite.SQLite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotPaintball extends JavaPlugin {

	private static Plugin instance = null;

	public ArrayList<PaintballGame> games = new ArrayList<PaintballGame>();

	public MySQL sql;
	public SQLite sqlite;
	public Connection c;

	public String host, port, database, username, password;
	public boolean useSQLite = false;

	public PaintballMapCreator mapCreator = null;
	public PaintballMapConfig mapConfig = null;
	
	@Override
	public void onEnable() {
		instance = this;

		Bukkit.getPluginManager().registerEvents(new PaintballEventHandler(this), this);
		Bukkit.getPluginManager().registerEvents(mapCreator = new PaintballMapCreator(this), this);
		Bukkit.getPluginCommand("paintball").setExecutor(this);

		mapConfig = new PaintballMapConfig();

		getConfig().options().copyDefaults(true);
		saveDefaultConfig();

		useSQLite = getConfig().getBoolean("enable");
		host = getConfig().getString("host");
		port = getConfig().getString("port");
		username = getConfig().getString("username");
		password = getConfig().getString("password");
		database = getConfig().getString("database");

		ArrayList<String> worlds = (ArrayList<String>) getConfig().getStringList("worlds");

		for (int i = 0;i<worlds.size();i++) {
			WorldCreator wc = new WorldCreator(worlds.get(i));
			World world = Bukkit.createWorld(wc);
			getServer().getWorlds().add(world);
			getLogger().info("Loaded world " + world.getName());	
		}

		if (useSQLite) {
			sqlite = new SQLite(this, "paintball.db");
			try {
				c = sqlite.openConnection();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			getLogger().info("Using SQLite to store data!");
		} else {
			sql = new MySQL(this, host, port, database, username, password);
			try {
				c = sql.openConnection();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			getLogger().info("Using MySQL to store data!");
		}

		try {
			Statement statement = c.createStatement();
			statement.execute("CREATE TABLE IF NOT EXISTS players ( uuid VARCHAR(255), kills INTEGER(255), wins INTEGER(255), losses INTEGER(255) )");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (int i = 0;i<mapConfig.settings_list.size();i++) {
			MapSettings settings = mapConfig.settings_list.get(i);
			String MAPNAME = settings.MAPNAME;
			String GAMEMODE = settings.GAMEMODE;
			String WEATHER = settings.WEATHER;
			String REDTEAMSPAWN = settings.RED_TEAM_SPAWN;
			String BLUETEAMSPAWN = settings.BLUE_TEAM_SPAWN;
			String LOBBYSPAWN = settings.LOBBY_SPAWN;
			String CTFRED = settings.CTF_RED_FLAG;
			String CTFBLUE = settings.CTF_BLUE_FLAG;
			String DOMPOINT = settings.DOMINATION_POINT;
			PaintballGame pbgame = new PaintballGame(MAPNAME, GAMEMODE, WEATHER, LOBBYSPAWN, BLUETEAMSPAWN, REDTEAMSPAWN, CTFRED, CTFBLUE, DOMPOINT);
			getServer().getPluginManager().registerEvents(pbgame, this);
			games.add(pbgame);
			getLogger().info("Successfully registered the map: " + MAPNAME);
		}
	}

	@Override
	public void onDisable() {
		for (int i = 0;i<games.size();i++) {
			games.get(i).onDisableReset();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			return true;
		}

		Player commandSender = (Player) sender;

		if (label.equalsIgnoreCase("hub")) {
			for (int i = 0;i<games.size();i++) {
				PaintballGame game = games.get(i);
				for (int j = 0;j<game.players.size();j++) {
					if (game.players.get(j).equalsIgnoreCase(commandSender.getUniqueId().toString())) {
						game.unregisterPlayer(commandSender);
					}
				}
			}
		} else if (label.equalsIgnoreCase("paintball")) {
			if (args.length == 0) {
				showCommandMenu(commandSender);
				return true;
			}
			if (args[0].equalsIgnoreCase("help")) {
				showCommandMenu(commandSender);
			} else if (args[0].equalsIgnoreCase("stats")) {
				commandSender.sendMessage("Not yet implemented");
			} else if (args[0].equalsIgnoreCase("teleport")) {
				if (args.length == 1) {
					commandSender.sendMessage(ChatColor.RED + "You cannot leave the world teleport name blank!");
					commandSender.sendMessage(ChatColor.GREEN + "Available world to teleport to:");
					for(int i = 0;i<getServer().getWorlds().size();i++) {
						commandSender.sendMessage(ChatColor.GREEN + "- " + getServer().getWorlds().get(i).getName());
					}
					return true;
				} else {
					for(int i = 0;i<getServer().getWorlds().size();i++) {
						if (args[1].equalsIgnoreCase(getServer().getWorlds().get(i).getName())) {
							commandSender.teleport(getServer().getWorlds().get(i).getSpawnLocation());
							return true;
						}
					}
					commandSender.sendMessage(ChatColor.RED + "That world doesn't exist!");
					commandSender.sendMessage(ChatColor.GREEN + "Available world to teleport to:");
					for(int i = 0;i<getServer().getWorlds().size();i++) {
						commandSender.sendMessage(ChatColor.GREEN + "- " + getServer().getWorlds().get(i).getName());
					}
					return true;
				}
			} else if (args[0].equalsIgnoreCase("create")) {
				if (mapCreator.registerCreator(commandSender)) {
					commandSender.sendMessage(ChatColor.GREEN + "You are now in map creation mode!");
				} else {
					mapCreator.unregisterCreator(commandSender);
					commandSender.sendMessage(ChatColor.RED + "You are no longer in map creation mode!");
				}
			} else {
				showCommandMenu(commandSender);
			}
		} else {
			showCommandMenu(commandSender);
		}
		return false;
	}

	public void showCommandMenu(Player commandSender) {
		commandSender.sendMessage(ChatColor.DARK_RED + "==================================");
		commandSender.sendMessage(ChatColor.RED +      "SpigotPaintball Commands");
		commandSender.sendMessage(ChatColor.DARK_RED + "==================================");
		commandSender.sendMessage(ChatColor.GREEN +    "/paintball help (Shows you this menu)");
		commandSender.sendMessage(ChatColor.GREEN +    "/paintball stats (Shows you your stats)");
		commandSender.sendMessage(ChatColor.GREEN +    "/paintball create (Shows you the steps to create a map)");
		commandSender.sendMessage(ChatColor.GREEN +    "/paintball teleport (Multiworld teleporting)");
		commandSender.sendMessage(ChatColor.DARK_RED + "==================================");

	}

	public static Plugin getInstance() {
		return instance;
	}

}
