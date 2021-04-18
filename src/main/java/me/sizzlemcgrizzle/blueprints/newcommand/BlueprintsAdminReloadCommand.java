package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BlueprintsAdminReloadCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsAdminReloadCommand(BlueprintsPlugin plugin) {
        super("blueprints.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return super.onTabComplete(sender, args);
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        Settings.load(plugin);
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, Settings.Messages.RELOAD_SUCCESS);
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
