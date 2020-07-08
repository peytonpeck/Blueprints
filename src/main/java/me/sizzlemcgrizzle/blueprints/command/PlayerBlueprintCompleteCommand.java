package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintCreationSession;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.Collections;
import java.util.List;

public class PlayerBlueprintCompleteCommand extends SimpleSubCommand {
    
    protected PlayerBlueprintCompleteCommand(SimpleCommandGroup parent) {
        super(parent, "complete");
        setPermission("blueprints.create");
        setDescription("Completes the blueprint creation session");
        setUsage("<name>");
    }
    
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1)
            return Collections.singletonList("name");
        return Collections.emptyList();
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        Player player = getPlayer();
        
        if (!player.hasMetadata("blueprint_create")) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou are not in a blueprint creation session.");
            return;
        }
        
        if (PlayerBlueprint.getLimit(player) != -1 && PlayerBlueprint.getAmount(player) >= PlayerBlueprint.getLimit(player)) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou already have the maximum amount of player blueprints possible!");
            return;
        }
        
        BlueprintCreationSession session = BlueprintsPlugin.instance.getCreationSession(player);
        
        int area = (int) session.getArea();
        if (area > Settings.PLAYER_BLUEPRINT_MAX_SIZE) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cThe size of this blueprint is too big! Your size: &6" + area + "&c, maximum size: &a" + Settings.PLAYER_BLUEPRINT_MAX_SIZE + "&c.");
            return;
        }
        
        if (args.length < 1) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cPlease enter the name of the blueprint (color codes are acceptable!)");
            return;
        }
        
        player.removeMetadata("blueprint_create", BlueprintsPlugin.instance);
        
        if (session.getPosition1().getWorld() != session.getPosition2().getWorld()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cThe two positions are not in the same world!");
            return;
        }
        
        session.complete(args[0]);
        tell(Settings.Messages.MESSAGE_PREFIX + "&eBlueprint creation session completed.");
    }
}
