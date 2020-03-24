package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.plugin.SimplePlugin;

import java.io.File;
import java.io.IOException;

public class BlueprintsSettimerCommand extends SimpleSubCommand {
	public BlueprintsSettimerCommand(final SimpleCommandGroup parent) {
		super(parent, "settimer");
		setMinArguments(1);
		setUsage("<seconds>");
		setPermission("blueprints.settimer");
	}

	@Override
	protected void onCommand() {
		try {
			Integer.valueOf(args[0]);
		} catch (NumberFormatException e) {
			tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must input a number!");
			return;
		}
		try {
			File file = new File(BlueprintsPlugin.getData().getAbsolutePath() + File.separator + "/settings.yml");
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);

			config.getConfigurationSection("Block").set("Bossbar_Duration", args[0]);
			config.save(file);

			tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully changed timer to " + args[0] + " seconds");

			SimplePlugin.getInstance().reload();

		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}
}
