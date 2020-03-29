package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BlueprintsAddtypeCommand extends SimpleSubCommand {
	private File file = new File(BlueprintsPlugin.getData().getAbsolutePath() + File.separator + "/settings.yml");
	private YamlConfiguration config = new YamlConfiguration();

	public BlueprintsAddtypeCommand(final SimpleCommandGroup parent) {
		super(parent, "addtype");
		setMinArguments(1);
		setPermission("blueprints.addtype");
	}

	@Override
	protected void onCommand() {
		checkConsole();

		try {
			config.load(file);

			List<String> types = config.getStringList("Blueprint_Types");
			types.add(args[0].toUpperCase());
			config.set("Blueprint_Types", types);
			tell(Settings.Messages.MESSAGE_PREFIX + "&7You have added type &a" + args[0].toUpperCase() + " &7to the type list! Use &6/blueprints reload&7 to reload changes.");
			config.save(file);

		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}


	}
}
