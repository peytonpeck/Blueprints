package me.sizzlemcgrizzle.blueprints.placement;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.session.ClipboardHolder;
import de.craftlancer.core.gui.PageItem;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintMaterialMenu;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerBlueprint extends Blueprint {
    
    private MaterialContainer materialContainer;
    private PlayerBlueprintMaterialMenu materialGUI;
    private UUID originalOwner;
    private UUID owner;
    private double cost;
    
    public PlayerBlueprint(ItemStack item, String schematic, String type, UUID owner, MaterialContainer container) {
        super(item, schematic, type);
        
        this.originalOwner = owner;
        this.owner = owner;
        this.materialContainer = container;
        setCost();
    }
    
    public PlayerBlueprint(Map<String, Object> map) {
        super(map);
        
        this.owner = UUID.fromString((String) map.get("owner"));
        this.originalOwner = UUID.fromString((String) map.getOrDefault("originalOwner", owner.toString()));
        this.cost = (double) map.get("cost");
        this.materialContainer = (MaterialContainer) map.get("materialContainer");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("originalOwner", originalOwner.toString());
        map.put("owner", owner.toString());
        map.put("cost", cost);
        map.put("materialContainer", materialContainer);
        
        return map;
    }
    
    @Override
    protected void getClipboardFromSchematic() {
        File file = new File(BlueprintsPlugin.getInstance().getDataFolder() + File.separator + "/playerblueprints" + File.separator + "/" + getSchematic());
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
        cost = materialContainer.getCost();
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public double getCost() {
        return cost;
    }
    
    public Map<Material, Integer> getMaterialMap() {
        return materialContainer.getMaterialMap();
    }
    
    public void remove() {
        owner = UUID.randomUUID();
        //Dealing with player blueprint guis and updating them
        BlueprintsPlugin.getInstance().getPlayerBlueprintMenu(originalOwner).setPageItems(PlayerBlueprint.getPageItems(originalOwner));
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
        
        for (Map.Entry<Material, Integer> entry : getMaterialMap().entrySet()) {
            
            ItemStack item = new ItemBuilder(entry.getKey())
                    .setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "ITEM: " + ChatColor.AQUA + entry.getKey().name().toLowerCase().replace('_', ' '))
                    .setLore("",
                            ChatColor.GRAY + "Amount needed: " + ChatColor.GREEN + entry.getValue(),
                            ChatColor.GRAY + "Total material price: " + ChatColor.GREEN + "$" + Settings.PlayerBlueprint.PLAYER_BLUEPRINT_MATERIAL_PRICE_MULTIPLIER
                                    .getOrDefault(entry.getKey(), Settings.PlayerBlueprint.PLAYER_BLUEPRINT_PRICE_MULTIPLIER) * entry.getValue()
                    )
                    .setAmount(Math.min(entry.getKey().getMaxStackSize(), entry.getValue())).build();
            
            pageItems.add(new PageItem(item));
        }
        
        this.materialGUI = new PlayerBlueprintMaterialMenu(BlueprintsPlugin.getInstance(),
                ChatColor.DARK_PURPLE + "Blueprint Material List",
                true,
                6,
                pageItems,
                true
        );
    }
    
    public PlayerBlueprintMaterialMenu getMaterialGUI() {
        if (materialGUI == null)
            setMaterialGUI();
        return materialGUI;
    }
    
    public static List<PageItem> getPageItems(UUID uuid) {
        
        List<PageItem> pageItems = new ArrayList<>();
        
        //Adds all items and their consumers
        BlueprintsPlugin.getInstance().getPlayerBlueprints().stream().filter(blueprint -> blueprint.getOwner().equals(uuid)).forEach(blueprint -> {
            
            ItemStack item = new ItemBuilder(blueprint.getItem()).addLore(
                    "",
                    ChatColor.AQUA + "Left click" + ChatColor.GRAY + " to " + ChatColor.GREEN + "buy" + ChatColor.GRAY + " item for " + ChatColor.GREEN + "$" + blueprint.getCost() + ChatColor.GRAY + ".",
                    ChatColor.AQUA + "Right click" + ChatColor.GRAY + " to " + ChatColor.LIGHT_PURPLE + "open" + ChatColor.GRAY + " material list.",
                    ChatColor.AQUA + "Shift left click" + ChatColor.GRAY + " to " + ChatColor.RED + "remove" + ChatColor.GRAY + " blueprint.").build();
            
            PageItem pageItem = new PageItem(item);
            
            pageItem.setClickAction(p -> blueprint.getMaterialGUI().display(p), ClickType.RIGHT);
            pageItem.setClickAction(p -> {
                if (!blueprint.charge(p))
                    return;
                p.getInventory().addItem(blueprint.getItem());
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 0.5F, 2F);
            }, ClickType.LEFT);
            pageItem.setClickAction(p -> BlueprintsPlugin.getInstance().getPlayerBlueprintRemoveGUI().display(p, blueprint), ClickType.SHIFT_LEFT);
            
            pageItems.add(pageItem);
        });
        
        return pageItems;
    }
    
    public static int getAmount(Player player) {
        return (int) BlueprintsPlugin.getInstance().getPlayerBlueprints().stream().filter(b -> b.getOwner().equals(player.getUniqueId())).count();
    }
    
    public static int getLimit(Player player) {
        int limit = -1;
        
        for (String l : Settings.PlayerBlueprint.LIMITS) {
            if (!player.hasPermission(l))
                continue;
            
            int num = Integer.parseInt(l.split("blueprints.placement.")[1]);
            
            if (num == -1)
                return -1;
            if (num > limit)
                limit = num;
        }
        return limit;
    }
}
