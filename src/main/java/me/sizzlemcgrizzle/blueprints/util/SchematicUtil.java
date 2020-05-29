package me.sizzlemcgrizzle.blueprints.util;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.craftlancer.core.util.Tuple;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SchematicUtil {
	
	private static File blueprintFile = new File(BlueprintsPlugin.instance.getDataFolder(), "blueprints.yml");
	private static YamlConfiguration config;
	
	/**
	 * @return A list of all schematic file names in WorldEdit
	 */
	public static List<String> getSchematics() {
		WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		File newFile = new File(worldEditPlugin.getDataFolder() + File.separator + "schematics");
		
		List<String> list = new ArrayList<>();
		
		if (!newFile.exists() || newFile.listFiles() == null) {
			return list;
		}
		
		for (File file : newFile.listFiles())
			list.add(file.getName());
		return list;
		
	}
	
	/**
	 * Adds a blueprint from a string (the schematic file name) and a block (item in hand).
	 *
	 * @param schematic the schematic of the blueprint
	 * @param blueprint the item of the blueprint
	 * @param type      the type of the blueprint
	 * @return true if successful, false if the schematic file doesn't exist
	 */
	public static boolean addBlueprint(String schematic, ItemStack blueprint, String type) {
		WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		blueprint.setAmount(1);
		
		try {
			if (!blueprintFile.exists())
				blueprintFile.createNewFile();
			
			config = YamlConfiguration.loadConfiguration(blueprintFile);
			
			//Check if the desired schematic is in the Schematics folder.
			File newFile = new File(worldEditPlugin.getDataFolder().getAbsolutePath()
					+ File.separator + "/schematics"
					+ File.separator + "/" + schematic);
			if (!newFile.exists())
				return false;
			
			//Check if the schematic has a special name, if it doesn't, return the notSpecial string, if it does, make a new section
			//in that config that will add the schematic name and ItemStack of the block being held.
			
			config.createSection(blueprint.getItemMeta().getDisplayName());
			config.getConfigurationSection(blueprint.getItemMeta().getDisplayName()).set("Schematic", schematic);
			config.getConfigurationSection(blueprint.getItemMeta().getDisplayName()).set("Type", type);
			config.getConfigurationSection(blueprint.getItemMeta().getDisplayName()).set("Blueprint", blueprint);
			
			config.save(blueprintFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Gets the schematic and type for a given blueprint item
	 *
	 * @param item the blueprint item
	 * @return A tuple of the schematic and type as strings
	 */
	public static Tuple<String, String> getSchematicFor(ItemStack item) {
		config = YamlConfiguration.loadConfiguration(blueprintFile);
		
		String schematicPut = null;
		String type = null;
		
		if (item.getItemMeta().getDisplayName().equals(""))
			return null;
		ConfigurationSection configSection = config.getConfigurationSection(item.getItemMeta().getDisplayName());
		if (configSection == null)
			return null;
		for (String key : configSection.getKeys(false)) {
			if (key.equals("Schematic"))
				schematicPut = configSection.getString(key);
			if (key.equals("Type"))
				type = configSection.getString(key);
		}
		if (type == null)
			type = "NORMAL";
		
		return new Tuple<>(schematicPut, type);
	}
	
	/**
	 * Removes a blueprint depending on the given item
	 *
	 * @param item the item in the player's hand to remove
	 * @return true if successful
	 */
	public static boolean removeBlueprint(ItemStack item) {
		try {
			config = YamlConfiguration.loadConfiguration(blueprintFile);
			
			if (item.getType().isAir())
				return false;
			if (!item.getItemMeta().getDisplayName().equals("")) {
				
				if (config.contains(item.getItemMeta().getDisplayName())) {
					config.set(item.getItemMeta().getDisplayName(), null);
					config.save(blueprintFile);
					return true;
				} else
					return false;
			} else
				return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets a map of all the blueprints
	 *
	 * @return a map containing each blueprint's ItemStack and schematic name
	 */
	public static HashMap<ItemStack, String> listBlueprints() {
		
		HashMap<ItemStack, String> listBlueprintMap = new HashMap<>();
		
		try {
			config.load(blueprintFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		for (String itemName : config.getKeys(false)) {
			listBlueprintMap.put(config.getConfigurationSection(itemName).getItemStack("Blueprint"), config.getConfigurationSection(itemName).getString("Schematic"));
		}
		return listBlueprintMap;
		
	}
	
	/*
	 * Gets the blueprint item for a given schematic file. If there are multiple blueprints
	 * using one file, then both are returned.
	 */
	public static List<ItemStack> getBlueprint(String schematic) {
		ArrayList<ItemStack> returnItem = new ArrayList<>();
		
		config = YamlConfiguration.loadConfiguration(blueprintFile);
		
		config.getKeys(false)
				.stream()
				.filter(itemName -> config.getConfigurationSection(itemName).getString("Schematic").equals(schematic))
				.forEach(itemName -> returnItem.add(config.getConfigurationSection(itemName).getItemStack("Blueprint")));
		return returnItem;
	}
	
	public static Set<String> cacheBlueprints() {
		YamlConfiguration config = new YamlConfiguration();
		
		try {
			if (!blueprintFile.exists())
				blueprintFile.createNewFile();
			
			config.load(blueprintFile);
			
			Common.log("Caching blueprints for optimization...");
			return config.getKeys(false);
			
		} catch (IOException | InvalidConfigurationException e) {
			return new HashSet<>();
		}
	}
}
