package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.Blueprint;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

public class BlueprintsMigrateCommand extends SimpleSubCommand {
    public BlueprintsMigrateCommand(final SimpleCommandGroup parent) {
        super(parent, "migrate");
        setPermission("blueprints.migrate");
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        
        Player player = getPlayer();
        
        if (!BlueprintsPlugin.BLUEPRINTS_FILE.exists()) {
            player.sendMessage(Settings.Messages.MESSAGE_PREFIX + "&eThe blueprints.yml file does not exist, and there is nothing to migrate.");
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(BlueprintsPlugin.BLUEPRINTS_FILE);
        
        config.getKeys(false).stream().filter(key -> !key.equalsIgnoreCase("blueprints") && !key.equalsIgnoreCase("playerBlueprints")).forEach(key ->
                BlueprintsPlugin.instance.addBlueprint(new Blueprint(config.getConfigurationSection(key).getItemStack("Blueprint"),
                        config.getConfigurationSection(key).getString("Schematic"),
                        config.getConfigurationSection(key).contains("Type") ? config.getConfigurationSection(key).getString("Type") : null)));
        
        tell(Settings.Messages.MESSAGE_PREFIX + "&aBlueprints data has been successfully migrated!");
    }
}
