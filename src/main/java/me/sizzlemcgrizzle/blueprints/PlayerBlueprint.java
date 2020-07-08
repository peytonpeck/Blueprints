package me.sizzlemcgrizzle.blueprints;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.session.ClipboardHolder;
import de.craftlancer.core.Utils;
import de.craftlancer.core.gui.GUIInventory;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintListGUI;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerBlueprint extends Blueprint {
    
    private Map<Material, Integer> materialMap = new HashMap<>();
    private UUID owner;
    private GUIInventory inventory;
    private double cost;
    
    public PlayerBlueprint(ItemStack item, String schematic, String type, UUID owner, Map<Material, Integer> materialMap) {
        super(item, schematic, type);
        
        this.owner = owner;
        this.materialMap = materialMap;
        setCost();
    }
    
    public PlayerBlueprint(Map<String, Object> map) {
        super(map);
        
        this.owner = UUID.fromString((String) map.get("owner"));
        this.cost = (double) map.get("cost");
        
        map.forEach((k, v) -> {
            try {
                materialMap.put(Material.valueOf(k), (Integer) v);
            } catch (IllegalArgumentException ignored) {
            
            }
        });
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("owner", owner.toString());
        map.put("cost", cost);
        materialMap.forEach((k, v) -> map.put(k.toString(), v));
        
        return map;
    }
    
    @Override
    protected void getClipboardFromSchematic() {
        File file = new File(BlueprintsPlugin.instance.getDataFolder() + File.separator + "/playerblueprints" + File.separator + "/" + getSchematic());
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            setClipboard(reader.read());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public ClipboardHolder getHolder(Player player) {
        Clipboard copy = getClipboard();
        return new ClipboardHolder(copy);
    }
    
    private void setCost() {
        cost = 0;
        materialMap.values().forEach(integer -> cost += Settings.PLAYER_BLUEPRINT_PRICE_MODIFIER * integer);
    }
    
    private void createInventory() {
        inventory = new GUIInventory(BlueprintsPlugin.instance, ChatColor.DARK_PURPLE + "Blueprint Material List");
        inventory.fill(Utils.buildItemStack(Material.BLACK_STAINED_GLASS_PANE, net.md_5.bungee.api.ChatColor.BLACK + "", Collections.emptyList()));
        
        int slot = 10;
        for (Map.Entry<Material, Integer> entry : materialMap.entrySet()) {
            if (entry.getKey() == Material.AIR)
                continue;
            String name = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "ITEM: " + ChatColor.AQUA + entry.getKey().name().toLowerCase().replace('_', ' ');
            List<String> lore = Arrays.asList("", ChatColor.GRAY + "Amount needed: " + ChatColor.GREEN + entry.getValue());
            ItemStack item = Utils.buildItemStack(entry.getKey(), name, lore);
            item.setAmount(Math.min(item.getMaxStackSize(), entry.getValue()));
            
            inventory.setItem(slot, item);
            
            if (slot % 9 == 7)
                slot += 3;
            else
                slot++;
        }
        
        inventory.setItem(49, getQuestionMarkItem());
        
        inventory.setItem(53, Utils.buildItemStack(Material.ENDER_EYE, ChatColor.GOLD + "Back to Main Page", Collections.emptyList()));
        inventory.setClickAction(53, () -> new PlayerBlueprintListGUI(Bukkit.getPlayer(owner)).display(Bukkit.getPlayer(owner)));
    }
    
    public void display(Player player) {
        if (inventory == null)
            createInventory();
        
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 2F);
        player.openInventory(inventory.getInventory());
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public double getCost() {
        return cost;
    }
    
    public Map<Material, Integer> getMaterialMap() {
        return materialMap;
    }
    
    private ItemStack getQuestionMarkItem() {
        ItemStack questionMarkItem = Utils.buildItemStack(Material.STONE, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "What is this?",
                Arrays.asList("",
                        ChatColor.GRAY + "This page shows all the materials",
                        ChatColor.GRAY + "needed for making the blueprint",
                        ChatColor.GRAY + "you clicked on.",
                        ""));
        
        ItemMeta m = questionMarkItem.getItemMeta();
        m.setCustomModelData(5);
        questionMarkItem.setItemMeta(m);
        
        return questionMarkItem;
    }
    
    public void remove() {
        owner = UUID.randomUUID();
    }
    
    public static int getAmount(Player player) {
        return (int) BlueprintsPlugin.instance.getPlayerBlueprints().stream().filter(b -> b.getOwner().equals(player.getUniqueId())).count();
    }
    
    public static int getLimit(Player player) {
        int limit = -1;
        
        for (String l : Settings.LIMITS) {
            if (!player.hasPermission(l))
                continue;
            
            int num = Integer.parseInt(l.split("blueprint.placement.")[1]);
            
            if (num == -1)
                return -1;
            if (num > limit)
                limit = num;
        }
        return limit;
    }
}
