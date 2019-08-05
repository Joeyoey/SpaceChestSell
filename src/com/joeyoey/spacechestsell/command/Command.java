package com.joeyoey.spacechestsell.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.joeyoey.spacechestsell.SpaceChestSell;
import com.joeyoey.spacechestsell.objects.JoLocation;

import net.md_5.bungee.api.ChatColor;

public class Command implements CommandExecutor {

	private SpaceChestSell plugin;

	public Command(SpaceChestSell instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("chestsell")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("add")) {
						Block block = p.getTargetBlock(null, 5);
						if (block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN
								|| block.getType() == Material.SIGN_POST) {
							JoLocation jLoc = new JoLocation(block.getLocation());
							if (plugin.getSigns().containsKey(jLoc)) {
								if (plugin.getSigns().get(jLoc).contains(p.getUniqueId())) {
									String players = args[1];
									String[] sepPlayers = players.split(",");
									for (int i = 0; i < sepPlayers.length; i++) {
										OfflinePlayer offlinep = Bukkit.getOfflinePlayer(sepPlayers[i]);
										if (!offlinep.hasPlayedBefore()) {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&',
													plugin.getConfig().getString("messages.player-hasnt-joined")
															.replaceAll("%player%", offlinep.getName())));

										} else {
											plugin.getSigns().get(jLoc).add(offlinep.getUniqueId());
											p.sendMessage(ChatColor.translateAlternateColorCodes('&',
													plugin.getConfig().getString("messages.player-added")
															.replaceAll("%player%", offlinep.getName())));
										}
									}
								} else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&',
											plugin.getConfig().getString("messages.you-are-not-owner")));
								}
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&',
										plugin.getConfig().getString("messages.not-a-sell-sign")));
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									plugin.getConfig().getString("messages.not-a-sell-sign")));
						}
					} else if (args[0].equalsIgnoreCase("remove")) {
						Block block = p.getTargetBlock(null, 5);
						if (block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN
								|| block.getType() == Material.SIGN_POST) {
							JoLocation jLoc = new JoLocation(block.getLocation());
							if (plugin.getSigns().containsKey(jLoc)) {
								if (plugin.getSigns().get(jLoc).contains(p.getUniqueId())) {
									String players = args[1];
									String[] sepPlayers = players.split(",");
									for (int i = 0; i < sepPlayers.length; i++) {
										OfflinePlayer offlinep = Bukkit.getOfflinePlayer(sepPlayers[i]);
										if (!offlinep.hasPlayedBefore()) {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&',
													plugin.getConfig().getString("messages.player-hasnt-joined")
															.replaceAll("%player%", offlinep.getName())));

										} else {
											plugin.getSigns().get(jLoc).remove(offlinep.getUniqueId());
											p.sendMessage(ChatColor.translateAlternateColorCodes('&',
													plugin.getConfig().getString("messages.player-removed")
															.replaceAll("%player%", offlinep.getName())));
										}
									}
								} else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&',
											plugin.getConfig().getString("messages.you-are-not-owner")));
								}
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&',
										plugin.getConfig().getString("messages.not-a-sell-sign")));
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									plugin.getConfig().getString("messages.not-a-sell-sign")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								plugin.getConfig().getString("messages.wrong-arguments")));
					}
				} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
					Block block = p.getTargetBlock(null, 5);
					if (block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN
							|| block.getType() == Material.SIGN_POST) {
						JoLocation jLoc = new JoLocation(block.getLocation());
						if (plugin.getSigns().containsKey(jLoc)) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									plugin.getConfig().getString("messages.header-list")));
							plugin.getSigns().get(jLoc).forEach(id -> {
								OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(id);
								try {
									if (offPlayer != null && !offPlayer.equals(null)) {
										p.sendMessage(ChatColor.translateAlternateColorCodes('&',
												plugin.getConfig().getString("messages.list-format")
														.replaceAll("%player%", offPlayer.getName())));
									}
								} catch (NullPointerException e) {

								}
							});
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									plugin.getConfig().getString("messages.not-a-sell-sign")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								plugin.getConfig().getString("messages.not-a-sell-sign")));
					}
				} else {
					p.sendMessage(
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.help")));
				}
			}
		} else {
			return false;
		}
		return false;
	}

}
