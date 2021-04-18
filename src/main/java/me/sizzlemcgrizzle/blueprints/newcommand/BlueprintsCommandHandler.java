package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.command.CommandHandler;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;

public class BlueprintsCommandHandler extends CommandHandler {
    public BlueprintsCommandHandler(BlueprintsPlugin plugin) {
        super(plugin);
        
        //admin
        registerSubCommand("admin", new BlueprintsAdminCommandHandler(plugin), "a");
        
        //player
        registerSubCommand("list", new BlueprintsPlayerListCommand(plugin));
        registerSubCommand("complete", new BlueprintsPlayerCompleteCommand(plugin));
        registerSubCommand("materials", new BlueprintsPlayerMaterialCommand(plugin));
        registerSubCommand("link", new BlueprintsPlayerLinkCommand(plugin));
    }
}
