package me.sizzlemcgrizzle.blueprints.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.InventoryLink;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BlueprintsPlayerLinkAddCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsPlayerLinkAddCommand(BlueprintsPlugin plugin) {
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
        
        Block block = player.getTargetBlockExact(5);
        
        if (block == null || (block.getType() != Material.BARREL && !block.getType().name().contains("SHULKER_BOX"))) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must look at a barrel or a shulker box.");
            return null;
        }
        
        if (!player.isOp() && !Utils.isTrusted(player.getUniqueId(), block.getLocation(), ClaimPermission.Build)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You are not trusted here.");
            return null;
        }
        
        if (!player.isOp() && Utils.isInAdminRegion(block.getLocation())) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You cannot add a link in an admin claim.");
            return null;
        }
        
        
        if (!plugin.getLink(player).isPresent()) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must first create a link! (Relog)");
            return null;
        }
        
        InventoryLink link = plugin.getLink(player).get();
        Inventory inventory;
        if (block.getType().name().contains("SHULKER_BOX"))
            inventory = ((ShulkerBox) block.getState()).getInventory();
        else
            inventory = ((Barrel) block.getState()).getInventory();
        
        if (!link.add(inventory))
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "This chest is already linked! §6/playerblueprint link remove");
        else
            MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully linked inventory!");
        
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
