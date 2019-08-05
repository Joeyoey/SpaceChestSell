package com.joeyoey.spacechestsell.events;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;

import com.joeyoey.spacechestsell.SpaceChestSell;
import com.joeyoey.spacechestsell.objects.JoLocation;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class BlockInteractEvent implements Listener {

	private SpaceChestSell plugin;
	
	
	public BlockInteractEvent(SpaceChestSell instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		Player p = e.getPlayer();
		
		if (e.getLine(0).equalsIgnoreCase(plugin.getConfig().getString("required"))) {
			Sign sign = (Sign) e.getBlock().getState().getData();
			Block attached = e.getBlock().getRelative(sign.getAttachedFace());
			if (attached.getType() == Material.CHEST || attached.getType() == Material.TRAPPED_CHEST) {
				if (p.hasPermission("spacechestsell.place")) {
					int size = plugin.getConfig().getStringList("layout").size();
					for (int i = 0; i < size; i++) {
						e.setLine(i, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getStringList("layout").get(i)));
					}
					List<UUID> toAdd = new ArrayList<UUID>();
					toAdd.add(p.getUniqueId());
					plugin.getSigns().put(new JoLocation(e.getBlock().getLocation()), toAdd);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.success")));
				} else {
					e.getBlock().breakNaturally();
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission")));
				}
			} else {
				return;
			}
		} else {
			return;
		}
	}
	
	@EventHandler
	public void onBlockInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

			if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.WALL_SIGN) {

				Sign sign = (Sign) e.getClickedBlock().getState().getData();
				org.bukkit.block.Sign sine = (org.bukkit.block.Sign) e.getClickedBlock().getState();
				Block attached = e.getClickedBlock().getRelative(sign.getAttachedFace());
				if (attached.getType() == Material.CHEST || attached.getType() == Material.TRAPPED_CHEST) {

					for (int i = 0; i < plugin.getLayoutLore().size(); i++) {
						if (!plugin.getLayoutLore().get(i).equals(sine.getLine(i))) {
							return;
						}
					}

					if (!p.hasPermission("spacechestsell.sell")) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission")));
						return;
					}
					
					if (plugin.getSigns().containsKey(new JoLocation(sine.getLocation()))) {
						if (!plugin.getSigns().get(new JoLocation (sine.getLocation())).contains(p.getUniqueId())) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-on-sign")));
							return;
						}
					}
					
					if (!plugin.getSigns().containsKey(new JoLocation(sine.getLocation()))) {
						sine.getBlock().breakNaturally();
						return;
					}
					
					
					org.bukkit.block.Chest chest = (org.bukkit.block.Chest) attached.getState();
					Inventory inventory = chest.getInventory();
					
					double priceMultiplier = 1.0;
					
					int slot = 0;
					double totalSale = 0.0D;
					NumberFormat numf = NumberFormat.getInstance();
					numf.setGroupingUsed(true);
					HashMap<Material, Integer> iAmounts = new HashMap<Material, Integer>();
					HashMap<Material, Double> iPrices = new HashMap<Material, Double>();
					String type;
					for (ItemStack ischest : inventory) {

						if ((ischest == null) || (ischest.getType().equals(Material.AIR))
								|| (ischest.getType() == null)) {
							slot++;
						} else {
							
							type = ischest.getType().toString().toLowerCase();
							type = type.replace("_", "");
							@SuppressWarnings("deprecation")
							String dataCode = String.valueOf(ischest.getData().getData());
							if (plugin.getPrices().containsKey(type) && plugin.getPrices().get(type) > 0) {
								chest.getInventory().setItem(slot, new ItemStack(Material.AIR));

								totalSale = totalSale + ((Double) plugin.getPrices().get(type)).doubleValue()
										* ischest.getAmount() * priceMultiplier;
								if (iPrices.containsKey(ischest.getType())) {
									iPrices.put(ischest.getType(),
											Double.valueOf(
													((Double) iPrices.get(ischest.getType())).doubleValue()
															+ ((Double) plugin.getPrices().get(type)).doubleValue()
																	* ischest.getAmount() * priceMultiplier));
								} else {
									iPrices.put(ischest.getType(),
											Double.valueOf(((Double) plugin.getPrices().get(type)).doubleValue()
													* ischest.getAmount() * priceMultiplier));
								}
								if (iAmounts.containsKey(ischest.getType())) {
									iAmounts.put(ischest.getType(),
											Integer.valueOf((iAmounts.get(ischest.getType())).intValue()
													+ ischest.getAmount()));
								} else {
									iAmounts.put(ischest.getType(), Integer.valueOf(ischest.getAmount()));
								}
							} else if (plugin.getPrices().containsKey(type + ":" + dataCode)
									&& plugin.getPrices().get(type + ":" + dataCode) > 0) {
								chest.getInventory().setItem(slot, new ItemStack(Material.AIR));

								totalSale = totalSale
										+ ((Double) plugin.getPrices().get(type + ":" + dataCode)).doubleValue()
												* ischest.getAmount() * priceMultiplier;
								if (iPrices.containsKey(ischest.getType())) {
									iPrices.put(ischest.getType(), Double
											.valueOf(((Double) iPrices.get(ischest.getType())).doubleValue()
													+ ((Double) plugin.getPrices().get(type + ":" + dataCode))
															.doubleValue() * ischest.getAmount()
															* priceMultiplier));
								} else {
									iPrices.put(ischest.getType(), Double.valueOf(
											((Double) plugin.getPrices().get(type + ":" + dataCode)).doubleValue()
													* ischest.getAmount() * priceMultiplier));
								}
								if (iAmounts.containsKey(ischest.getType())) {
									iAmounts.put(ischest.getType(),
											Integer.valueOf(iAmounts.get(ischest.getType())).intValue()
													+ ischest.getAmount());
								} else {
									iAmounts.put(ischest.getType(), Integer.valueOf(ischest.getAmount()));

								}
							} else if (plugin.isShopguiplus()) {

								if (ShopGuiPlusApi.getItemStackPriceSell(p, ischest) > 0) {
									chest.getInventory().setItem(slot, new ItemStack(Material.AIR));

									totalSale = totalSale
											+ ShopGuiPlusApi.getItemStackPriceSell(p, ischest)
													* priceMultiplier;
									if (iPrices.containsKey(ischest.getType())) {
										iPrices.put(ischest.getType(), Double
												.valueOf(((Double) iPrices.get(ischest.getType())).doubleValue()
														+ ShopGuiPlusApi.getItemStackPriceSell(p, ischest)
																* priceMultiplier));
									} else {
										iPrices.put(ischest.getType(),
												Double.valueOf(
														ShopGuiPlusApi.getItemStackPriceSell(p, ischest)
																* priceMultiplier));
									}
									if (iAmounts.containsKey(ischest.getType())) {
										iAmounts.put(ischest.getType(),
												Integer.valueOf(iAmounts.get(ischest.getType())).intValue()
														+ ischest.getAmount());
									} else {
										iAmounts.put(ischest.getType(), Integer.valueOf(ischest.getAmount()));
									}
								}
							}
							slot++;
						}
					}
					if (iAmounts.isEmpty()) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								plugin.getConfig().getString("messages.chest-empty")));
						return;
					}

					EconomyResponse er = plugin.economy.depositPlayer(p, totalSale);
					if (er.transactionSuccess()) {
						p.sendMessage(ChatColor
								.translateAlternateColorCodes('&',
										plugin.getConfig().getString("messages.sale"))
								.replace("%amount%", numf.format(totalSale)));
						if (plugin.getConfig().getBoolean("breakdown")) {
							for (Iterator<Entry<Material, Integer>> itr = iAmounts.entrySet().iterator(); itr
									.hasNext();) {
								Entry<Material, Integer> entry = itr.next();
								Material material = entry.getKey();
								Integer value = entry.getValue();
								p.sendMessage(ChatColor
										.translateAlternateColorCodes('&',
												plugin.getConfig().getString("messages.breakdown"))
										.replaceAll("%amount%", value.toString())
										.replace("%item%",
												StringUtils.capitalize(
														material.name().toLowerCase().replace("_", " ")))
										.replace("%price%", numf.format(iPrices.get(material))));
							}
						}
					}
				}
			} else {
				return;
			}
		} else {
			return;
		}
	}
	
	
}
