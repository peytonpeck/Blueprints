package me.sizzlemcgrizzle.blueprints;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import de.craftlancer.clclans.CLClans;
import me.sizzlemcgrizzle.blueprints.util.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Blueprint implements ConfigurationSerializable {
    private CLClans clansPlugin = (CLClans) Bukkit.getPluginManager().getPlugin("CLClans");
    private WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    
    private ItemStack item;
    private String schematic;
    private String type;
    private Clipboard clipboard;
    
    public Blueprint(ItemStack item, String schematic, String type) {
        this.item = item;
        this.schematic = schematic;
        if (type == null)
            this.type = "NORMAL";
        else
            this.type = type;
        
        getClipboardFromSchematic();
    }
    
    public Blueprint(Map<String, Object> map) {
        this.item = (ItemStack) map.get("item");
        this.schematic = (String) map.get("schematic");
        this.type = (String) map.get("type");
        
        getClipboardFromSchematic();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("item", item);
        map.put("schematic", schematic);
        map.put("type", type);
        
        return map;
    }
    
    protected void getClipboardFromSchematic() {
        File file = new File(worldEditPlugin.getDataFolder().getAbsolutePath() + File.separator + "/schematics" + File.separator + "/" + schematic);
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public String getSchematic() {
        return schematic;
    }
    
    public void setItem(ItemStack item) {
        this.item = item;
    }
    
    public void setSchematic(String schematic) {
        this.schematic = schematic;
    }
    
    public String getType() {
        return type;
    }
    
    void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }
    
    Clipboard getClipboard() {
        return clipboard;
    }
    
    public ClipboardHolder getHolder(Player player) {
        Clipboard copy = clipboard;
        
        try {
            if (clansPlugin != null && clansPlugin.isEnabled())
                if (clansPlugin.getClan(Bukkit.getOfflinePlayer(player.getUniqueId())) != null) {
                    ChatColor color = clansPlugin.getClan(Bukkit.getOfflinePlayer(player.getUniqueId())).getColor();
                    for (BlockVector3 blockVector3 : copy.getRegion()) {
                        if (copy.getBlock(blockVector3).getBlockType().equals(BlockTypes.WHITE_WOOL))
                            copy.setBlock(blockVector3, MaterialUtil.getWoolColor(color));
                        if (copy.getBlock(blockVector3).getBlockType().equals(BlockTypes.WHITE_CONCRETE))
                            copy.setBlock(blockVector3, MaterialUtil.getConcreteColor(color));
                    }
                }
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        
        return new ClipboardHolder(copy);
    }
}
