package me.sizzlemcgrizzle.blueprints.gui;

import de.craftlancer.core.gui.PageItem;
import de.craftlancer.core.gui.PagedListGUIInventory;
import de.craftlancer.core.util.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PlayerBlueprintMenu extends PagedListGUIInventory {
    
    private UUID owner;
    
    public PlayerBlueprintMenu(@Nonnull Plugin plugin, @Nullable String title, boolean useBorders, int rows, @Nonnull List<PageItem> pageItems, boolean playSounds, UUID owner) {
        super(plugin, title, useBorders, rows, pageItems, playSounds);
        
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
