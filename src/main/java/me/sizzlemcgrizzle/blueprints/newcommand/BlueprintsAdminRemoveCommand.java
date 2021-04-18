package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlueprintsAdminRemoveCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsAdminRemoveCommand(BlueprintsPlugin plugin) {
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
        }
        Player player = (Player) sender;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType().isAir() || plugin.getBlueprints().stream().noneMatch(blueprint -> blueprint.getItem().isSimilar(item)))
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must hold a blueprint.");
        else {
            plugin.getBlueprints().removeIf(b -> b.compareItem(item));
            MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Blueprint removed.");
        }
        
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
