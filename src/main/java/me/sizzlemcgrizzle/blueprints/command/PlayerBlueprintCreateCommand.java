package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintCreationSession;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

public class PlayerBlueprintCreateCommand extends SimpleSubCommand {
    
    protected PlayerBlueprintCreateCommand(SimpleCommandGroup parent) {
        super(parent, "create");
        setPermission("blueprints.create");
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        Player player = getPlayer();
        
        if (player.hasMetadata("blueprint_create")) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cPlease complete your current blueprint creation session to make a new one.");
            return;
        }
        
        player.setMetadata("blueprint_create", new FixedMetadataValue(BlueprintsPlugin.instance, true));
        tell(Settings.Messages.MESSAGE_PREFIX + "&aPlace two blocks. Left click a block with no item in hand to set position 1, right click the other block to set position 2. When done, type &2/blueprints complete&a.");
        
        BlueprintsPlugin.instance.addCreationSession(player, new BlueprintCreationSession(player));
    }
}
