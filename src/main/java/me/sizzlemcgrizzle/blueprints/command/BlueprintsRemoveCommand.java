package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.settings.Settings;
import me.sizzlemcgrizzle.blueprints.util.SchematicUtil;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

public class BlueprintsRemoveCommand extends SimpleSubCommand {
	
	private static String failure = Settings.Messages.MESSAGE_PREFIX + "&cA blueprint for this item does not exist.";
	private static String success = Settings.Messages.MESSAGE_PREFIX + "&7Blueprint has been successfully removed.";
	
	protected BlueprintsRemoveCommand(SimpleCommandGroup parent) {
		super(parent, "remove");
		setPermission("blueprints.remove");
	}
	
	@Override
	protected void onCommand() {
		checkConsole();
		if (getPlayer().getInventory().getItemInMainHand().getType().isAir())
			tell(Settings.Messages.MESSAGE_PREFIX + "&cYou are not holding a block!");
		else if (SchematicUtil.removeBlueprint(getPlayer().getInventory().getItemInMainHand()))
			tell(success);
		else
			tell(failure);
	}
	
	
}
