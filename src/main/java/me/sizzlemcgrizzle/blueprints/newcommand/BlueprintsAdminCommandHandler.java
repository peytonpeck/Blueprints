package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.command.SubCommandHandler;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.command.CommandSender;

public class BlueprintsAdminCommandHandler extends SubCommandHandler {
    
    public BlueprintsAdminCommandHandler(BlueprintsPlugin plugin) {
        super("blueprints.admin", plugin, false, 1);
        
        registerSubCommand("create", new BlueprintsAdminCreateCommand(plugin));
        registerSubCommand("list", new BlueprintsAdminListCommand(plugin));
        registerSubCommand("reload", new BlueprintsAdminReloadCommand(plugin));
        registerSubCommand("remove", new BlueprintsAdminRemoveCommand(plugin));
        registerSubCommand("get", new BlueprintsAdminGetCommand(plugin));
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
