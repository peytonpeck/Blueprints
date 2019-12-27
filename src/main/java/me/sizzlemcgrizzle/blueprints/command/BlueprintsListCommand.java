package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.settings.SchematicCache;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.model.SimpleComponent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlueprintsListCommand extends SimpleSubCommand {
	protected BlueprintsListCommand(SimpleCommandGroup parent) {
		super(parent, "list");
		setPermission("blueprints.list");
	}

	@Override
	protected void onCommand() {

		try {

			HashMap<ItemStack, String> map = new SchematicCache().listBlueprints();


			if (map != null && map.size() != 0) {
				tell(Settings.Messages.MESSAGE_PREFIX + "&6&oHover for more information.");

				int counter = 0;
				for (Map.Entry<ItemStack, String> entry : map.entrySet()) {
					counter++;
					SimpleComponent
							.of(Settings.Messages.MESSAGE_PREFIX)
							.append(" &e" + counter + ".  &f" + entry.getKey().getItemMeta().getDisplayName())
							.onHover("&5Click me to get the blueprint!\n\n&r&f" + entry.getKey().getItemMeta().getDisplayName() + "&7 uses \n&c" + entry.getValue() + "&7 as a schematic")
							.onClickRunCmd("/blueprints get " + entry.getValue())
							.send(getPlayer());
				}
			} else
				tell(Settings.Messages.MESSAGE_PREFIX + "&cThere are currently no blueprints.");
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
