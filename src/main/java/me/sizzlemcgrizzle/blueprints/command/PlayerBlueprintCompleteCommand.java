package me.sizzlemcgrizzle.blueprints.command;

import de.craftlancer.core.clipboard.Clipboard;
import de.craftlancer.core.clipboard.ClipboardManager;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprintUtil;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        Optional<Clipboard> optional = ClipboardManager.getInstance().getClipboard(player.getUniqueId());
        
        if (!optional.isPresent()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou do not have an active clipboard. Use &a/clipboard new &cto create a new clipboard.");
            return;
        }
        
        Clipboard clipboard = optional.get();
        
        if (PlayerBlueprint.getLimit(player) != -1 && PlayerBlueprint.getAmount(player) >= PlayerBlueprint.getLimit(player)) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou already have the maximum amount of player blueprints possible!");
            return;
        }
        
        int volume = (int) clipboard.getVolume();
        if (volume > Settings.PlayerBlueprint.PLAYER_BLUEPRINT_MAX_SIZE) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cThe size of this blueprint is too big! Your size: &6" + volume + "&c, maximum size: &a" + Settings.PlayerBlueprint.PLAYER_BLUEPRINT_MAX_SIZE + "&c.");
            return;
        }
        
        if (args.length < 1) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cPlease enter the name of the blueprint (color codes are acceptable!)");
            return;
        }
        
        if (!clipboard.hasTwoPoints()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYour clipboard does not have two points set.");
            return;
        }
        
        PlayerBlueprintUtil.complete(player, args[0], clipboard);
        tell(Settings.Messages.MESSAGE_PREFIX + "&eBlueprint creation session completed.");
    }
}
