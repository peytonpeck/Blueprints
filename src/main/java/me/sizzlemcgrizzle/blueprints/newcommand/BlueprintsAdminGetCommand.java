package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.Blueprint;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BlueprintsAdminGetCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsAdminGetCommand(BlueprintsPlugin plugin) {
        super("blueprints.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 3)
            return Utils.getMatches(args[2], plugin.getBlueprints().stream().filter(blueprint -> !(blueprint instanceof PlayerBlueprint)).map(Blueprint::getSchematic).collect(Collectors.toList()));
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        Player player = (Player) sender;
        
        List<ItemStack> itemList = plugin.getBlueprints().stream().filter(blueprint -> blueprint.getSchematic().equalsIgnoreCase(args[2])).map(Blueprint::getItem).collect(Collectors.toList());
        if (itemList.size() == 0)
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "There are no blueprints using this schematic.");
        else {
            if (itemList.size() > 1)
                MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "There are multiple blueprints using this schematic, you have been given all of them.");
            for (ItemStack item : itemList) {
                player.getInventory().addItem(item);
                MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "You have been given " + item.getItemMeta().getDisplayName() + " §eblueprint.");
            }
        }
        
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
