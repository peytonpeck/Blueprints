package me.sizzlemcgrizzle.blueprints.gui;

import de.craftlancer.core.Utils;
import de.craftlancer.core.gui.GUIInventory;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerBlueprintRemoveGUI {
    
    private Map<UUID, PlayerBlueprint> playerBlueprintMap = new HashMap<>();
    private GUIInventory inventory;
    
    public void display(Player player, PlayerBlueprint blueprint) {
        playerBlueprintMap.put(player.getUniqueId(), blueprint);
        
        if (inventory == null)
            createInventory();
        
        player.openInventory(inventory.getInventory());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 2F);
    }
    
    private void createInventory() {
        inventory = new GUIInventory(BlueprintsPlugin.getInstance(), ChatColor.DARK_RED + "Remove Blueprint?", 1);
        inventory.fill(Utils.buildItemStack(Material.BLACK_STAINED_GLASS_PANE, net.md_5.bungee.api.ChatColor.BLACK + "", Collections.emptyList()));
        
        List<Integer> confirmBlockSlots = Arrays.asList(0, 1, 2, 3);
        List<Integer> denyBlockSlots = Arrays.asList(5, 6, 7, 8);
        
        confirmBlockSlots.forEach(slot -> {
            inventory.setItem(slot, new ItemBuilder(Material.EMERALD_BLOCK)
                    .setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "YES")
                    .setEnchantmentGlow(true).build());
            inventory.setClickAction(slot, player -> {
                playerBlueprintMap.get(player.getUniqueId()).remove();
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1F);
                Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&aYou have removed this blueprint!");
            });
        });
        
        denyBlockSlots.forEach(slot -> {
            inventory.setItem(slot, new ItemBuilder(Material.REDSTONE_BLOCK)
                    .setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "NO")
                    .setEnchantmentGlow(true).build());
            inventory.setClickAction(slot, player -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.5F, 1F);
            });
        });
        
    }
}
