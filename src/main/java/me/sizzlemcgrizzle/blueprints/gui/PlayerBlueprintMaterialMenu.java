package me.sizzlemcgrizzle.blueprints.gui;

import de.craftlancer.core.Utils;
import de.craftlancer.core.gui.NavigationItem;
import de.craftlancer.core.gui.PageItem;
import de.craftlancer.core.gui.PagedListGUIInventory;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class PlayerBlueprintMaterialMenu extends PagedListGUIInventory {
    public PlayerBlueprintMaterialMenu(@Nonnull Plugin plugin, @Nullable String title, boolean useBorders, int rows, @Nonnull List<PageItem> pageItems, boolean playSounds, PlayerBlueprintMenu menu) {
        super(plugin, title, useBorders, rows, pageItems, playSounds);
        
        setInfoItem(makeInfoItem());
        
        NavigationItem navigationItem = new NavigationItem(Utils.buildItemStack(Material.ENDER_EYE, ChatColor.GOLD + "Back to Main Page", Collections.emptyList()), -1);
        navigationItem.setClickAction(player -> BlueprintsPlugin.getInstance().getPlayerBlueprintListGUIFor(player.getUniqueId()));
        
        addNavigationItem(navigationItem);
    }
    
    private ItemStack makeInfoItem() {
        return new ItemBuilder(Material.STONE).setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "What is this?")
                .setLore("",
                        ChatColor.GRAY + "This page shows all the materials",
                        ChatColor.GRAY + "involved in creating this player",
                        ChatColor.GRAY + "blueprint.")
                .setCustomModelData(5)
                .build();
    }
}
