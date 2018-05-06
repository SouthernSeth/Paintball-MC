package com.jordan.paintball;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PaintballEventHandler implements Listener {

	public SpigotPaintball plugin = null;

	public PaintballEventHandler(SpigotPaintball plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerPlaceSign(SignChangeEvent event) {
		if (event.getLine(0).equalsIgnoreCase("[Paintball]")) {
			if (event.getPlayer().hasPermission("paintball.createlobby") || event.getPlayer().isOp()) {
				if (event.getLine(1) != null) {
					for (int i = 0;i<plugin.games.size();i++) {
						if (plugin.games.get(i).map_name.equalsIgnoreCase(event.getLine(1))) {
							event.setLine(0, ChatColor.RED + "[Paintball]");
							event.setLine(1, ChatColor.GREEN + plugin.games.get(i).map_name);
							event.getPlayer().sendMessage(ChatColor.GREEN + "Lobby created!");
							return;
						}
					}
					event.getPlayer().sendMessage(ChatColor.RED + "Couldn't create the lobby!");
					event.setCancelled(true);
					return;
				}
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission for that!");
			}
		}
	}

	@EventHandler
	public void onPlayerRightClickSign(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(event.getClickedBlock().getState() instanceof Sign){
				Sign sign = (Sign) event.getClickedBlock().getState();
				if(sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "[Paintball]")){
					for (int i = 0;i<plugin.games.size();i++) {
						String mapname = ChatColor.GREEN + plugin.games.get(i).map_name;
						if (mapname.equalsIgnoreCase(sign.getLine(1))) {
							plugin.games.get(i).registerPlayer(player);
							event.setCancelled(true);
						}
					}
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		for (int i = 0;i<plugin.games.size();i++) {
			PaintballGame game = plugin.games.get(i);
			ArrayList<String> quit_players = new ArrayList<String>(game.quit_players.keySet());
			for (int j = 0;j<quit_players.size();j++) {
				if (quit_players.get(i).equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
					if (game.gameStarted) {
						game.resumePlayer(event.getPlayer());
						return;
					}
				}
			}
		}
		
		Player player = event.getPlayer();
		player.getInventory().clear();
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		player.getInventory().setChestplate(new ItemStack(Material.AIR));
		player.getInventory().setLeggings(new ItemStack(Material.AIR));
		player.getInventory().setBoots(new ItemStack(Material.AIR));
		player.teleport(Bukkit.getWorld("world").getSpawnLocation());
		player.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
		player.setGameMode(GameMode.ADVENTURE);
	}
	
}
