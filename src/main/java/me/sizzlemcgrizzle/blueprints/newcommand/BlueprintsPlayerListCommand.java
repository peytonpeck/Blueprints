package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlueprintsPlayerListCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsPlayerListCommand(BlueprintsPlugin plugin) {
        super("", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        Player player = (Player) sender;
        
        plugin.getPlayerBlueprintMenu(player.getUniqueId()).display(player);
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
