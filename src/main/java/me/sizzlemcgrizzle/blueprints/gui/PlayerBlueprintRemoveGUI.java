package me.sizzlemcgrizzle.blueprints.gui;

import de.craftlancer.core.Utils;
import de.craftlancer.core.gui.GUIInventory;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerBlueprintRemoveGUI {
    
    private PlayerBlueprint blueprint;
    private Player player;
    private GUIInventory inventory;
    
    public PlayerBlueprintRemoveGUI(Player player, PlayerBlueprint blueprint) {
        this.player = player;
        this.blueprint = blueprint;
    }
    
    private void createInventory() {
        inventory = new GUIInventory(BlueprintsPlugin.instance, ChatColor.DARK_RED + "Remove Blueprint?");
        inventory.fill(Utils.buildItemStack(Material.BLACK_STAINED_GLASS_PANE, net.md_5.bungee.api.ChatColor.BLACK + "", Collections.emptyList()));
        
        inventory.setItem(13, blueprint.getItem());
        
        List<Integer> confirmBlockSlots = Arrays.asList(19, 20, 21, 28, 29, 30, 37, 38, 39);
        List<Integer> denyBlockSlots = Arrays.asList(23, 24, 25, 32, 33, 34, 41, 42, 43);
        
        ItemStack confirmItem = getConfirmItem();
        ItemStack denyItem = getDenyItem();
        
        confirmBlockSlots.forEach(slot -> {
            inventory.setItem(slot, confirmItem);
            inventory.setClickAction(slot, () -> {
                blueprint.remove();
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1F);
                Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&aYou have removed this blueprint!");
            });
        });
        
        denyBlockSlots.forEach(slot -> {
            inventory.setItem(slot, denyItem);
            inventory.setClickAction(slot, () -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.5F, 1F);
            });
        });
        
    }
    
    private ItemStack getConfirmItem() {
        ItemStack confirmItem = Utils.buildItemStack(Material.EMERALD_BLOCK, ChatColor.GREEN + "" + ChatColor.BOLD + "YES", Collections.emptyList());
        confirmItem.addUnsafeEnchantment(Enchantment.MENDING, 1);
        
        ItemMeta meta = confirmItem.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        confirmItem.setItemMeta(meta);
        return confirmItem;
    }
    
    private ItemStack getDenyItem() {
        ItemStack denyItem = Utils.buildItemStack(Material.REDSTONE_BLOCK, ChatColor.RED + "" + ChatColor.BOLD + "NO", Collections.emptyList());
        denyItem.addUnsafeEnchantment(Enchantment.MENDING, 1);
        
        ItemMeta meta = denyItem.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        denyItem.setItemMeta(meta);
        return denyItem;
    }
    
    public void display(Player player) {
        createInventory();
        
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 2F);
        player.openInventory(inventory.getInventory());
    }
}
