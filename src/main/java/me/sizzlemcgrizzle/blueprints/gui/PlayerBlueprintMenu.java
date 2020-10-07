package me.sizzlemcgrizzle.blueprints.gui;

import de.craftlancer.core.gui.PagedListGUIInventory;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerBlueprintMenu extends PagedListGUIInventory {
    
    private UUID owner;
    
    public PlayerBlueprintMenu(UUID owner) {
        super(BlueprintsPlugin.getInstance(),
                ChatColor.DARK_PURPLE + Bukkit.getOfflinePlayer(owner).getName() + "'s Player Blueprints",
                false,
                2,
                PlayerBlueprint.getPageItems(owner),
                true);
        
        this.owner = owner;
        
        setInfoItem(makeInfoItem());
    }
    
    private ItemStack makeInfoItem() {
        return new ItemBuilder(Material.STONE).setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "What is this?")
                .setLore("",
                        ChatColor.GRAY + "This page shows all the player",
                        ChatColor.GRAY + "blueprints you own. If you don't",
                        ChatColor.GRAY + "have any, you won't see any!",
                        "",
                        ChatColor.AQUA + "Left click" + ChatColor.GRAY + " to get a blueprint",
                        ChatColor.AQUA + "Right click" + ChatColor.GRAY + " to open the material list.",
                        ChatColor.AQUA + "Shift left click" + ChatColor.GRAY + " to " + ChatColor.RED + "remove" + ChatColor.GRAY + " blueprint.")
                .setCustomModelData(5)
                .build();
    }
    
    public UUID getOwner() {
        return owner;
    }
}
