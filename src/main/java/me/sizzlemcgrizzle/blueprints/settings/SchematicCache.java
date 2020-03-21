package me.sizzlemcgrizzle.blueprints.settings;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchematicCache {
	private File file = new File(BlueprintsPlugin.getData().getAbsolutePath() + File.separator + "/blueprints.yml");
	private YamlConfiguration config = new YamlConfiguration();
	private HashMap<ItemStack, String> listBlueprintMap = new HashMap<>();

	private WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");

	/*
	 * Adds a blueprint from a string (the schematic file name) and a block (item in hand).
	 */
	public String addBlueprint(String schematic, ItemStack blueprint) throws IOException, InvalidConfigurationException {
		String success = Settings.Messages.MESSAGE_PREFIX + "&7You successfully added a blueprint with name " + blueprint.getItemMeta().getDisplayName() + " &7calling schematic &d" + schematic;
		String failure = Settings.Messages.MESSAGE_PREFIX + "&cThere is no such file '&4" + schematic + "&c'. Please add the schematic to the Schematics folder.";
		String notSpecial = Settings.Messages.MESSAGE_PREFIX + "&cThe block you are holding does not have a special name, and this is dangerous!";

		blueprint.setAmount(1);

		if (!file.exists())
			file.createNewFile();

		config.load(file);

		//Check if the desired schematic is in the Schematics folder.
		File newFile = new File(worldEditPlugin.getDataFolder().getAbsolutePath() + File.separator + "/schematics" + File.separator + "/" + schematic);
		if (!newFile.exists())
			return failure;

		//Check if the schematic has a special name, if it doesn't, return the notSpecial string, if it does, make a new section
		//in that config that will add the schematic name and ItemStack of the block being held.
		if (!blueprint.getItemMeta().getDisplayName().equals("")) {
			config.createSection(blueprint.getItemMeta().getDisplayName());
			config.getConfigurationSection(blueprint.getItemMeta().getDisplayName()).set("Schematic", schematic);
			config.getConfigurationSection(blueprint.getItemMeta().getDisplayName()).set("Blueprint", blueprint);
		} else
			return notSpecial;
		config.save(file);

		return success;
	}

	/*
	 * (Used in BlueprintBuilder) Gets the schematic for a given block/item.
	 */
	public String getSchematicFor(ItemStack item) throws IOException, InvalidConfigurationException {
		config.load(file);
		String schematicPut = null;

		if (!item.getItemMeta().getDisplayName().equals("")) {
			ConfigurationSection configSection = config.getConfigurationSection(item.getItemMeta().getDisplayName());
			if (configSection != null)
				for (String key : configSection.getKeys(false)) {
					if (key.equals("Schematic"))
						schematicPut = configSection.getString(key);
				}
		} else
			return null;

		return schematicPut;
	}

	/*
	 * Removes a blueprint from a given item.
	 */
	public String removeBlueprint(ItemStack item) throws IOException, InvalidConfigurationException {
		String failure = Settings.Messages.MESSAGE_PREFIX + "&cA blueprint for this item does not exist.";

		config.load(file);

		if (item.getType().isAir())
			return failure;
		if (!item.getItemMeta().getDisplayName().equals("")) {
			String success = Settings.Messages.MESSAGE_PREFIX + "&7Successfully removed the " + item.getItemMeta().getDisplayName() + " &7blueprint.";

			if (config.contains(item.getItemMeta().getDisplayName())) {
				config.set(item.getItemMeta().getDisplayName(), null);
				config.save(file);
				return success;
			} else
				return failure;
		} else
			return failure;
	}

	/*
	 * Returns a map of all blueprints.
	 */
	public HashMap<ItemStack, String> listBlueprints() throws IOException, InvalidConfigurationException {
		listBlueprintMap.clear();
		config.load(file);

		for (String itemName : config.getKeys(false)) {
			listBlueprintMap.put(config.getConfigurationSection(itemName).getItemStack("Blueprint"), config.getConfigurationSection(itemName).getString("Schematic"));
		}
		return listBlueprintMap;

	}

	/*
	 * Gets the blueprint item for a given schematic file. If there are multiple blueprints
	 * using one file, then both are returned.
	 */
	public List<ItemStack> getBlueprint(String schematic) throws IOException, InvalidConfigurationException {
		ArrayList<ItemStack> returnItem = new ArrayList<>();
		config.load(file);

		for (String itemName : config.getKeys(false)) {
			if (config.getConfigurationSection(itemName).getString("Schematic").equals(schematic))
				returnItem.add(config.getConfigurationSection(itemName).getItemStack("Blueprint"));
		}
		return returnItem;
	}
}
