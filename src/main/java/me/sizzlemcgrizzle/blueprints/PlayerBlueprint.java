package me.sizzlemcgrizzle.blueprints;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBlueprint extends Blueprint {
    
    private Map<Material, Integer> materialMap = new HashMap<>();
    private UUID owner;
    
    public PlayerBlueprint(ItemStack item, String schematic, String type, UUID owner) {
        super(item, schematic, type);
        
        this.owner = owner;
        
        setMaterialMap();
    }
    
    public PlayerBlueprint(Map<String, Object> map) {
        super(map);
        
        this.owner = UUID.fromString((String) map.get("owner"));
        
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
    
    private void setMaterialMap() {
        for (BlockVector3 blockVector3 : getClipboard().getRegion()) {
            Material material = BukkitAdapter.adapt(getClipboard().getBlock(blockVector3).getBlockType().getItemType());
            
            if (material != Material.AIR)
                materialMap.compute(material,
                        (k, v) -> materialMap.containsKey(k) ? materialMap.get(k) + 1 : 1);
        }
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public Map<Material, Integer> getMaterialMap() {
        return materialMap;
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
            
            if (num != -1 && num > limit)
                limit = num;
        }
        
        return limit;
    }
}
