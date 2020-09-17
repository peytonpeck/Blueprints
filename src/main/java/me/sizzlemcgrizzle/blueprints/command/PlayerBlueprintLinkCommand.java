package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.InventoryLink;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerBlueprintLinkCommand extends SimpleSubCommand {
    
    protected PlayerBlueprintLinkCommand(SimpleCommandGroup parent) {
        super(parent, "link");
        setPermission("blueprints.create");
        setDescription("Links chests together for blueprint placement");
        setMinArguments(1);
    }
    
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1)
            return completeLastWord(Arrays.asList("create", "add", "remove", "removeLink"));
        return Collections.emptyList();
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        Player player = getPlayer();
        
        if (args[0].equalsIgnoreCase("add"))
            add(player);
        else if (args[0].equalsIgnoreCase("create"))
            create(player);
        else if (args[0].equalsIgnoreCase("remove"))
            remove(player);
        else if (args[0].equalsIgnoreCase("removeLink"))
            removeLink(player);
        else
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must enter a valid argument!");
    }
    
    private void create(Player player) {
        if (BlueprintsPlugin.getInstance().getLink(player).isPresent()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou already have an active link! To remove, &4/playerblueprint link remove&c!");
            return;
        }
        
        BlueprintsPlugin.getInstance().addInventoryLink(new InventoryLink(player));
        tell(Settings.Messages.MESSAGE_PREFIX + "&aInventory link successfully created. &3/playerblueprint link add &awhile looking at a barrel or shulker box!");
    }
    
    private void add(Player player) {
        Block block = player.getTargetBlockExact(5);
        
        if (block == null || (block.getType() != Material.BARREL && !block.getType().name().contains("SHULKER_BOX"))) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must look at a barrel or a shulker box!");
            return;
        }
        
        if (!BlueprintsPlugin.isTrusted(player, block.getLocation())) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou are not trusted here!");
            return;
        }
        
        if (BlueprintsPlugin.isInRegion(player, block.getLocation())) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou cannot add a link in an admin claim!");
            return;
        }
        
        
        if (!BlueprintsPlugin.getInstance().getLink(player).isPresent()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must first create a link! &4/playerblueprint link create");
            return;
        }
        
        InventoryLink link = BlueprintsPlugin.getInstance().getLink(player).get();
        Inventory inventory;
        if (block.getType().name().contains("SHULKER_BOX"))
            inventory = ((ShulkerBox) block.getState()).getInventory();
        else
            inventory = ((Barrel) block.getState()).getInventory();
        
        if (!link.add(inventory))
            tell(Settings.Messages.MESSAGE_PREFIX + "&cThis chest is already linked! &4/playerblueprint link remove");
        else
            tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully linked inventory!");
    }
    
    private void remove(Player player) {
        Block block = player.getTargetBlockExact(5);
        
        if (block == null || (block.getType() != Material.BARREL && !block.getType().name().contains("SHULKER_BOX"))) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must look at a barrel or shulker box!");
            return;
        }
        
        if (!BlueprintsPlugin.getInstance().getLink(player).isPresent()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must first create a link! &4/playerblueprint link create");
            return;
        }
        
        InventoryLink link = BlueprintsPlugin.getInstance().getLink(player).get();
        Inventory inventory = ((Chest) block.getState()).getInventory();
        
        if (!link.remove(inventory))
            tell(Settings.Messages.MESSAGE_PREFIX + "&cThis barrel/shulker box is not linked.");
        else
            tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully unlinked inventory!");
    }
    
    private void removeLink(Player player) {
        if (!BlueprintsPlugin.getInstance().getLink(player).isPresent()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou do not have an active link!");
            return;
        }
        
        BlueprintsPlugin.getInstance().removeInventoryLink(player);
        tell(Settings.Messages.MESSAGE_PREFIX + "&aInventory link successfully removed.");
    }
}
