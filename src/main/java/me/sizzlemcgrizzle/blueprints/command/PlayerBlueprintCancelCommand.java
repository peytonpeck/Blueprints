package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

public class PlayerBlueprintCancelCommand extends SimpleSubCommand {
    
    protected PlayerBlueprintCancelCommand(SimpleCommandGroup parent) {
        super(parent, "cancel");
        setPermission("blueprints.create");
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        Player player = getPlayer();
        
        if (!player.hasMetadata("blueprint_create")) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou are not in a blueprint creation session.");
            return;
        }
        
        BlueprintsPlugin.instance.removeCreationSession(player);
        
        player.removeMetadata("blueprint_create", BlueprintsPlugin.instance);
        tell(Settings.Messages.MESSAGE_PREFIX + "&eBlueprint creation session cancelled.");
    }
}
