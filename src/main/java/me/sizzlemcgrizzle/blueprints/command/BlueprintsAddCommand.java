package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.settings.SchematicCache;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.io.IOException;

public class BlueprintsAddCommand extends SimpleSubCommand {
	public BlueprintsAddCommand(final SimpleCommandGroup parent) {
		super(parent, "add");
		setMinArguments(1);
		setUsage("<schematicname.schematic>");
		setPermission("blueprints.add");
	}

	@Override
	protected void onCommand() {
		checkConsole();
		final ItemStack blueprint = getPlayer().getInventory().getItemInMainHand();

		if (args.length == 1) {
			try {
				Common.tell(getPlayer(), new SchematicCache().addBlueprint(args[0], blueprint));
				new SchematicCache().getSchematicFor(getPlayer().getInventory().getItemInMainHand());

			} catch (final IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
}