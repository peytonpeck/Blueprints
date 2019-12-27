package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.settings.SchematicCache;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.io.IOException;
import java.util.List;

public class BlueprintsGetCommand extends SimpleSubCommand {
	protected BlueprintsGetCommand(SimpleCommandGroup parent) {
		super(parent, "get");
		setMinArguments(1);
		setUsage("<schematicname.schematic>");
		setPermission("blueprints.get");
	}

	@Override
	protected void onCommand() {
		try {
			List<ItemStack> itemList = new SchematicCache().getBlueprint(args[0]);
			if (itemList == null)
				tell(Settings.Messages.MESSAGE_PREFIX + "&cThere are no blueprints using this schematic. Check a list of schematics with &4/blueprints list&c.");
			else {
				if (itemList.size() > 1)
					tell(Settings.Messages.MESSAGE_PREFIX + "&eThere are multiple blueprints using this schematic so here are all of them");
				for (ItemStack item : itemList) {
					getPlayer().getInventory().addItem(item);
					tell(Settings.Messages.MESSAGE_PREFIX + "&7You have been given " + item.getItemMeta().getDisplayName() + " &7blueprint.");
				}
			}
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
