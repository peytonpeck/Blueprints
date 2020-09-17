package me.sizzlemcgrizzle.blueprints.placement;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;
import de.craftlancer.core.Utils;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.ParticleUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.mineacademy.fo.Common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class BlueprintCreationSession {
    private Location position1;
    private Location position2;
    private Player owner;
    private BukkitTask runnable;
    
    public BlueprintCreationSession(Player player) {
        this.owner = player;
        
        BlueprintsPlugin.getInstance().addCreationSession(player, this);
    }
    
    public void complete(String name) {
        BoundingBox box = new BoundingBox(position1.getX(), position1.getY(), position1.getZ(), position2.getX(), position2.getY(), position2.getZ());
        MaterialContainer container = new MaterialContainer(box, position1.getWorld());
        
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(position1.getWorld()),
                BlockVector3.at(position1.getX(), position1.getY(), position1.getZ()),
                BlockVector3.at(position2.getX(), position2.getY(), position2.getZ()));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(position1.getWorld()), -1)) {
            
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            
            Operations.complete(forwardExtentCopy);
            
            for (BlockVector3 blockVector3 : clipboard.getRegion()) {
                Material material = BukkitAdapter.adapt(clipboard.getBlock(blockVector3).getBlockType());
                if (material == Material.WATER || material == Material.LAVA)
                    clipboard.setBlock(blockVector3, BlockTypes.AIR.getDefaultState());
            }
            
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        
        String schematic = UUID.randomUUID().toString();
        ItemStack item = createItem(name, schematic);
        
        File file = new File(BlueprintsPlugin.getInstance().getDataFolder(), "/playerblueprints/" + schematic + ".schem");
        
        try {
            if (!file.exists())
                file.createNewFile();
            
            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                writer.write(clipboard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        BlueprintsPlugin.getInstance().addBlueprint(new PlayerBlueprint(item, schematic + ".schem", "NORMAL", owner.getUniqueId(), container));
        
        BlueprintsPlugin.getInstance().removeCreationSession(owner);
    }
    
    private ItemStack createItem(String name, String schematic) {
        return new ItemBuilder(Material.STONE)
                .setCustomModelData(4)
                .setLore("§7Place this block to spawn the building.",
                        "§7Link chests to draw blocks from using",
                        "§3/blueprint link create§7.",
                        "",
                        "§7Blueprint made by: §a" + owner.getName(),
                        "",
                        "§o§2/cchelp blueprints",
                        "§o§cOnce placed it cannot be undone.")
                .setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Player Blueprint: " +
                        ChatColor.WHITE + (owner.hasPermission("blueprints.create.color") ? ChatColor.translateAlternateColorCodes('&', name) : name))
                .addPersistentData(BlueprintsPlugin.getInstance(), "blueprint-id", schematic)
                .build();
    }
    
    public void remove() {
        if (runnable == null)
            return;
        
        runnable.cancel();
    }
    
    public void setPosition1(Location position1) {
        this.position1 = position1;
        
        Common.tell(owner, Settings.Messages.MESSAGE_PREFIX + "&aPosition 1 set at &2(" + position1.getX() + ", " + position1.getY() + ", " + position1.getZ() + ")&a.");
        
        spawnParticles();
    }
    
    public void setPosition2(Location position2) {
        this.position2 = position2;
        
        Common.tell(owner, Settings.Messages.MESSAGE_PREFIX + "&aPosition 2 set at &2(" + position2.getX() + ", " + position2.getY() + ", " + position2.getZ() + ")&a.");
        
        spawnParticles();
    }
    
    public Location getPosition1() {
        return position1;
    }
    
    public Location getPosition2() {
        return position2;
    }
    
    public Player getPlayer() {
        return owner;
    }
    
    public double getArea() {
        double minX = Math.min(position1.getX(), position2.getX());
        double minY = Math.min(position1.getY(), position2.getY());
        double minZ = Math.min(position1.getZ(), position2.getZ());
        double maxX = Math.max(position1.getX(), position2.getX()) + 1;
        double maxY = Math.max(position1.getY(), position2.getY()) + 1;
        double maxZ = Math.max(position1.getZ(), position2.getZ()) + 1;
        
        return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }
    
    private void spawnParticles() {
        if (runnable != null)
            runnable.cancel();
        
        Location pos1, pos2;
        
        if (position1 == null && position2 == null)
            return;
        
        if (position1 == null) {
            pos1 = position2.clone();
            pos2 = position2.clone();
        } else if (position2 == null) {
            pos1 = position1.clone();
            pos2 = position1.clone();
        } else {
            pos1 = position1;
            pos2 = position2;
        }
        
        if (pos1.getWorld() != pos2.getWorld())
            return;
        
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!Utils.isChunkLoaded(pos1) || !Utils.isChunkLoaded(pos2))
                    return;
                
                double minX = Math.min(pos1.getX(), pos2.getX());
                double minY = Math.min(pos1.getY(), pos2.getY());
                double minZ = Math.min(pos1.getZ(), pos2.getZ());
                double maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
                double maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
                double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;
                
                Location minLocation = new Location(pos1.getWorld(), minX, minY, minZ);
                Location maxLocation = new Location(pos1.getWorld(), maxX, maxY, maxZ);
                
                ParticleUtil.spawnParticleRect(minLocation, maxLocation, Color.TEAL);
            }
        }.runTaskTimer(BlueprintsPlugin.getInstance(), 1, 20);
    }
}
