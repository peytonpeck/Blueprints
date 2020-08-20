package me.sizzlemcgrizzle.blueprints.placement;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.session.ClipboardHolder;
import de.craftlancer.core.Utils;
import de.craftlancer.core.gui.PageItem;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintMaterialMenu;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintMenu;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintRemoveGUI;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerBlueprint extends Blueprint {
    
    private Map<Material, Integer> materialMap;
    
    private PlayerBlueprintMaterialMenu materialGUI;
    private UUID owner;
    private double cost;
    
    public PlayerBlueprint(ItemStack item, String schematic, String type, UUID owner, Map<Material, Integer> materialMap) {
        super(item, schematic, type);
        
        this.owner = owner;
        this.materialMap = materialMap;
        setCost();
    }
    
    public PlayerBlueprint(Map<String, Object> map) {
        super(map);
        
        materialMap = new HashMap<>();
        
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
    
    public UUID getOwner() {
        return owner;
    }
    
    public double getCost() {
        return cost;
    }
    
    public Map<Material, Integer> getMaterialMap() {
        return materialMap;
    }
    
    public void remove() {
        UUID player = owner;
        owner = UUID.randomUUID();
        //Dealing with player blueprint guis and updating them
        Optional<PlayerBlueprintMenu> optional = BlueprintsPlugin.instance.getGuiAssignmentFactory().getPlayerBlueprintListGUIFor(player);
        if (optional.isPresent()) {
            optional.get().setPageItems(PlayerBlueprint.getPageItems(player));
        } else {
            PlayerBlueprintMenu gui = new PlayerBlueprintMenu(BlueprintsPlugin.instance,
                    ChatColor.DARK_PURPLE + Bukkit.getOfflinePlayer(owner).getName() + "'s Player Blueprints",
                    true,
                    6,
                    PlayerBlueprint.getPageItems(player),
                    true,
                    player);
            
            BlueprintsPlugin.instance.getGuiAssignmentFactory().addPlayerBlueprintListGUI(gui);
        }
        
    }
    
    private boolean charge(Player player) {
        if (!Settings.USE_ECONOMY)
            return true;
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        
        if (!BlueprintsPlugin.getEconomy().has(offlinePlayer, getCost())) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.5F, 1F);
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eYou cannot afford the &6$" + getCost() + " &efor this blueprint!");
            return false;
        } else {
            BlueprintsPlugin.getEconomy().withdrawPlayer(offlinePlayer, getCost());
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&aYou have purchased this blueprint for &2$" + getCost() + "&a!");
            return true;
        }
    }
    
    private void setMaterialGUI() {
        List<PageItem> pageItems = new ArrayList<>();
        
        PlayerBlueprintMenu gui = new PlayerBlueprintMenu(BlueprintsPlugin.instance,
                org.bukkit.ChatColor.DARK_PURPLE + Bukkit.getOfflinePlayer(owner).getName() + "'s Player Blueprints",
                true,
                6,
                PlayerBlueprint.getPageItems(owner),
                true,
                owner);
        
        BlueprintsPlugin.instance.getGuiAssignmentFactory().addPlayerBlueprintListGUI(gui);
        
        for (Map.Entry<Material, Integer> entry : materialMap.entrySet()) {
            String name = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "ITEM: " + ChatColor.AQUA + entry.getKey().name().toLowerCase().replace('_', ' ');
            List<String> lore = Arrays.asList("", ChatColor.GRAY + "Amount needed: " + ChatColor.GREEN + entry.getValue());
            ItemStack item = Utils.buildItemStack(entry.getKey(), name, lore);
            item.setAmount(Math.min(item.getMaxStackSize(), entry.getValue()));
            
            pageItems.add(new PageItem(item));
        }
        
        this.materialGUI = new PlayerBlueprintMaterialMenu(BlueprintsPlugin.instance,
                ChatColor.DARK_PURPLE + "Blueprint Material List",
                true,
                6,
                pageItems,
                true,
                gui);
    }
    
    public PlayerBlueprintMaterialMenu getMaterialGUI() {
        if (materialGUI == null)
            setMaterialGUI();
        return materialGUI;
    }
    
    public static List<PageItem> getPageItems(UUID uuid) {
        
        List<PageItem> pageItems = new ArrayList<>();
        
        //Adds all items and their consumers
        for (PlayerBlueprint blueprint : BlueprintsPlugin.instance.getPlayerBlueprints().stream().filter(blueprint -> blueprint.getOwner().equals(uuid)).collect(Collectors.toList())) {
            
            ItemStack item = blueprint.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            
            lore.add("");
            lore.add(ChatColor.AQUA + "Left click" + ChatColor.GRAY + " to " + ChatColor.GREEN + "buy" + ChatColor.GRAY + " item for " + ChatColor.GREEN + "$" + blueprint.getCost() + ChatColor.GRAY + ".");
            lore.add(ChatColor.AQUA + "Right click" + ChatColor.GRAY + " to " + ChatColor.LIGHT_PURPLE + "open" + ChatColor.GRAY + " material list.");
            lore.add(ChatColor.AQUA + "Shift left click" + ChatColor.GRAY + " to " + ChatColor.RED + "remove" + ChatColor.GRAY + " blueprint.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            PageItem pageItem = new PageItem(item);
            
            pageItem.setClickAction(p -> blueprint.getMaterialGUI().display(p), ClickType.RIGHT);
            pageItem.setClickAction(p -> {
                if (!blueprint.charge(p))
                    return;
                p.getInventory().addItem(blueprint.getItem());
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 0.5F, 2F);
            }, ClickType.LEFT);
            pageItem.setClickAction(p -> new PlayerBlueprintRemoveGUI(p, blueprint).display(p), ClickType.SHIFT_LEFT);
            
            pageItems.add(pageItem);
        }
        
        return pageItems;
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
