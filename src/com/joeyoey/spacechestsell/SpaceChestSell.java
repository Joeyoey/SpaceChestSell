package com.joeyoey.spacechestsell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.joeyoey.spacechestsell.command.Command;
import com.joeyoey.spacechestsell.events.BlockInteractEvent;
import com.joeyoey.spacechestsell.objects.JoLocation;
import com.joeyoey.spacechestsell.serial.SerialUtil;

import net.milkbowl.vault.economy.Economy;

public class SpaceChestSell extends JavaPlugin {

	
	private HashMap<String, Double> prices = new HashMap<String, Double>();
	private List<String> layoutLore = new ArrayList<String>();

	private HashMap<JoLocation, List<UUID>> signs = new HashMap<JoLocation, List<UUID>>();
	private String required;
	private static File file;
	public Economy economy = null;
	private boolean shopguiplus;

	
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = (Economy) economyProvider.getProvider();
		}
		return economy != null;
	}
	
	
	public void onEnable() {
		setupEconomy();
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);

		try {
			createOutBin();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			signs = SerialUtil.readFromFile(file);
			getLogger().log(Level.INFO, "Loaded " + signs.size() + " signs, preparing to win :D");
		} catch (ClassNotFoundException | IOException e) {
			getLogger().log(Level.SEVERE, "No File was found to load data from, are you using the plugin?");
		}
		
		shopguiplus = getConfig().getBoolean("shopguiplus");
		if (!shopguiplus) {
			loadPrices();
		}
		getServer().getPluginManager().registerEvents(new BlockInteractEvent(this), this);
		getCommand("chestsell").setExecutor(new Command(this));
	}
	
	public void onDisable() {
		getLogger().log(Level.WARNING, "Saving data!");
		try {
			SerialUtil.writeToFile(signs, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		getLogger().log(Level.WARNING, "Data saved!");
	}
	
	
	private void createOutBin() throws IOException {
		file = new File(getDataFolder(), "signs.bin");
		file.createNewFile();
	}
	
	public void loadPrices() {
		File essentials = new File("plugins/Essentials/worth.yml");
		File config = new File("plugins/SpaceChestSell/config.yml");
		if (essentials.exists()) {
			Bukkit.getLogger().log(Level.INFO,
					"Essentials has been found. If you wish to use this enable it in your config.yml by setting essentials-price to TRUE.");
			if (getConfig().getBoolean("use-essentials-worth-yml")) {
				Bukkit.getLogger().log(Level.INFO,
						"This plugin is configured to use Essentials worth.yml");
			}
		} else {
			Bukkit.getLogger().log(Level.WARNING,
					"Essentials worth.yml has not been located. In your config.yml make sure essentials-price is set to FALSE.");
		}
		if (getConfig().getBoolean("use-essentials-worth-yml")) {
			parseConfigFile(essentials, "worth");
		} else {
			parseConfigFile(config, "prices");
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public void parseConfigFile(File file, String rootKey) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		config.getConfigurationSection(rootKey).getKeys(false).forEach(s -> {
			if (config.isConfigurationSection(rootKey + "." + s)) {
				for (String itemdata : config.getConfigurationSection(rootKey + "." + s).getKeys(false)) {
					String itemReformat = s;
					Double price = Double.valueOf(config.getDouble(rootKey + "." + s + "." + itemdata));

					boolean isItemId = false;

					try {
						Integer.parseInt(itemReformat);
						isItemId = true;
					} catch (NumberFormatException localNumberFormatException) {
					}

					if (isItemId) {

						try {
							itemReformat = Material.getMaterial(Integer.parseInt(itemReformat)).toString();
						} catch (NullPointerException localNullPointerException) {
						}

					}

					itemReformat = itemReformat.replace("_", "").toLowerCase();

					System.out.println("[SpaceChestSell] Item " + itemReformat + " registered for $" + price);
					this.prices.put(itemReformat + ":" + itemdata, price);
				}
			} else {
				String itemReformat = s;
				Double price = Double.valueOf(config.getDouble(rootKey + "." + s));

				boolean isItemId = false;

				try {
					Integer.parseInt(itemReformat);
					isItemId = true;
				} catch (NumberFormatException localNumberFormatException1) {
				}

				if (isItemId) {

					try {
						itemReformat = Material.getMaterial(Integer.parseInt(itemReformat)).toString();
					} catch (NullPointerException localNullPointerException1) {
					}

				}
				itemReformat = itemReformat.replace("_", "").toLowerCase();

				System.out.println("[SpaceChestSell] Item " + itemReformat + " registered for $" + price);
				this.prices.put(itemReformat, price);
			}

		});
		System.out.println("[SpaceChestSell] A total of " + this.prices.size() + " items have been registered.");
	}

	public HashMap<String, Double> getPrices() {
		return prices;
	}

	public void setPrices(HashMap<String, Double> prices) {
		this.prices = prices;
	}

	public List<String> getLayoutLore() {
		return layoutLore;
	}

	public String getRequired() {
		return required;
	}

	public boolean isShopguiplus() {
		return shopguiplus;
	}

	public HashMap<JoLocation, List<UUID>> getSigns() {
		return signs;
	}
	
	
	
}
