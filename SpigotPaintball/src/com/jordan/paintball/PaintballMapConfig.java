package com.jordan.paintball;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PaintballMapConfig {

	FileConfiguration fileConfig;
	File file = null;

	ArrayList<MapSettings> settings_list = new ArrayList<MapSettings>();

	public PaintballMapConfig() {
		fileConfig = new YamlConfiguration();
		file = new File(SpigotPaintball.getInstance().getDataFolder(), "mapconfig.yml");

		if (!file.exists()) {
			try {
				file.createNewFile();
				fileConfig.load(file);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				fileConfig.load(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}

			if (!fileConfig.contains("maps"))
			{
				System.out.println("[SpigotPaintball] There are no maps to load.");
				return;
			}

			ArrayList<String> maps = new ArrayList<String>(fileConfig.getConfigurationSection("maps").getKeys(false));

			for (int i = 0;i<maps.size();i++) {
				MapSettings mapSettings = new MapSettings();
				mapSettings.MAPNAME = maps.get(i);
				mapSettings.WEATHER = fileConfig.getString("maps." + mapSettings.MAPNAME + ".settings.weather");
				mapSettings.GAMEMODE = fileConfig.getString("maps." + mapSettings.MAPNAME + ".paintball.gamemode");
				mapSettings.LOBBY_SPAWN = fileConfig.getString("maps." + mapSettings.MAPNAME + ".paintball.lobby_spawn");
				mapSettings.RED_TEAM_SPAWN = fileConfig.getString("maps." + mapSettings.MAPNAME + ".paintball.red_team_spawn");
				mapSettings.BLUE_TEAM_SPAWN = fileConfig.getString("maps." + mapSettings.MAPNAME + ".paintball.blue_team_spawn");
				if (mapSettings.GAMEMODE.equalsIgnoreCase("capture_the_flag")) {
					mapSettings.CTF_BLUE_FLAG = fileConfig.getString("maps." + mapSettings.MAPNAME + ".capture_the_flag.blue_team_flag_location");
					mapSettings.CTF_RED_FLAG = fileConfig.getString("maps." + mapSettings.MAPNAME + ".capture_the_flag.red_team_flag_location");
				} else if (mapSettings.GAMEMODE.equalsIgnoreCase("king_of_the_hill")) {
					mapSettings.DOMINATION_POINT = fileConfig.getString("maps." + mapSettings.MAPNAME + ".king_of_the_hill.domination_point");
				} else if (mapSettings.GAMEMODE.equalsIgnoreCase("deathmatch")) {

				}
				settings_list.add(mapSettings);
			}
		}
	}

	public void saveMap(MapSettings settings) {
		fileConfig.set("maps." + settings.MAPNAME + ".settings.weather", settings.WEATHER);

		fileConfig.set("maps." + settings.MAPNAME + ".paintball.gamemode", settings.GAMEMODE);
		fileConfig.set("maps." + settings.MAPNAME + ".paintball.red_team_spawn", settings.BLUE_TEAM_SPAWN);
		fileConfig.set("maps." + settings.MAPNAME + ".paintball.blue_team_spawn", settings.RED_TEAM_SPAWN);
		fileConfig.set("maps." + settings.MAPNAME + ".paintball.lobby_spawn", settings.LOBBY_SPAWN);

		if (settings.GAMEMODE.equalsIgnoreCase("capture_the_flag")) {
			fileConfig.set("maps." + settings.MAPNAME + ".capture_the_flag.blue_team_flag_location", settings.CTF_BLUE_FLAG);
			fileConfig.set("maps." + settings.MAPNAME + ".capture_the_flag.red_team_flag_location", settings.CTF_RED_FLAG);
		} else if (settings.GAMEMODE.equalsIgnoreCase("king_of_the_hill")) {
			fileConfig.set("maps." + settings.MAPNAME + ".king_of_the_hill.domination_point", settings.DOMINATION_POINT);
		} else if (settings.GAMEMODE.equalsIgnoreCase("deathmatch")) {

		}

		save();
	}

	public void save() {
		try {
			fileConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
