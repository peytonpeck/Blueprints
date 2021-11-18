package me.sizzlemcgrizzle.blueprints.placement;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import de.craftlancer.clapi.LazyService;
import de.craftlancer.clapi.blueprints.AbstractBlueprint;
import de.craftlancer.clapi.clclans.AbstractClan;
import de.craftlancer.clapi.clclans.PluginClans;
import de.craftlancer.core.util.MaterialUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Blueprint implements ConfigurationSerializable, AbstractBlueprint {
    
    public static final NamespacedKey BLUEPRINT_KEY = new NamespacedKey(BlueprintsPlugin.getInstance(), "blueprintItem");
    private static final LazyService<PluginClans> CLANS = new LazyService<>(PluginClans.class);
    
    private WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    
    private ItemStack item;
    private String schematic;
    private String type;
    private Clipboard clipboard;
    
    private boolean canRotate90Degrees;
    private boolean canItemFrameRotate45Degrees;
    private boolean canTranslate;
    
    public Blueprint(ItemStack item, String schematic, String type, boolean canRotate90Degrees, boolean canTranslate, boolean canItemFrameRotate45Degrees) {
        this.item = item;
        this.schematic = schematic;
        if (type == null)
            this.type = "NORMAL";
        else
            this.type = type;
        
        this.canRotate90Degrees = canRotate90Degrees;
        this.canItemFrameRotate45Degrees = canItemFrameRotate45Degrees;
        this.canTranslate = canTranslate;
        
        getClipboardFromSchematic();
    }
    
    public Blueprint(Map<String, Object> map) {
        this.item = (ItemStack) map.get("item");
        this.schematic = (String) map.get("schematic");
        this.type = (String) map.get("type");
        
        this.canItemFrameRotate45Degrees = (boolean) map.getOrDefault("canRotate45Degrees", true);
        this.canRotate90Degrees = (boolean) map.getOrDefault("canRotate", true);
        this.canTranslate = (boolean) map.getOrDefault("canTranslate", true);
        
        getClipboardFromSchematic();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("item", item);
        map.put("schematic", schematic);
        map.put("type", type);
        
        map.put("canRotate45Degrees", canItemFrameRotate45Degrees);
        map.put("canRotate", canRotate90Degrees);
        map.put("canTranslate", canTranslate);
        
        return map;
    }
    
    private void getClipboardFromSchematic() {
        File file = getSchematicFile();
        
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected File getSchematicFile() {
        return new File(worldEditPlugin.getDataFolder().getAbsolutePath(), "/schematics/" + schematic);
    }
    
    @Override
    public ItemStack getItem() {
        return item;
    }
    
    @Override
    public String getSchematic() {
        return schematic;
    }
    
    public void setItem(ItemStack item) {
        this.item = item;
    }
    
    public void setSchematic(String schematic) {
        this.schematic = schematic;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }
    
    Clipboard getClipboard() {
        return clipboard;
    }
    
    @Override
    public boolean canItemFrameRotate45Degrees() {
        return canItemFrameRotate45Degrees;
    }
    
    @Override
    public boolean canTranslate() {
        return canTranslate;
    }
    
    @Override
    public boolean canRotate90Degrees() {
        return canRotate90Degrees;
    }
    
    public ClipboardHolder getHolder(Player player) {
        Clipboard copy = getClipboard();
        
        CLANS.ifPresent(clans -> {
            try {
                if (clans != null) {
                    AbstractClan clan = clans.getClan(Bukkit.getOfflinePlayer(player.getUniqueId()));
                    if (clan != null) {
                        ChatColor color = clan.getColor();
                        for (BlockVector3 blockVector3 : copy.getRegion()) {
                            if (copy.getBlock(blockVector3).getBlockType().equals(BlockTypes.WHITE_WOOL))
                                copy.setBlock(blockVector3, BukkitAdapter.asBlockType(MaterialUtil.getWoolColor(color)).getDefaultState());
                            if (copy.getBlock(blockVector3).getBlockType().equals(BlockTypes.WHITE_CONCRETE))
                                copy.setBlock(blockVector3, BukkitAdapter.asBlockType(MaterialUtil.getConcreteColor(color)).getDefaultState());
                        }
                    }
                }
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        });
        
        return new ClipboardHolder(copy);
    }
    
    @Override
    public boolean compareItem(ItemStack i) {
        if (i.isSimilar(item))
            return true;
        
        if (!item.getItemMeta().getPersistentDataContainer().has(BLUEPRINT_KEY, PersistentDataType.STRING))
            return false;
        
        if (!i.getItemMeta().getPersistentDataContainer().has(BLUEPRINT_KEY, PersistentDataType.STRING))
            return false;
        
        return item.getItemMeta().getPersistentDataContainer().get(BLUEPRINT_KEY, PersistentDataType.STRING).equals(
                i.getItemMeta().getPersistentDataContainer().get(BLUEPRINT_KEY, PersistentDataType.STRING)
        );
    }
    
    @Override
    public boolean canCopyEntities() {
        return true;
    }
}
