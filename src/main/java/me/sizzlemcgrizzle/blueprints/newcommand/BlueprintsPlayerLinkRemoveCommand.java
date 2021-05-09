package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.InventoryLink;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Optional;

public class BlueprintsPlayerLinkRemoveCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsPlayerLinkRemoveCommand(BlueprintsPlugin plugin) {
        super("", plugin, false);
        
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
        
        Player player = (Player) sender;
        
        Block block = player.getTargetBlockExact(5);
        
        if (block == null || (block.getType() != Material.BARREL && !block.getType().name().contains("SHULKER_BOX"))) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must look at a barrel or shulker box.");
            return null;
        }
        
        Optional<InventoryLink> optional = plugin.getLink(player);
        
        if (!optional.isPresent()) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must first create a link. (Relog)");
            return null;
        }
        
        InventoryLink link = optional.get();
        Inventory inventory = ((Container) block.getState()).getInventory();
        
        if (!link.remove(inventory))
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "This barrel/shulker box is not linked.");
        else
            MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully unlinked inventory.");
        
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
