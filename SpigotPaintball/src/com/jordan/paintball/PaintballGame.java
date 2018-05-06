package com.jordan.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;

public class PaintballGame implements Listener {

	public String map_name = null;

	public HashMap<String, String> quit_players = new HashMap<String, String>();

	public ArrayList<String> players = new ArrayList<String>();

	public ArrayList<String> invincible = new ArrayList<String>();

	public ArrayList<String> strongarm = new ArrayList<String>();

	public ArrayList<String> blue_team = new ArrayList<String>();
	public ArrayList<String> red_team = new ArrayList<String>();

	public HashMap<String, Integer> kills = new HashMap<String, Integer>();

	public ArrayList<String> dead = new ArrayList<String>();

	public ArrayList<Location> placedBlocks = new ArrayList<Location>();

	public ArrayList<String> block_break_cooldown = new ArrayList<String>();

	public ArrayList<String> players_in_dom_area = new ArrayList<String>();

	public int redTeamPoints = 0;
	public int blueTeamPoints = 0;

	public Location blue_team_spawn = null;
	public Location red_team_spawn = null;
	public Location lobby_spawn = null;
	public String gamemode = null;
	public String weather = null;
	public Location ctf_red = null;
	public Location ctf_blue = null;
	public Location dom_point = null;

	public String blueFlagCarrier = "";
	public String redFlagCarrier = "";

	public Location redTeamFlagLocation = null;
	public Location blueTeamFlagLocation = null;

	public boolean blueFlagAtBase = true;
	public boolean redFlagAtBase = true;

	public boolean gameStarted = false;

	public int countdown = 300;
	public int timer_id = 0;

	public BukkitTask cooldown_block_timer = null;

	public int gameTimer = 300;
	public int gameTimerID = 0;

	public Scoreboard board = null;
	public Objective obj = null;
	public Team redTeamObj = null;
	public Team blueTeamObj = null;

	public PaintballGame(String map_name, String gamemode, String weather, String lobby_spawn, String blue_team_spawn, String red_team_spawn, String ctf_red, String ctf_blue, String dom_point) {
		this.map_name = map_name;
		this.gamemode = gamemode;
		this.weather = weather;
		this.lobby_spawn = Util.stringToLocation(lobby_spawn);
		this.red_team_spawn = Util.stringToLocation(red_team_spawn);
		this.blue_team_spawn = Util.stringToLocation(blue_team_spawn);

		if (gamemode.equalsIgnoreCase("capture_the_flag")) {
			this.ctf_red = Util.stringToLocation(ctf_red);
			this.ctf_blue = Util.stringToLocation(ctf_blue);
			redTeamFlagLocation = Util.stringToLocation(ctf_red);
			blueTeamFlagLocation = Util.stringToLocation(ctf_blue);
		} else if (gamemode.equalsIgnoreCase("king_of_the_hill")) {
			this.dom_point = Util.stringToLocation(dom_point);
		} else if (gamemode.equalsIgnoreCase("deathmatch")) {

		}

		ScoreboardManager manager = Bukkit.getServer().getScoreboardManager();
		board = manager.getNewScoreboard();
		obj = board.registerNewObjective("test", "dummy");
		redTeamObj = board.registerNewTeam("redteam");
		blueTeamObj = board.registerNewTeam("blueteam");
	}

	public void startLobbyTimer() {
		timer_id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SpigotPaintball.getInstance(), new Runnable() {
			@Override
			public void run() {
				countdown -= 1;
				if (countdown <= 10) {
					if (countdown == 0) {
						for (int i = 0;i<players.size();i++) {
							Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.GREEN + "Game starts in: Now!");
						}
					} else {
						for (int i = 0;i<players.size();i++) {
							Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.GREEN + "Game starts in: " + String.valueOf(countdown));
						}
					}
				}
				if (countdown <= 0) {
					startGame();
				}
			}
		}, 0L, 20L);
	}

	public void stopTimer() {
		if (Bukkit.getServer().getScheduler().isCurrentlyRunning(timer_id)) {
			Bukkit.getServer().getScheduler().cancelTask(timer_id);
		}
	}

	@EventHandler
	public void onPlayerThrowSnowball(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof Snowball) {
			Snowball snowball = (Snowball) event.getEntity();
			if (snowball.getShooter() instanceof Player) {
				Player shooter = (Player) snowball.getShooter();
				if (!players.contains(shooter.getUniqueId().toString())) {
					return;
				}

				if (strongarm.contains(shooter.getUniqueId().toString())) {
					snowball.setVelocity(snowball.getVelocity().multiply(2f));
				}
			}
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (!players.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		event.setQuitMessage(null);

		if (!gameStarted) {
			unregisterPlayer(event.getPlayer());
			if (players.size() < 10) {
				stopTimer();
				countdown = 300;
			}

			if (players.size() >= 12) {
				countdown = 30;
			}
			return;
		} else {
			if (getTeam(event.getPlayer()).equalsIgnoreCase("RED")) {
				red_team.remove(event.getPlayer().getUniqueId().toString());
				event.setQuitMessage(ChatColor.YELLOW + "Red team player " + ChatColor.RED + event.getPlayer().getDisplayName() + ChatColor.YELLOW + " left the game!");
			} else {
				blue_team.remove(event.getPlayer().getUniqueId().toString());
				event.setQuitMessage(ChatColor.YELLOW + "Blue team player " + ChatColor.BLUE + event.getPlayer().getDisplayName() + ChatColor.YELLOW + " left the game!");
			}

			quit_players.put(event.getPlayer().getUniqueId().toString(), getTeam(event.getPlayer()));
			unregisterPlayer(event.getPlayer());

			if (red_team.size() == 0 && blue_team.size() == 0) {
				reset();
				Bukkit.getServer().getScheduler().cancelTask(gameTimerID);
			}

			if (red_team.size() == 0) {
				for (int i = 0;i<players.size();i++) {
					Bukkit.getPlayer(UUID.fromString(players.get(i))).getInventory().clear();
					Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.BLUE + "Blue teams wins!");
					gameStarted = false;
				}

				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SpigotPaintball.getInstance(), new Runnable() {
					@Override
					public void run() {
						reset();
						Bukkit.getServer().getScheduler().cancelTask(gameTimerID);
					}
				}, 100);
			}

			if (blue_team.size() == 0) {
				for (int i = 0;i<players.size();i++) {
					Bukkit.getPlayer(UUID.fromString(players.get(i))).getInventory().clear();
					Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "Red teams wins!");
					gameStarted = false;
				}

				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SpigotPaintball.getInstance(), new Runnable() {
					@Override
					public void run() {
						reset();
						Bukkit.getServer().getScheduler().cancelTask(gameTimerID);
					}
				}, 100);
			}
		}
	}

	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		if (!players.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		if (gamemode.equalsIgnoreCase("capture_the_flag")) {
			if (dead.contains(event.getPlayer().getUniqueId().toString())) {
				//Do nothing because the player is dead
			} else {
				if (event.getPlayer().getLocation().getBlock().getState() instanceof Banner) {
					if (redTeamFlagLocation.getBlockX() == event.getPlayer().getLocation().getBlock().getLocation().getBlockX()) {
						if (redTeamFlagLocation.getBlockY() == event.getPlayer().getLocation().getBlock().getLocation().getBlockY()) {
							if (redTeamFlagLocation.getBlockZ() == event.getPlayer().getLocation().getBlock().getLocation().getBlockZ()) {
								if (getTeam(event.getPlayer()).equalsIgnoreCase("BLUE")) {
									if (redFlagCarrier.equalsIgnoreCase("")) {
										redFlagCarrier = event.getPlayer().getUniqueId().toString();
										redFlagAtBase = false;
										event.getPlayer().getLocation().getBlock().setType(Material.AIR);
										for (int i = 0;i<players.size();i++) {
											Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Blue team player " + event.getPlayer().getDisplayName() + " took the red team's flag!");
										}
									}
								} else {
									if (redTeamFlagLocation.getBlockX() != ctf_red.getBlockX()) {
										if (redTeamFlagLocation.getBlockY() != ctf_red.getBlockY()) {
											if (redTeamFlagLocation.getBlockZ() != ctf_red.getBlockZ()) {
												redFlagCarrier = "";
												redTeamFlagLocation.getBlock().setType(Material.AIR);
												redTeamFlagLocation = ctf_red;
												Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
													@Override
													public void run() {
														redTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
														Banner banner = (Banner) redTeamFlagLocation.getBlock().getState();
														banner.setBaseColor(DyeColor.RED);
														banner.update(true);
													} 
												});
												redFlagAtBase = true;
												for (int i = 0;i<players.size();i++) {
													Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Red team player " + event.getPlayer().getDisplayName() + " returned the red team's flag!");
												}
											}
										}
									}
								}
							}
						}
					}

					if (blueTeamFlagLocation.getBlockX() == event.getPlayer().getLocation().getBlock().getLocation().getBlockX()) {
						if (blueTeamFlagLocation.getBlockY() == event.getPlayer().getLocation().getBlock().getLocation().getBlockY()) {
							if (blueTeamFlagLocation.getBlockZ() == event.getPlayer().getLocation().getBlock().getLocation().getBlockZ()) {
								if (getTeam(event.getPlayer()).equalsIgnoreCase("RED")) {
									if (blueFlagCarrier.equalsIgnoreCase("")) {
										blueFlagCarrier = event.getPlayer().getUniqueId().toString();
										blueFlagAtBase = false;
										event.getPlayer().getLocation().getBlock().setType(Material.AIR);
										for (int i = 0;i<players.size();i++) {
											Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Red team player " + event.getPlayer().getDisplayName() + " took the blue team's flag!");
										}
									}
								} else {
									if (blueTeamFlagLocation.getBlockX() != ctf_blue.getBlockX()) {
										if (blueTeamFlagLocation.getBlockY() != ctf_blue.getBlockY()) {
											if (blueTeamFlagLocation.getBlockZ() != ctf_blue.getBlockZ()) {
												blueFlagCarrier = "";
												blueTeamFlagLocation.getBlock().setType(Material.AIR);
												blueTeamFlagLocation = ctf_blue;
												Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
													@Override
													public void run() {
														blueTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
														Banner banner = (Banner) blueTeamFlagLocation.getBlock().getState();
														banner.setBaseColor(DyeColor.BLUE);
														banner.update(true);
													} 
												});
												blueFlagAtBase = true;
												for (int i = 0;i<players.size();i++) {
													Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Blue team player " + event.getPlayer().getDisplayName() + " returned the blue team's flag!");
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (gamemode.equalsIgnoreCase("capture_the_flag")) {
			if (dead.contains(event.getPlayer().getUniqueId().toString())) {
				//Do nothing because the player is dead
			} else {
				if (redFlagCarrier.equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
					if (ctf_blue.getBlockX() == event.getPlayer().getLocation().getBlockX()) {
						if (ctf_blue.getBlockY() == event.getPlayer().getLocation().getBlockY()) {
							if (ctf_blue.getBlockZ() == event.getPlayer().getLocation().getBlockZ()) {
								if (blueFlagAtBase) {
									for (int i = 0;i<players.size();i++) {
										Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Blue team captured the red team's flag!");
									}
									blueTeamPoints += 1;
									revalidateGameScoreboard();
									redTeamFlagLocation = ctf_red;
									redFlagCarrier = "";
									redFlagAtBase = true;
									Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
										@Override
										public void run() {
											redTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
											Banner banner = (Banner) redTeamFlagLocation.getBlock().getState();
											banner.setBaseColor(DyeColor.RED);
											banner.update(true);
										} 
									});
								} else {
									event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Your flag must be returned before you can capture the red team's flag!");
								}
							}
						}
					}
				} else if (blueFlagCarrier.equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
					if (ctf_red.getBlockX() == event.getPlayer().getLocation().getBlockX()) {
						if (ctf_red.getBlockY() == event.getPlayer().getLocation().getBlockY()) {
							if (ctf_red.getBlockZ() == event.getPlayer().getLocation().getBlockZ()) {
								if (redFlagAtBase) {
									for (int i = 0;i<players.size();i++) {
										Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Red team captured the blue team's flag!");
									}
									redTeamPoints += 1;
									revalidateGameScoreboard();
									blueTeamFlagLocation = ctf_blue;
									blueFlagCarrier = "";
									blueFlagAtBase = true;
									Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
										@Override
										public void run() {
											blueTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
											Banner banner = (Banner) blueTeamFlagLocation.getBlock().getState();
											banner.setBaseColor(DyeColor.BLUE);
											banner.update(true);
										} 
									});
								} else {
									event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Your flag must be returned before you can capture the red team's flag!");
								}
							}
						}
					}
				}
			}
		} else if (gamemode.equalsIgnoreCase("king_of_the_hill")) {
			if (Util.getDistance(dom_point, event.getPlayer().getLocation()) <= 10) {
				if (!players_in_dom_area.contains(event.getPlayer().getUniqueId().toString())) {
					players_in_dom_area.add(event.getPlayer().getUniqueId().toString());
					event.getPlayer().sendMessage("You are within 10 blocks of the dom point");
				}
			} else {
				if (players_in_dom_area.contains(event.getPlayer().getUniqueId().toString())) {
					players_in_dom_area.remove(event.getPlayer().getUniqueId().toString());
					event.getPlayer().sendMessage("You are not within 10 blocks of the dom point");
				}
			}
		} else {
			//Deathmatch stuff
		}
	}

	@EventHandler
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		if (!players.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		for (int i = 0;i<placedBlocks.size();i++) {
			Location loc = placedBlocks.get(i);
			if (loc.getBlockX() == event.getBlock().getLocation().getBlockX()) {
				if (loc.getBlockY() == event.getBlock().getLocation().getBlockY()) {
					if (loc.getBlockZ() == event.getBlock().getLocation().getBlockZ()) {
						return;
					}
				}
			}
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		if (!players.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		placedBlocks.add(event.getBlock().getLocation());
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (players.contains(p.getUniqueId().toString())) {
				if (event.getCause() == DamageCause.FALL){
					event.setCancelled(true);
				} else if (event.getCause() == DamageCause.VOID) {
					if (!gameStarted) {
						p.teleport(lobby_spawn);
					}
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerHitWithPaintball(EntityDamageByEntityEvent event) {
		if (gameStarted) {
			if (event.getDamager() instanceof Snowball) {
				Snowball projectile = (Snowball) event.getDamager();
				if (event.getEntity() instanceof Player) {
					Player victim = (Player) event.getEntity();
					if (projectile.getShooter() instanceof Player) {
						Player shooter = (Player) projectile.getShooter();

						if (!players.contains(shooter.getUniqueId().toString())) {
							return;
						}

						if (!players.contains(victim.getUniqueId().toString())) {
							return;
						}

						if (getTeam(victim).equalsIgnoreCase(getTeam(shooter))) {
							event.setCancelled(true);
							return;
						} else {
							if (invincible.contains(victim.getUniqueId().toString())) {
								event.setCancelled(true);
								shooter.sendMessage(ChatColor.GOLD + "This player has spawn protection on!");
								return;
							}

							if (invincible.contains(shooter.getUniqueId().toString())) {
								event.setCancelled(true);
								shooter.sendMessage(ChatColor.GOLD + "You have spawn protection on and cannot frag another player yet!");
								return;
							}

							dead.add(victim.getUniqueId().toString());

							event.setCancelled(true);

							shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
							victim.playEffect(EntityEffect.HURT);

							if (gamemode.equalsIgnoreCase("king_of_the_hill")) {

							} else if (gamemode.equalsIgnoreCase("capture_the_flag")) {
								if (redFlagCarrier.equalsIgnoreCase(victim.getUniqueId().toString())) {
									redTeamFlagLocation = victim.getLocation();
									while (redTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR || redTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WATER) {
										redTeamFlagLocation.setY(redTeamFlagLocation.getY() - 1);
									}
									Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
										@Override
										public void run() {
											redTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
											Banner banner = (Banner) redTeamFlagLocation.getBlock().getState();
											banner.setBaseColor(DyeColor.RED);
											banner.update(true);
										} 
									});
									for (int i = 0;i<players.size();i++) {
										Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Blue team player " + victim.getDisplayName() + " dropped the red team's flag!");
									}
									redFlagCarrier = "";
								} else if (blueFlagCarrier.equalsIgnoreCase(victim.getUniqueId().toString())) {
									blueTeamFlagLocation = victim.getLocation();
									while (blueTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR || blueTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WATER) {
										blueTeamFlagLocation.setY(blueTeamFlagLocation.getY() - 1);
									}
									Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
										@Override
										public void run() {
											blueTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
											Banner banner = (Banner) blueTeamFlagLocation.getBlock().getState();
											banner.setBaseColor(DyeColor.BLUE);
											banner.update(true);
										} 
									});
									for (int i = 0;i<players.size();i++) {
										Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Red team player " + victim.getDisplayName() + " dropped the blue team's flag!");
									}
									blueFlagCarrier = "";
								}
							} else {
								if (getTeam(shooter).equalsIgnoreCase("RED")) {
									redTeamPoints += 1;
								} else if (getTeam(shooter).equalsIgnoreCase("BLUE")) {
									blueTeamPoints += 1;
								}
							}

							if (kills.containsKey(shooter.getUniqueId().toString())) {
								int num = kills.get(shooter.getUniqueId().toString()) + 1;
								kills.put(shooter.getUniqueId().toString(), num);
							} else {
								kills.put(shooter.getUniqueId().toString(), 1);
							}

							if (getTeam(victim).equalsIgnoreCase("RED")) {
								Firework firework = (Firework) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
								FireworkMeta meta = firework.getFireworkMeta();
								meta.addEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.WHITE).withFade(Color.RED).build());
								meta.setPower(0);
								firework.setFireworkMeta(meta);
								new BukkitRunnable() {
									@Override
									public void run() {
										firework.detonate();
									}
								}.runTaskLater(SpigotPaintball.getInstance(), 2L);
							} else {
								Firework firework = (Firework) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
								FireworkMeta meta = firework.getFireworkMeta();
								meta.addEffect(FireworkEffect.builder().withColor(Color.BLUE).withColor(Color.WHITE).withFade(Color.BLUE).build());
								meta.setPower(0);
								firework.setFireworkMeta(meta);
								new BukkitRunnable() {
									@Override
									public void run() {
										firework.detonate();
									}
								}.runTaskLater(SpigotPaintball.getInstance(), 2L);
							}

							giveKillStreakSelector(shooter);
							revalidateGameScoreboard();

							for(int i = 0;i<players.size();i++) {
								Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + shooter.getDisplayName() + ChatColor.GOLD + " fragged " + ChatColor.RED + victim.getDisplayName());
							}

							respawn(victim);
							return;
						}
					}
				}
			} else if (event.getDamager() instanceof Player) {
				Player shooter = (Player) event.getDamager();
				if (event.getEntity() instanceof Player) {
					Player victim = (Player) event.getEntity();
					if (!players.contains(shooter.getUniqueId().toString())) {
						return;
					}

					if (!players.contains(victim.getUniqueId().toString())) {
						return;
					}

					if (getTeam(victim).equalsIgnoreCase(getTeam(shooter))) {
						event.setCancelled(true);
						return;
					} else {
						if (invincible.contains(victim.getUniqueId().toString())) {
							event.setCancelled(true);
							shooter.sendMessage(ChatColor.GOLD + "This player has spawn protection on!");
							return;
						}

						if (invincible.contains(shooter.getUniqueId().toString())) {
							event.setCancelled(true);
							shooter.sendMessage(ChatColor.GOLD + "You have spawn protection on and cannot frag another player yet!");
							return;
						}

						dead.add(victim.getUniqueId().toString());
						event.setCancelled(true);
						shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
						victim.playEffect(EntityEffect.HURT);

						if (gamemode.equalsIgnoreCase("king_of_the_hill")) {

						} else if (gamemode.equalsIgnoreCase("capture_the_flag")) {
							if (redFlagCarrier.equalsIgnoreCase(victim.getUniqueId().toString())) {
								redTeamFlagLocation = victim.getLocation();
								while (redTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR || redTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WATER) {
									redTeamFlagLocation.setY(redTeamFlagLocation.getY() - 1);
								}
								Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
									@Override
									public void run() {
										redTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
										Banner banner = (Banner) redTeamFlagLocation.getBlock().getState();
										banner.setBaseColor(DyeColor.RED);
										banner.update(true);
									} 
								});
								for (int i = 0;i<players.size();i++) {
									Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Blue team player " + victim.getDisplayName() + " dropped the red team's flag!");
								}
								redFlagCarrier = "";
							} else if (blueFlagCarrier.equalsIgnoreCase(victim.getUniqueId().toString())) {
								blueTeamFlagLocation = victim.getLocation();
								while (blueTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR || blueTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WATER) {
									blueTeamFlagLocation.setY(blueTeamFlagLocation.getY() - 1);
								}
								Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
									@Override
									public void run() {
										blueTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
										Banner banner = (Banner) blueTeamFlagLocation.getBlock().getState();
										banner.setBaseColor(DyeColor.BLUE);
										banner.update(true);
									} 
								});
								for (int i = 0;i<players.size();i++) {
									Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Red team player " + victim.getDisplayName() + " dropped the blue team's flag!");
								}
								blueFlagCarrier = "";
							}
						} else {
							if (getTeam(shooter).equalsIgnoreCase("RED")) {
								redTeamPoints += 1;
							} else if (getTeam(shooter).equalsIgnoreCase("BLUE")) {
								blueTeamPoints += 1;
							}
						}

						if (kills.containsKey(shooter.getUniqueId().toString())) {
							int num = kills.get(shooter.getUniqueId().toString()) + 1;
							kills.put(shooter.getUniqueId().toString(), num);
						} else {
							kills.put(shooter.getUniqueId().toString(), 1);
						}

						if (getTeam(victim).equalsIgnoreCase("RED")) {
							Firework firework = (Firework) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
							FireworkMeta meta = firework.getFireworkMeta();
							meta.addEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.WHITE).withFade(Color.RED).build());
							meta.setPower(0);
							firework.setFireworkMeta(meta);
							new BukkitRunnable() {
								@Override
								public void run() {
									firework.detonate();
								}
							}.runTaskLater(SpigotPaintball.getInstance(), 2L);
						} else {
							Firework firework = (Firework) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
							FireworkMeta meta = firework.getFireworkMeta();
							meta.addEffect(FireworkEffect.builder().withColor(Color.BLUE).withColor(Color.WHITE).withFade(Color.BLUE).build());
							meta.setPower(0);
							firework.setFireworkMeta(meta);
							new BukkitRunnable() {
								@Override
								public void run() {
									firework.detonate();
								}
							}.runTaskLater(SpigotPaintball.getInstance(), 2L);
						}

						giveKillStreakSelector(shooter);
						revalidateGameScoreboard();

						for(int i = 0;i<players.size();i++) {
							Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + shooter.getDisplayName() + ChatColor.GOLD + " melee'd " + ChatColor.RED + victim.getDisplayName());
						}

						respawn(victim);
						return;
					}
				}
			} else {
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		if (!players.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		for (int i = 0;i<players.size();i++) {
			if (getTeam(event.getPlayer()).equalsIgnoreCase("RED") && gameStarted) {
				Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "[RED] " + ChatColor.GRAY + event.getPlayer().getDisplayName() + " > " + ChatColor.WHITE + event.getMessage());
			} else if (getTeam(event.getPlayer()).equalsIgnoreCase("BLUE") && gameStarted) {
				Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.BLUE + "[BLUE] " + ChatColor.GRAY + event.getPlayer().getDisplayName() + " > " + ChatColor.WHITE + event.getMessage());
			} else {
				Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.GRAY + event.getPlayer().getDisplayName() + " > " + ChatColor.WHITE + event.getMessage());
			}
		}

		if (event.getMessage().equalsIgnoreCase("forcestartgame")) { //TODO: Remove after release!!!
			startGame();
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (!players.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerHungerChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();

			if (!players.contains(player.getUniqueId().toString())) {
				return;
			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerRightClickTeamSelectorTool(PlayerInteractEvent event) {
		if (!players.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.BOOK) {
			if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Team Selector")) {
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					showTeamSelectorMenu(event.getPlayer());
					event.setCancelled(true);
				}
			}
		} else if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND) {
			if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "Killstreaks")) {
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					showKillstreakShop(event.getPlayer());
					event.setCancelled(true);
				}
			}
		}

		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			Block bl = event.getClickedBlock();
			for (Location loc : placedBlocks) {
				if (bl.getLocation().getBlockX() == loc.getBlockX() && bl.getLocation().getBlockY() == loc.getBlockY() && bl.getLocation().getBlockZ() == loc.getBlockZ()) {
					if (block_break_cooldown.contains(event.getPlayer().getUniqueId().toString())) {
						return;
					}

					loc.getBlock().setType(Material.AIR);
					block_break_cooldown.add(event.getPlayer().getUniqueId().toString());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerClickItem(InventoryClickEvent event) {
		Player plr = (Player) event.getWhoClicked();

		if (!players.contains(plr.getUniqueId().toString())) {
			return;
		}

		if (event.getInventory().getTitle().equalsIgnoreCase(ChatColor.BLACK + "Select Your Team!")) {
			if (event.getSlot() == 24) {
				for (int i = 0;i<red_team.size();i++) {
					if (red_team.get(i).equalsIgnoreCase(plr.getUniqueId().toString())) {
						red_team.remove(plr.getUniqueId().toString());
					}
				}
				blue_team.add(plr.getUniqueId().toString());
				plr.sendMessage(ChatColor.GREEN + "You are now on the blue team!");
				plr.closeInventory();
				event.setCancelled(true);
			} else if (event.getSlot() == 20) {
				for (int i = 0;i<blue_team.size();i++) {
					if (blue_team.get(i).equalsIgnoreCase(plr.getUniqueId().toString())) {
						blue_team.remove(plr.getUniqueId().toString());
					}
				}
				red_team.add(plr.getUniqueId().toString());
				plr.sendMessage(ChatColor.GREEN + "You are now on the red team!");
				plr.closeInventory();
				event.setCancelled(true);
			}
		}

		if (event.getInventory().getTitle().equalsIgnoreCase(ChatColor.BLACK + "Killstreaks") && gameStarted) {
			if (event.getSlot() == 0) {
				if (kills.containsKey(plr.getUniqueId().toString())) {
					int killcount = kills.get(plr.getUniqueId().toString());
					if (killcount >= 5) {
						killcount -= 5;
						kills.put(plr.getUniqueId().toString(), killcount);
						plr.sendMessage(ChatColor.GREEN + "You have activated " + ChatColor.GOLD + "Strong Arm" + ChatColor.GREEN + " for " + ChatColor.GOLD + "30s" + ChatColor.GREEN + "!");
						strongarm.add(plr.getUniqueId().toString());
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SpigotPaintball.getInstance(), new Runnable() {
							@Override
							public void run() {
								strongarm.remove(plr.getUniqueId().toString());
								plr.sendMessage(ChatColor.GOLD + "Strong Arm " + ChatColor.GREEN + " has worn off!");
							}
						}, 600L);
						plr.closeInventory();
						giveKillStreakSelector(plr);
					} else {
						event.setCancelled(true);
					}
				}
			} else if (event.getSlot() == 1) {
				if (kills.containsKey(plr.getUniqueId().toString())) {
					int killcount = kills.get(plr.getUniqueId().toString());
					if (killcount >= 5) {
						killcount -= 5;
						kills.put(plr.getUniqueId().toString(), killcount);
						if (getTeam(plr).equalsIgnoreCase("RED")) {
							for (int i = 0;i<red_team.size();i++) {
								if (red_team.get(i).equalsIgnoreCase(plr.getUniqueId().toString())) {
									Bukkit.getPlayer(UUID.fromString(red_team.get(i))).getInventory().addItem(new ItemStack(Material.SNOW_BALL, 32));
									Bukkit.getPlayer(UUID.fromString(red_team.get(i))).sendMessage(ChatColor.GREEN + "You activated the killstreak " + ChatColor.GOLD + "Team Ammo!");
									plr.closeInventory();
									giveKillStreakSelector(plr);
								} else {
									Bukkit.getPlayer(UUID.fromString(red_team.get(i))).getInventory().addItem(new ItemStack(Material.SNOW_BALL, 32));
									Bukkit.getPlayer(UUID.fromString(red_team.get(i))).sendMessage(ChatColor.GREEN + "Your team member " + ChatColor.GOLD + plr.getDisplayName() + ChatColor.GREEN + " activated " + ChatColor.GOLD + "Team Ammo!");
								}
							}
							event.setCancelled(true);
						} else {
							for (int i = 0;i<blue_team.size();i++) {
								if (blue_team.get(i).equalsIgnoreCase(plr.getUniqueId().toString())) {
									Bukkit.getPlayer(UUID.fromString(blue_team.get(i))).getInventory().addItem(new ItemStack(Material.SNOW_BALL, 32));
									Bukkit.getPlayer(UUID.fromString(blue_team.get(i))).sendMessage(ChatColor.GREEN + "You activated the killstreak team ammo!");
									plr.closeInventory();
									giveKillStreakSelector(plr);
								} else {
									Bukkit.getPlayer(UUID.fromString(blue_team.get(i))).getInventory().addItem(new ItemStack(Material.SNOW_BALL, 32));
									Bukkit.getPlayer(UUID.fromString(blue_team.get(i))).sendMessage(ChatColor.GREEN + "Your team member " + ChatColor.GOLD + plr.getDisplayName() + ChatColor.GREEN + " activated team ammo!");
								}
							}
							event.setCancelled(true);
						}
					} else {
						event.setCancelled(true);
					}
				}
			} else if (event.getSlot() == 2) {
				if (kills.containsKey(plr.getUniqueId().toString())) {
					int killcount = kills.get(plr.getUniqueId().toString());
					if (killcount >= 2) {
						killcount -= 2;
						kills.put(plr.getUniqueId().toString(), killcount);
						plr.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 32));
						plr.sendMessage(ChatColor.GREEN + "You have activated " + ChatColor.GOLD + "Ammo" + ChatColor.GREEN + "!");
						plr.closeInventory();
						giveKillStreakSelector(plr);
					} else {
						event.setCancelled(true);
					}
				}
			} else if (event.getSlot() == 3) {
				if (kills.containsKey(plr.getUniqueId().toString())) {
					int killcount = kills.get(plr.getUniqueId().toString());
					if (killcount >= 15) {
						killcount -= 15;
						kills.put(plr.getUniqueId().toString(), killcount);
						if (getTeam(plr).equalsIgnoreCase("RED")) {
							for (int k = 0;k<blue_team.size();k++) {
								Player victim = Bukkit.getPlayer(UUID.fromString(blue_team.get(k)));

								victim.getWorld().strikeLightningEffect(victim.getLocation());
								dead.add(victim.getUniqueId().toString());

								event.setCancelled(true);

								victim.playEffect(EntityEffect.HURT);

								if (gamemode.equalsIgnoreCase("king_of_the_hill")) {

								} else if (gamemode.equalsIgnoreCase("capture_the_flag")) {
									if (redFlagCarrier.equalsIgnoreCase(victim.getUniqueId().toString())) {
										redTeamFlagLocation = victim.getLocation();
										while (redTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR || redTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WATER) {
											redTeamFlagLocation.setY(redTeamFlagLocation.getY() - 1);
										}
										Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
											@Override
											public void run() {
												redTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
												Banner banner = (Banner) redTeamFlagLocation.getBlock().getState();
												banner.setBaseColor(DyeColor.RED);
												banner.update(true);
											} 
										});
										for (int i = 0;i<players.size();i++) {
											Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Blue team player " + victim.getDisplayName() + " dropped the red team's flag!");
										}
										redFlagCarrier = "";
									} else if (blueFlagCarrier.equalsIgnoreCase(victim.getUniqueId().toString())) {
										blueTeamFlagLocation = victim.getLocation();
										while (blueTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR || blueTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WATER) {
											blueTeamFlagLocation.setY(blueTeamFlagLocation.getY() - 1);
										}
										Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
											@Override
											public void run() {
												blueTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
												Banner banner = (Banner) blueTeamFlagLocation.getBlock().getState();
												banner.setBaseColor(DyeColor.BLUE);
												banner.update(true);
											} 
										});
										for (int i = 0;i<players.size();i++) {
											Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Red team player " + victim.getDisplayName() + " dropped the blue team's flag!");
										}
										blueFlagCarrier = "";
									}
								}

								if (getTeam(victim).equalsIgnoreCase("RED")) {
									Firework firework = (Firework) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
									FireworkMeta meta = firework.getFireworkMeta();
									meta.addEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.WHITE).withFade(Color.RED).build());
									meta.setPower(0);
									firework.setFireworkMeta(meta);
									new BukkitRunnable() {
										@Override
										public void run() {
											firework.detonate();
										}
									}.runTaskLater(SpigotPaintball.getInstance(), 2L);
								} else {
									Firework firework = (Firework) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
									FireworkMeta meta = firework.getFireworkMeta();
									meta.addEffect(FireworkEffect.builder().withColor(Color.BLUE).withColor(Color.WHITE).withFade(Color.BLUE).build());
									meta.setPower(0);
									firework.setFireworkMeta(meta);
									new BukkitRunnable() {
										@Override
										public void run() {
											firework.detonate();
										}
									}.runTaskLater(SpigotPaintball.getInstance(), 2L);
								}

								revalidateGameScoreboard();

								for(int i = 0;i<players.size();i++) {
									Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + plr.getDisplayName() + ChatColor.GOLD + " fragged " + ChatColor.RED + victim.getDisplayName() + ChatColor.GOLD + " with a griff bomb!");
								}

								respawn(victim);
							}
							event.setCancelled(true);
						} else {
							for (int k = 0;k<red_team.size();k++) {
								Player victim = Bukkit.getPlayer(UUID.fromString(blue_team.get(k)));

								victim.getWorld().strikeLightningEffect(victim.getLocation());
								dead.add(victim.getUniqueId().toString());

								event.setCancelled(true);

								victim.playEffect(EntityEffect.HURT);

								if (gamemode.equalsIgnoreCase("king_of_the_hill")) {

								} else if (gamemode.equalsIgnoreCase("capture_the_flag")) {
									if (redFlagCarrier.equalsIgnoreCase(victim.getUniqueId().toString())) {
										redTeamFlagLocation = victim.getLocation();
										while (redTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR || redTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WATER) {
											redTeamFlagLocation.setY(redTeamFlagLocation.getY() - 1);
										}
										Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
											@Override
											public void run() {
												redTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
												Banner banner = (Banner) redTeamFlagLocation.getBlock().getState();
												banner.setBaseColor(DyeColor.RED);
												banner.update(true);
											} 
										});
										for (int i = 0;i<players.size();i++) {
											Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Blue team player " + victim.getDisplayName() + " dropped the red team's flag!");
										}
										redFlagCarrier = "";
									} else if (blueFlagCarrier.equalsIgnoreCase(victim.getUniqueId().toString())) {
										blueTeamFlagLocation = victim.getLocation();
										while (blueTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR || blueTeamFlagLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WATER) {
											blueTeamFlagLocation.setY(blueTeamFlagLocation.getY() - 1);
										}
										Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
											@Override
											public void run() {
												blueTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
												Banner banner = (Banner) blueTeamFlagLocation.getBlock().getState();
												banner.setBaseColor(DyeColor.BLUE);
												banner.update(true);
											} 
										});
										for (int i = 0;i<players.size();i++) {
											Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Red team player " + victim.getDisplayName() + " dropped the blue team's flag!");
										}
										blueFlagCarrier = "";
									}
								}

								if (getTeam(victim).equalsIgnoreCase("RED")) {
									Firework firework = (Firework) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
									FireworkMeta meta = firework.getFireworkMeta();
									meta.addEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.WHITE).withFade(Color.RED).build());
									meta.setPower(0);
									firework.setFireworkMeta(meta);
									new BukkitRunnable() {
										@Override
										public void run() {
											firework.detonate();
										}
									}.runTaskLater(SpigotPaintball.getInstance(), 2L);
								} else {
									Firework firework = (Firework) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
									FireworkMeta meta = firework.getFireworkMeta();
									meta.addEffect(FireworkEffect.builder().withColor(Color.BLUE).withColor(Color.WHITE).withFade(Color.BLUE).build());
									meta.setPower(0);
									firework.setFireworkMeta(meta);
									new BukkitRunnable() {
										@Override
										public void run() {
											firework.detonate();
										}
									}.runTaskLater(SpigotPaintball.getInstance(), 2L);
								}

								revalidateGameScoreboard();

								for(int i = 0;i<players.size();i++) {
									Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + plr.getDisplayName() + ChatColor.GOLD + " fragged " + ChatColor.RED + victim.getDisplayName() + ChatColor.GOLD + " with a griff bomb!");
								}

								respawn(victim);
							}
							event.setCancelled(true);
						}
						plr.sendMessage(ChatColor.GREEN + "You have activated " + ChatColor.GOLD + "The Griff Bomb" + ChatColor.GREEN + "!");
						plr.closeInventory();
						giveKillStreakSelector(plr);
					} else {
						event.setCancelled(true);
					}
				}
			} else if (event.getSlot() == 4) {
				if (kills.containsKey(plr.getUniqueId().toString())) {
					int killcount = kills.get(plr.getUniqueId().toString());
					if (killcount >= 12) {
						killcount -= 12;
						kills.put(plr.getUniqueId().toString(), killcount);
						//Give your team speed boost for 45 seconds and notify them
						plr.sendMessage(ChatColor.GREEN + "You have activated " + ChatColor.GOLD + "Cocaine Party" + ChatColor.GREEN + "!");
						plr.closeInventory();
						giveKillStreakSelector(plr);
					} else {
						event.setCancelled(true);
					}
				}
			} else if (event.getSlot() == 5) {
				if (kills.containsKey(plr.getUniqueId().toString())) {
					int killcount = kills.get(plr.getUniqueId().toString());
					if (killcount >= 10) {
						killcount -= 10;
						kills.put(plr.getUniqueId().toString(), killcount);
						//Turn you into juggernaut and alert all players
						plr.sendMessage(ChatColor.GREEN + "You have activated " + ChatColor.GOLD + "Juggernaut" + ChatColor.GREEN + "!");
						plr.closeInventory();
						giveKillStreakSelector(plr);
					} else {
						event.setCancelled(true);
					}
				}
			} else if (event.getSlot() == 6) {
				if (kills.containsKey(plr.getUniqueId().toString())) {
					int killcount = kills.get(plr.getUniqueId().toString());
					if (killcount >= 7) {
						killcount -= 7;
						kills.put(plr.getUniqueId().toString(), killcount);
						//Give chicken egg grenade and alert player
						plr.sendMessage(ChatColor.GREEN + "You have activated " + ChatColor.GOLD + "Chicken 'Nade" + ChatColor.GREEN + "!");
						plr.closeInventory();
						giveKillStreakSelector(plr);
					} else {
						event.setCancelled(true);
					}
				}
			}
		}

		event.setCancelled(true);
	}

	public void onDisableReset() {
		for (int i = 0;i<placedBlocks.size();i++) {
			placedBlocks.get(i).getBlock().setType(Material.AIR);
		}

		cooldown_block_timer.cancel();
	}

	public void reset() {
		for (int i = 0;i<placedBlocks.size();i++) {
			placedBlocks.get(i).getBlock().setType(Material.AIR);
		}

		for (int i = 0;i<players.size();i++) {
			Player player = Bukkit.getPlayer(UUID.fromString(players.get(i)));
			Bukkit.getServer().getScheduler().runTaskLater(SpigotPaintball.getInstance(), new Runnable() {
				@Override
				public void run() {
					unregisterPlayer(player);
				}
			}, 1);
		}

		cooldown_block_timer.cancel();

		gameStarted = false;
		countdown = 300;
		gameTimer = 1000;
		redTeamPoints = 0;
		blueTeamPoints = 0;
		blue_team.clear();
		red_team.clear();
		players.clear();
		kills.clear();

		if (gamemode.equalsIgnoreCase("capture_the_flag")) {
			redTeamFlagLocation = ctf_red;
			blueTeamFlagLocation = ctf_blue;
			blueFlagAtBase = true;
			redFlagAtBase = true;
			redFlagCarrier = "";
			blueFlagCarrier = "";
			Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
				@Override
				public void run() {
					redTeamFlagLocation.getBlock().setType(Material.AIR);
					blueTeamFlagLocation.getBlock().setType(Material.AIR);
				} 
			});
		}
	}

	public void resumePlayer(Player player) {
		player.sendMessage(ChatColor.RED + "type /hub if you would like to quit paintball!");
		if (quit_players.get(player.getUniqueId().toString()).equalsIgnoreCase("RED")) {
			red_team.add(player.getUniqueId().toString());
			Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
				@Override
				public void run() {
					player.teleport(red_team_spawn);
					player.getInventory().clear();
					player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
					Wool wool = new Wool();
					wool.setColor(DyeColor.RED);
					player.getInventory().setHelmet(wool.toItemStack(1));
					player.getInventory().addItem(wool.toItemStack(20));

					ItemStack leather_chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
					LeatherArmorMeta meta = (LeatherArmorMeta) leather_chestplate.getItemMeta();
					meta.setColor(Color.RED);
					leather_chestplate.setItemMeta(meta);
					player.getInventory().setChestplate(leather_chestplate);

					ItemStack leather_leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
					meta = (LeatherArmorMeta) leather_leggings.getItemMeta();
					meta.setColor(Color.RED);
					leather_leggings.setItemMeta(meta);
					player.getInventory().setLeggings(leather_leggings);

					ItemStack leather_boots = new ItemStack(Material.LEATHER_BOOTS, 1);
					meta = (LeatherArmorMeta) leather_boots.getItemMeta();
					meta.setColor(Color.RED);
					leather_boots.setItemMeta(meta);
					player.getInventory().setBoots(leather_boots);

					giveKillStreakSelector(player);

					player.setGameMode(GameMode.SURVIVAL);
				}
			});
		} else if (quit_players.get(player.getUniqueId().toString()).equalsIgnoreCase("BLUE")) {
			blue_team.add(player.getUniqueId().toString());
			Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
				@Override
				public void run() {
					player.teleport(blue_team_spawn);
					player.getInventory().clear();
					player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
					Wool wool = new Wool();
					wool.setColor(DyeColor.BLUE);
					player.getInventory().setHelmet(wool.toItemStack(1));
					player.getInventory().addItem(wool.toItemStack(20));

					ItemStack leather_chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
					LeatherArmorMeta meta = (LeatherArmorMeta) leather_chestplate.getItemMeta();
					meta.setColor(Color.BLUE);
					leather_chestplate.setItemMeta(meta);
					player.getInventory().setChestplate(leather_chestplate);

					ItemStack leather_leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
					meta = (LeatherArmorMeta) leather_leggings.getItemMeta();
					meta.setColor(Color.BLUE);
					leather_leggings.setItemMeta(meta);
					player.getInventory().setLeggings(leather_leggings);

					ItemStack leather_boots = new ItemStack(Material.LEATHER_BOOTS, 1);
					meta = (LeatherArmorMeta) leather_boots.getItemMeta();
					meta.setColor(Color.BLUE);
					leather_boots.setItemMeta(meta);
					player.getInventory().setBoots(leather_boots);

					giveKillStreakSelector(player);
				}
			});
		}

		quit_players.remove(player.getUniqueId().toString());
		players.add(player.getUniqueId().toString());
		player.setGameMode(GameMode.SURVIVAL);
		player.setFoodLevel(20);

		for (int i = 0;i<players.size();i++) {
			if (getTeam(player).equalsIgnoreCase("RED")) {
				Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.YELLOW + "Red team player " + ChatColor.RED + player.getDisplayName() + ChatColor.YELLOW + " has returned to the game!");
			} else {
				Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.YELLOW + "Blue team player " + ChatColor.BLUE + player.getDisplayName() + ChatColor.YELLOW + " has returned to the game!");
			}
		}
	}


	public void startGame() {
		//Random rand = new Random();

		gameStarted = true;

		if (gamemode.equalsIgnoreCase("capture_the_flag")) {
			gameTimer = 600;
		} else if (gamemode.equalsIgnoreCase("deathmatch")) {
			gameTimer = 300;
		}

		cooldown_block_timer = Bukkit.getServer().getScheduler().runTaskTimer(SpigotPaintball.getInstance(), new Runnable() {
			@Override
			public void run() {
				block_break_cooldown.clear();
			}
		}, 0L, 20L);

		if (gamemode.equalsIgnoreCase("capture_the_flag")) {
			Bukkit.getServer().getScheduler().runTask(SpigotPaintball.getInstance(), new Runnable() {
				@Override
				public void run() {
					redTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
					Banner banner = (Banner) redTeamFlagLocation.getBlock().getState();
					banner.setBaseColor(DyeColor.RED);
					banner.update(true);

					blueTeamFlagLocation.getBlock().setType(Material.STANDING_BANNER);
					banner = (Banner) blueTeamFlagLocation.getBlock().getState();
					banner.setBaseColor(DyeColor.BLUE);
					banner.update(true);
				} 
			});
		}

		for (int i = 0;i<players.size();i++) {
			if (getTeam(Bukkit.getPlayer(UUID.fromString(players.get(i)))).equalsIgnoreCase("NULL")) {
				if (red_team.size() > blue_team.size()) {
					red_team.add(players.get(i));
				} else if (blue_team.size() > red_team.size()){
					blue_team.add(players.get(i));
				} else if (red_team.size() == 0 && blue_team.size() == 0) {
					int ran = new Random().nextInt(1);
					if (ran == 0) {
						red_team.add(players.get(i));
					} else {
						blue_team.add(players.get(i));
					}
				}
			}
		}

		//int redTeamSize = red_team.size() + 1;
		//int blueTeamSize = blue_team.size() + 1;
		//
		//		while (red_team.size() > blueTeamSize) {
		//			int randomPlr = rand.nextInt(red_team.size() - 1);
		//			Player randomP = Bukkit.getPlayer(UUID.fromString(red_team.get(randomPlr)));
		//			red_team.remove(randomP.getUniqueId().toString());
		//			blue_team.add(randomP.getUniqueId().toString());
		//			randomP.sendMessage(ChatColor.GREEN + "You were moved to the blue team on random to balance teams!");
		//		}
		//
		//		while (blue_team.size() > redTeamSize) {
		//			int randomPlr = rand.nextInt(blue_team.size() - 1);
		//			Player randomP = Bukkit.getPlayer(UUID.fromString(blue_team.get(randomPlr)));
		//			blue_team.remove(randomP.getUniqueId().toString());
		//			red_team.add(randomP.getUniqueId().toString());
		//			randomP.sendMessage(ChatColor.GREEN + "You were moved to the red team on random to balance teams!");
		//		}

		for (int i = 0;i<players.size();i++) {
			Player plr = Bukkit.getPlayer(UUID.fromString(players.get(i)));
			Bukkit.getServer().getScheduler().runTaskLater(SpigotPaintball.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (getTeam(plr).equalsIgnoreCase("RED")) {
						plr.teleport(red_team_spawn);
						plr.getInventory().clear();
						plr.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
						Wool wool = new Wool();
						wool.setColor(DyeColor.RED);
						plr.getInventory().setHelmet(wool.toItemStack(1));
						plr.getInventory().addItem(wool.toItemStack(20));

						ItemStack leather_chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
						LeatherArmorMeta meta = (LeatherArmorMeta) leather_chestplate.getItemMeta();
						meta.setColor(Color.RED);
						leather_chestplate.setItemMeta(meta);
						plr.getInventory().setChestplate(leather_chestplate);

						ItemStack leather_leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
						meta = (LeatherArmorMeta) leather_leggings.getItemMeta();
						meta.setColor(Color.RED);
						leather_leggings.setItemMeta(meta);
						plr.getInventory().setLeggings(leather_leggings);

						ItemStack leather_boots = new ItemStack(Material.LEATHER_BOOTS, 1);
						meta = (LeatherArmorMeta) leather_boots.getItemMeta();
						meta.setColor(Color.RED);
						leather_boots.setItemMeta(meta);
						plr.getInventory().setBoots(leather_boots);

						giveKillStreakSelector(plr);

						plr.setGameMode(GameMode.SURVIVAL);

						plr.sendMessage(ChatColor.GREEN + "Game started!");
					} else if (getTeam(plr).equalsIgnoreCase("BLUE")) {
						plr.teleport(blue_team_spawn);
						plr.getInventory().clear();
						plr.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
						Wool wool = new Wool();
						wool.setColor(DyeColor.BLUE);
						plr.getInventory().setHelmet(wool.toItemStack(1));
						plr.getInventory().addItem(wool.toItemStack(20));

						ItemStack leather_chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
						LeatherArmorMeta meta = (LeatherArmorMeta) leather_chestplate.getItemMeta();
						meta.setColor(Color.BLUE);
						leather_chestplate.setItemMeta(meta);
						plr.getInventory().setChestplate(leather_chestplate);

						ItemStack leather_leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
						meta = (LeatherArmorMeta) leather_leggings.getItemMeta();
						meta.setColor(Color.BLUE);
						leather_leggings.setItemMeta(meta);
						plr.getInventory().setLeggings(leather_leggings);

						ItemStack leather_boots = new ItemStack(Material.LEATHER_BOOTS, 1);
						meta = (LeatherArmorMeta) leather_boots.getItemMeta();
						meta.setColor(Color.BLUE);
						leather_boots.setItemMeta(meta);
						plr.getInventory().setBoots(leather_boots);

						giveKillStreakSelector(plr);

						plr.setGameMode(GameMode.SURVIVAL);

						plr.sendMessage(ChatColor.GREEN + "Game started!");
					}
				}
			}, 1);

			revalidateGameScoreboard();
		}

		gameTimerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SpigotPaintball.getInstance(), new Runnable() {
			@Override
			public void run() {
				gameTimer -= 1;
				if (gameTimer <= 10 && gameTimer > 0) {
					for (int i = 0;i<players.size();i++) {
						Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "Game ends in " + ChatColor.GOLD + String.valueOf(gameTimer) + ChatColor.RED + " seconds!");
					}
				}

				if (gameTimer == 0) {
					gameStarted = false;

					for (int i = 0;i<players.size();i++) {
						Bukkit.getPlayer(UUID.fromString(players.get(i))).getInventory().clear();
					}
					if (redTeamPoints > blueTeamPoints) {
						for (int i = 0;i<players.size();i++) {
							Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.RED + "Red team wins!");
						}
					} else if (redTeamPoints < blueTeamPoints) {
						for (int i = 0;i<players.size();i++) {
							Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.BLUE + "Blue team wins!");
						}
					} else if (redTeamPoints == blueTeamPoints) { 
						for (int i = 0;i<players.size();i++) {
							Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.GOLD + "The game was a tie!");
						}
					}

					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SpigotPaintball.getInstance(), new Runnable() {
						@Override
						public void run() {
							reset();
							Bukkit.getServer().getScheduler().cancelTask(gameTimerID);
						}
					}, 100);
				}
			}
		}, 0L, 20L);
	}

	public void respawn(Player player) {
		if (getTeam(player).equalsIgnoreCase("RED")) {
			player.teleport(red_team_spawn);
		} else if (getTeam(player).equalsIgnoreCase("BLUE")) {
			player.teleport(blue_team_spawn);
		} else {
			player.teleport(lobby_spawn);
		}

		invincible.add(player.getUniqueId().toString());
		player.sendMessage(ChatColor.GOLD + "You have spawn protection for " + ChatColor.GREEN + "5s");
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SpigotPaintball.getInstance(), new Runnable() {
			@Override
			public void run() {
				invincible.remove(player.getUniqueId().toString());
				player.sendMessage(ChatColor.GOLD + "Your spawn protection has worn off!");
			}
		}, 100);

		player.getInventory().clear();
		player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
		if (getTeam(player).equalsIgnoreCase("RED")) {
			Wool wool =  new Wool();
			wool.setColor(DyeColor.RED);
			player.getInventory().addItem(wool.toItemStack(20));
		} else {
			Wool wool =  new Wool();
			wool.setColor(DyeColor.BLUE);
			player.getInventory().addItem(wool.toItemStack(20));
		}
		giveKillStreakSelector(player);

		dead.remove(player.getUniqueId().toString());
	}

	public String getTeam(Player player) {
		for (int i = 0;i<blue_team.size();i++) {
			if (blue_team.get(i).equalsIgnoreCase(player.getUniqueId().toString())) {
				return "BLUE";
			}
		}

		for (int i = 0;i<red_team.size();i++) {
			if (red_team.get(i).equalsIgnoreCase(player.getUniqueId().toString())) {
				return "RED";
			}
		}

		return "NULL";
	}

	public void showKillstreakShop(Player player) {
		int killcount = 0;

		if (kills.containsKey(player.getUniqueId().toString())) {
			killcount = kills.get(player.getUniqueId().toString());
		}

		Inventory inv = Bukkit.getServer().createInventory(player, 27, ChatColor.BLACK + "Killstreaks");

		ItemStack item = new ItemStack(Material.SNOW_BALL);
		ItemStack item2 = new ItemStack(Material.ANVIL);
		ItemStack item3 = new ItemStack(Material.CHEST);
		ItemStack item4 = new ItemStack(Material.TNT);
		ItemStack item5 = new ItemStack(Material.SUGAR);
		ItemStack item6 = new ItemStack(Material.DIAMOND_CHESTPLATE);
		ItemStack item7 = new ItemStack(Material.EGG);


		ItemMeta itemmeta = item.getItemMeta();
		ItemMeta itemmeta2 = item2.getItemMeta();
		ItemMeta itemmeta3 = item3.getItemMeta();
		ItemMeta itemmeta4 = item4.getItemMeta();
		ItemMeta itemmeta5 = item5.getItemMeta();
		ItemMeta itemmeta6 = item6.getItemMeta();
		ItemMeta itemmeta7 = item7.getItemMeta();

		if (killcount >= 7) {
			itemmeta7.setDisplayName(ChatColor.GREEN + "Chicken 'Nade");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Clear out a group of those pesky");
			lore.add(ChatColor.GRAY + "steves!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "7");
			itemmeta7.setLore(lore);
		} else {
			itemmeta7.setDisplayName(ChatColor.RED + "Chicken 'Nade");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Clear out a group of those pesky");
			lore.add(ChatColor.GRAY + "steves!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "7");
			lore.add(ChatColor.RED + "You need more killcoins!");
			itemmeta7.setLore(lore);
		}

		if (killcount >= 10) {
			itemmeta6.setDisplayName(ChatColor.GREEN + "Juggernaut");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "The human meat shield!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "10");
			itemmeta6.setLore(lore);
			
			itemmeta6.getItemFlags().clear();
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_ENCHANTS);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_PLACED_ON);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_UNBREAKABLE);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_DESTROYS);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_POTION_EFFECTS);
		} else {
			itemmeta6.setDisplayName(ChatColor.RED + "Juggernaut");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "The human meat shield!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "10");
			lore.add(ChatColor.RED + "You need more killcoins!");
			itemmeta6.setLore(lore);
			
			itemmeta6.getItemFlags().clear();
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_ENCHANTS);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_PLACED_ON);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_UNBREAKABLE);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_DESTROYS);
			itemmeta6.getItemFlags().add(ItemFlag.HIDE_POTION_EFFECTS);
		}

		if (killcount >= 12) {
			itemmeta5.setDisplayName(ChatColor.GREEN + "Cocaine Party");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "This cocaine got the whole");
			lore.add(ChatColor.GRAY + "party crazy!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "12");
			itemmeta5.setLore(lore);
		} else {
			itemmeta5.setDisplayName(ChatColor.RED + "Cocaine Party");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "This cocaine got the whole");
			lore.add(ChatColor.GRAY + "party crazy!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "12");
			lore.add(ChatColor.RED + "You need more killcoins!");
			itemmeta5.setLore(lore);
		}

		if (killcount >= 15) {
			itemmeta4.setDisplayName(ChatColor.GREEN + "The Griff Bomb");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Griff bomb incoming!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "15");
			itemmeta4.setLore(lore);
		} else {
			itemmeta4.setDisplayName(ChatColor.RED + "The Griff Bomb");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Griff bomb incoming!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "15");
			lore.add(ChatColor.RED + "You need more killcoins!");
			itemmeta4.setLore(lore);
		}

		if (killcount >= 5) {
			itemmeta.setDisplayName(ChatColor.GREEN + "Strong Arm");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Shoot your paintball twice as far");
			lore.add(ChatColor.GRAY + "and twice as fast!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "5");
			itemmeta.setLore(lore);
		} else {
			itemmeta.setDisplayName(ChatColor.RED + "Strong Arm");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Shoot your paintball twice as far");
			lore.add(ChatColor.GRAY + "and twice as fast!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "5");
			lore.add(ChatColor.RED + "You need more killcoins!");
			itemmeta.setLore(lore);
		}

		if (killcount >= 5) {
			itemmeta2.setDisplayName(ChatColor.GREEN + "Team Ammo");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Give everyone on your team");
			lore.add(ChatColor.GRAY + "32 paintballs!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "5");
			itemmeta2.setLore(lore);
		} else {
			itemmeta2.setDisplayName(ChatColor.RED + "Team Ammo");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Give everyone on your team");
			lore.add(ChatColor.GRAY + "32 paintballs!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "5");
			lore.add(ChatColor.RED + "You need more killcoins!");
			itemmeta2.setLore(lore);
		}

		if (killcount >= 2) {
			itemmeta3.setDisplayName(ChatColor.GREEN + "Ammo");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Supplies you with 32 more paintballs!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "2");
			itemmeta3.setLore(lore);
		} else {
			itemmeta3.setDisplayName(ChatColor.RED + "Ammo");

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Supplies you with 32 more paintballs!");
			lore.add("");
			lore.add(ChatColor.GRAY + "Killcoins: " + ChatColor.GOLD + "2");
			lore.add(ChatColor.RED + "You need more killcoins!");
			itemmeta3.setLore(lore);
		}

		item.setItemMeta(itemmeta);
		item2.setItemMeta(itemmeta2);
		item3.setItemMeta(itemmeta3);
		item4.setItemMeta(itemmeta4);
		item5.setItemMeta(itemmeta5);
		item6.setItemMeta(itemmeta6);
		item7.setItemMeta(itemmeta7);

		inv.setItem(0, item);
		inv.setItem(1, item2);
		inv.setItem(2, item3);
		inv.setItem(3, item4);
		inv.setItem(4, item5);
		inv.setItem(5, item6);
		inv.setItem(6, item7);

		player.openInventory(inv);
	}

	public void showTeamSelectorMenu(Player player) {
		Inventory inv = Bukkit.getServer().createInventory(player, 54, ChatColor.BLACK + "Select Your Team!");

		Wool redwool = new Wool();
		redwool.setColor(DyeColor.RED);
		ItemStack redwoolitem = redwool.toItemStack(1);

		Wool bluewool = new Wool();
		bluewool.setColor(DyeColor.BLUE);
		ItemStack bluewoolitem = bluewool.toItemStack(1);

		ItemMeta redmeta = redwoolitem.getItemMeta();
		ItemMeta bluemeta = bluewoolitem.getItemMeta();

		bluemeta.setDisplayName(ChatColor.BLUE + "Blue Team");
		bluemeta.setLore(new ArrayList<String>());
		bluewoolitem.setItemMeta(bluemeta);

		redmeta.setDisplayName(ChatColor.RED + "Red Team");
		redmeta.setLore(new ArrayList<String>());
		redwoolitem.setItemMeta(redmeta);

		inv.setItem(20, redwoolitem);
		inv.setItem(24, bluewoolitem);

		player.openInventory(inv);
	}

	public void giveTeamSelectorTool(Player player) {
		ItemStack item = new ItemStack(Material.BOOK, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Team Selector");
		meta.setLore(new ArrayList<String>());
		item.setItemMeta(meta);
		player.getInventory().addItem(item);
	}

	public void giveKillStreakSelector(Player player) {
		int killcount = 0;

		if (kills.containsKey(player.getUniqueId().toString())) {
			killcount = kills.get(player.getUniqueId().toString());
		}

		if (killcount > 64) {
			killcount = 64;
		}

		if (killcount == 0) {
			killcount = 1;
		}

		ItemStack item = new ItemStack(Material.DIAMOND, killcount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Killstreaks");
		meta.setLore(new ArrayList<String>());
		item.setItemMeta(meta);
		player.getInventory().setItem(8, item);
	}

	public void revalidateLobbyScoreboard() {
		obj.unregister();
		obj = board.registerNewObjective("test", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.YELLOW + "PAINTBALL");
		obj.getScore("").setScore(5);
		obj.getScore(ChatColor.WHITE + "Map: " + ChatColor.GREEN + map_name).setScore(4);
		if (gamemode.equalsIgnoreCase("capture_the_flag")) {
			obj.getScore(ChatColor.WHITE + "Gamemode: " + ChatColor.GREEN + "Capture the Flag").setScore(3);
		} else if (gamemode.equalsIgnoreCase("king_of_the_hill")) {
			obj.getScore(ChatColor.WHITE + "Gamemode: " + ChatColor.GREEN + "King of the Hill").setScore(3);
		} else {
			obj.getScore(ChatColor.WHITE + "Gamemode: " + ChatColor.GREEN + "Deathmatch").setScore(3);
		}
		obj.getScore(ChatColor.WHITE + "Players: " + ChatColor.GREEN + String.valueOf(players.size()) + "/24").setScore(2);
		obj.getScore(" ").setScore(1);
		if (countdown <= 30) {
			if (countdown == 0) {
				obj.getScore("Game Starts In: " + ChatColor.GREEN + "Now!").setScore(0);
			} else {
				obj.getScore("Game Starts In: " + ChatColor.GREEN + String.valueOf(countdown) + "s").setScore(0);
			}
		} else {
			obj.getScore("Waiting...").setScore(0);
		}

		for (int i = 0;i<players.size();i++) {
			Player plr = Bukkit.getPlayer(UUID.fromString(players.get(i)));
			plr.setScoreboard(board);
		}
	}

	public void revalidateGameScoreboard() {
		obj.unregister();
		redTeamObj.unregister();
		blueTeamObj.unregister();
		obj = board.registerNewObjective("test", "dummy");
		redTeamObj = board.registerNewTeam("redteam");
		blueTeamObj = board.registerNewTeam("blueteam");
		redTeamObj.setPrefix(ChatColor.RED + "");
		blueTeamObj.setPrefix(ChatColor.BLUE + "");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.YELLOW + "PAINTBALL");
		Score blank = obj.getScore("");
		Score redTeam = obj.getScore(ChatColor.WHITE +  "Red Team:  " + ChatColor.RED + String.valueOf(redTeamPoints));
		Score blueTeam = obj.getScore(ChatColor.WHITE + "Blue Team: " + ChatColor.BLUE + String.valueOf(blueTeamPoints));
		blank.setScore(2);
		redTeam.setScore(1);							
		blueTeam.setScore(0);		
		for (int i = 0;i<players.size();i++) {
			Player plr = Bukkit.getPlayer(UUID.fromString(players.get(i)));
			if (getTeam(plr).equalsIgnoreCase("RED")) {
				redTeamObj.addEntry(plr.getDisplayName());
			} else {
				blueTeamObj.addEntry(plr.getDisplayName());
			}
			plr.setScoreboard(board);
		}
	}

	public void unregisterPlayer(Player player) {
		if (getTeam(player).equalsIgnoreCase("RED")) {
			redTeamObj.removeEntry(player.getDisplayName());
		}

		if (getTeam(player).equalsIgnoreCase("BLUE")) {
			blueTeamObj.removeEntry(player.getDisplayName());
		}

		player.getInventory().clear();
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		player.getInventory().setChestplate(new ItemStack(Material.AIR));
		player.getInventory().setLeggings(new ItemStack(Material.AIR));
		player.getInventory().setBoots(new ItemStack(Material.AIR));
		player.teleport(Bukkit.getWorld("world").getSpawnLocation());
		player.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
		players.remove(player.getUniqueId().toString());

		if (!gameStarted) {
			for (int i = 0;i<players.size();i++) {
				Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.GOLD + "[" + ChatColor.YELLOW + "PAINTBALL" + ChatColor.GOLD + "] " + ChatColor.GRAY + player.getDisplayName() + ChatColor.GREEN + " has left (" + ChatColor.AQUA + String.valueOf(players.size()) + ChatColor.GREEN + "/" + ChatColor.AQUA + "24" + ChatColor.GREEN + ")!");
			}

			revalidateLobbyScoreboard();
		} else {
			for (int i = 0;i<players.size();i++) {
				Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.GOLD + "[" + ChatColor.YELLOW + "PAINTBALL" + ChatColor.GOLD + "] " + ChatColor.GRAY + player.getDisplayName() + ChatColor.GREEN + " has left!");
			}
		}
		
		if (players.size() == 0) {
			reset();
		}
	}

	public void registerPlayer(Player player) {
		if ((players.size() + 1) > 24) {
			player.sendMessage(ChatColor.RED + "Lobby full!");
			return;
		}

		if (gameStarted) {
			player.sendMessage(ChatColor.RED + "Game is already in session!");
			return;
		}

		players.add(player.getUniqueId().toString());
		player.teleport(lobby_spawn);
		for (int i = 0;i<players.size();i++) {
			Bukkit.getPlayer(UUID.fromString(players.get(i))).sendMessage(ChatColor.GOLD + "[" + ChatColor.YELLOW + "PAINTBALL" + ChatColor.GOLD + "] " + ChatColor.GRAY + player.getDisplayName() + ChatColor.GREEN + " has joined (" + ChatColor.AQUA + String.valueOf(players.size()) + ChatColor.GREEN + "/" + ChatColor.AQUA + "24" + ChatColor.GREEN + ")!");
		}
		player.setGameMode(GameMode.ADVENTURE);
		player.setFoodLevel(20);

		player.getInventory().clear();
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SpigotPaintball.getInstance(), new Runnable() {
			@Override
			public void run() {
				giveTeamSelectorTool(player);
			}
		}, 20L);

		revalidateLobbyScoreboard();

		if (players.size() >= 12) {
			countdown = 30;
			startLobbyTimer();
		}
	}
}
