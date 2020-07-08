package me.sizzlemcgrizzle.blueprints.gui;

import de.craftlancer.core.Utils;
import de.craftlancer.core.gui.GUIInventory;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerBlueprintListGUI {
    
    private Player player;
    private List<PlayerBlueprint> playerBlueprints;
    private GUIInventory inventory;
    private int page;
    
    public PlayerBlueprintListGUI(Player player) {
        this(player, 0);
    }
    
    public PlayerBlueprintListGUI(Player player, int page) {
        this.page = page;
        this.player = player;
        this.playerBlueprints = BlueprintsPlugin.instance.getPlayerBlueprints().stream().filter(b -> b.getOwner().equals(player.getUniqueId())).collect(Collectors.toList());
        
        createInventory();
    }
    
    public GUIInventory getInventory() {
        return inventory;
    }
    
    public void createInventory() {
        inventory = new GUIInventory(BlueprintsPlugin.instance, ChatColor.DARK_AQUA + "Player blueprints for: " + ChatColor.DARK_PURPLE + player.getName());
        inventory.fill(Utils.buildItemStack(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "", Collections.emptyList()));
        
        setBlueprintItems();
        
        setBackwardsArrowItem();
        inventory.setItem(49, getQuestionMarkItem());
        setForwardsArrowItem();
    }
    
    private void setBackwardsArrowItem() {
        if (page == 0)
            inventory.setItem(48, Utils.buildItemStack(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "", Collections.emptyList()));
        else {
            inventory.setItem(48, Utils.buildItemStack(Material.ARROW, ChatColor.GOLD + "Back", Collections.emptyList()));
            inventory.setClickAction(48, () -> new PlayerBlueprintListGUI(player, page - 1).display(player));
        }
    }
    
    private void setForwardsArrowItem() {
        if ((page + 1) * 28 >= playerBlueprints.size())
            inventory.setItem(50, Utils.buildItemStack(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "", Collections.emptyList()));
        else {
            inventory.setItem(50, Utils.buildItemStack(Material.ARROW, ChatColor.GOLD + "Forward", Collections.emptyList()));
            inventory.setClickAction(50, () -> new PlayerBlueprintListGUI(player, page + 1).display(player));
        }
    }
    
    private ItemStack getQuestionMarkItem() {
        ItemStack questionMarkItem = Utils.buildItemStack(Material.STONE, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "What is this?",
                Arrays.asList("",
                        ChatColor.GRAY + "This page shows all the player",
                        ChatColor.GRAY + "blueprints you own. If you don't",
                        ChatColor.GRAY + "have any, you won't see any!",
                        "",
                        ChatColor.AQUA + "Left click" + ChatColor.GRAY + " to get a blueprint",
                        ChatColor.AQUA + "Right click" + ChatColor.GRAY + " to open the material list.",
                        ChatColor.AQUA + "Shift left click" + ChatColor.GRAY + " to " + ChatColor.RED + "remove" + ChatColor.GRAY + " blueprint."));
        
        ItemMeta m = questionMarkItem.getItemMeta();
        m.setCustomModelData(5);
        questionMarkItem.setItemMeta(m);
        
        return questionMarkItem;
    }
    
    private void setBlueprintItems() {
        
        List<PlayerBlueprint> list = new ArrayList<>();
        for (int i = page * 28; i < (page + 1) * 28; i++) {
            if (playerBlueprints.size() > i)
                list.add(playerBlueprints.get(i));
            else
                break;
        }
        
        int slot = 10;
        for (PlayerBlueprint blueprint : list) {
            ItemStack item = blueprint.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            
            lore.add("");
            lore.add(ChatColor.AQUA + "Left click" + ChatColor.GRAY + " to " + ChatColor.GREEN + "buy" + ChatColor.GRAY + " item for " + ChatColor.GREEN + "$" + blueprint.getCost() + ChatColor.GRAY + ".");
            lore.add(ChatColor.AQUA + "Right click" + ChatColor.GRAY + " to " + ChatColor.LIGHT_PURPLE + "open" + ChatColor.GRAY + " material list.");
            lore.add(ChatColor.AQUA + "Shift left click" + ChatColor.GRAY + " to " + ChatColor.RED + "remove" + ChatColor.GRAY + " blueprint.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inventory.setItem(slot, item);
            inventory.setClickAction(slot, () -> {
                if (!charge(blueprint))
                    return;
                player.getInventory().addItem(blueprint.getItem());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 0.5F, 2F);
            }, ClickType.LEFT);
            inventory.setClickAction(slot, () -> blueprint.display(player), ClickType.RIGHT);
            inventory.setClickAction(slot, () -> new PlayerBlueprintRemoveGUI(player, blueprint).display(player), ClickType.SHIFT_LEFT);
            
            if (slot % 9 == 7)
                if (slot == 43)
                    break;
                else
                    slot += 3;
            else
                slot++;
        }
    }
    
    private boolean charge(PlayerBlueprint blueprint) {
        if (!Settings.USE_ECONOMY)
            return true;
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        
        if (!BlueprintsPlugin.getEconomy().has(offlinePlayer, blueprint.getCost())) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.5F, 1F);
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eYou cannot afford the &6$" + blueprint.getCost() + " &efor this blueprint!");
            return false;
        } else {
            BlueprintsPlugin.getEconomy().withdrawPlayer(offlinePlayer, blueprint.getCost());
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&aYou have purchased this blueprint for &2$" + blueprint.getCost() + "&a!");
            return true;
        }
    }
    
    public void display(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 2F);
        createInventory();
        player.openInventory(inventory.getInventory());
    }
    
    
}
