package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.io.IOException;

public class BlueprintsRemoveCommand extends SimpleSubCommand {
	private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");

	protected BlueprintsRemoveCommand(SimpleCommandGroup parent) {
		super(parent, "remove");
		setPermission("blueprints.remove");
	}

	@Override
	protected void onCommand() {
		if (getPlayer().getInventory().getItemInMainHand().getType().isAir())
			tell(Settings.Messages.MESSAGE_PREFIX + "&cYou are not holding a block!");
		else
			try {
				Common.tell(getPlayer(), blueprintsPlugin.schematicCache().removeBlueprint(getPlayer().getInventory().getItemInMainHand()));
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
	}


}
