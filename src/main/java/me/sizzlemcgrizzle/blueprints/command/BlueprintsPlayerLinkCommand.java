package me.sizzlemcgrizzle.blueprints.command;

import de.craftlancer.core.command.SubCommandHandler;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.command.CommandSender;

public class BlueprintsPlayerLinkCommand extends SubCommandHandler {
    
    public BlueprintsPlayerLinkCommand(BlueprintsPlugin plugin) {
        super("", plugin, false, 1);
        
        registerSubCommand("add", new BlueprintsPlayerLinkAddCommand(plugin));
        registerSubCommand("remove", new BlueprintsPlayerLinkRemoveCommand(plugin));
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
