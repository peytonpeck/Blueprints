package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.settings.SchematicCache;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.io.IOException;

public class BlueprintsRemoveCommand extends SimpleSubCommand {
	protected BlueprintsRemoveCommand(SimpleCommandGroup parent) {
		super(parent, "remove");
		setPermission("blueprints.remove");
	}

	@Override
	protected void onCommand() {
		if (getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR))
			tell(Settings.Messages.MESSAGE_PREFIX + "&cYou are not holding a block!");
		else
			try {
				Common.tell(getPlayer(), new SchematicCache().removeBlueprint(getPlayer().getInventory().getItemInMainHand()));
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
	}


}
