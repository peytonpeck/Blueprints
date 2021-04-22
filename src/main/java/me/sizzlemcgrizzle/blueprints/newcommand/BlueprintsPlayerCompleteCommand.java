package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.clipboard.Clipboard;
import de.craftlancer.core.clipboard.ClipboardManager;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprintUtil;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BlueprintsPlayerCompleteCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsPlayerCompleteCommand(BlueprintsPlugin plugin) {
        super("", plugin, false);
        
        this.plugin = plugin;
    }
    
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Collections.singletonList("name");
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        
        Player player = (Player) sender;
        Optional<Clipboard> optional = ClipboardManager.getInstance().getClipboard(player.getUniqueId());
        
        if (!optional.isPresent()) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have an active clipboard. Use " +
                    "§6/clipboard new §6cto create a new clipboard.");
            return null;
        }
        
        Clipboard clipboard = optional.get();
        
        if (PlayerBlueprint.getLimit(player) != -1 && PlayerBlueprint.getAmount(player) >= PlayerBlueprint.getLimit(player)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You already have the maximum amount of player blueprints possible!");
            return null;
        }
        
        int volume = (int) clipboard.getVolume();
        if (volume > Settings.PlayerBlueprint.PLAYER_BLUEPRINT_MAX_SIZE) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.WARNING, "The size of this blueprint is too big! Your size: " + volume + ", maximum size: " + Settings.PlayerBlueprint.PLAYER_BLUEPRINT_MAX_SIZE + ".");
            return null;
        }
        
        if (args.length < 2) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "Please enter the name of the blueprint (color codes are acceptable!)");
            return null;
        }
        
        if (!clipboard.hasTwoPoints()) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "Your clipboard does not have two points set.");
            return null;
        }
        
        PlayerBlueprintUtil.complete(player, args[1], clipboard);
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Blueprint creation session completed.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
