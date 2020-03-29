package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlueprintsAddCommand extends SimpleSubCommand {
	private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");

	public BlueprintsAddCommand(final SimpleCommandGroup parent) {
		super(parent, "add");
		setMinArguments(1);
		setUsage("<schematicname.schematic>");
		setPermission("blueprints.add");
	}

	@Override
	protected List<String> tabComplete() {
		if (args.length == 2)
			return completeLastWord(Settings.TYPES);
		return new ArrayList<>();
	}

	@Override
	protected void onCommand() {
		String type;

		checkConsole();

		if (args[1] == null)
			type = "NORMAL";
		else {
			type = args[1];
			if (!Settings.TYPES.contains(type)) {
				tell(Settings.Messages.MESSAGE_PREFIX + "&6" + args[1] + "&e is not a valid blueprint type! Use &6/blueprints addtype &eto add a type.");
				return;
			}

		}
		final ItemStack blueprint = getPlayer().getInventory().getItemInMainHand();

		try {
			Common.tell(getPlayer(), blueprintsPlugin.schematicCache().addBlueprint(args[0], blueprint, type));
			blueprintsPlugin.schematicCache().getSchematicFor(getPlayer().getInventory().getItemInMainHand());

		} catch (final IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}