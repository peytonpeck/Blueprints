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
import de.craftlancer.core.clipboard.Clipboard;
import de.craftlancer.core.clipboard.ClipboardManager;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayerBlueprintUtil {
    
    public static void complete(Player player, String name, Clipboard clipboard) {
        BoundingBox box = clipboard.toBoundingBox();
        MaterialContainer container = new MaterialContainer(box, clipboard.getWorld());
        
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(clipboard.getWorld()),
                BlockVector3.at(clipboard.getLocation1().getX(), clipboard.getLocation1().getY(), clipboard.getLocation1().getZ()),
                BlockVector3.at(clipboard.getLocation2().getX(), clipboard.getLocation2().getY(), clipboard.getLocation2().getZ()));
        BlockArrayClipboard blockArrayClipboard = new BlockArrayClipboard(region);
        
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(clipboard.getWorld()), -1)) {
            
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, blockArrayClipboard, region.getMinimumPoint()
            );
            
            Operations.complete(forwardExtentCopy);
            
            for (BlockVector3 blockVector3 : blockArrayClipboard.getRegion()) {
                Material material = BukkitAdapter.adapt(blockArrayClipboard.getBlock(blockVector3).getBlockType());
                if (material == Material.WATER
                        || material == Material.LAVA
                        || material == Material.RED_MUSHROOM_BLOCK
                        || material == Material.BROWN_MUSHROOM_BLOCK
                        || !material.isItem())
                    blockArrayClipboard.setBlock(blockVector3, BlockTypes.AIR.getDefaultState());
            }
            
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        
        String schematic = UUID.randomUUID().toString();
        ItemStack item = createItem(player, name, schematic);
        
        File file = new File(BlueprintsPlugin.getInstance().getDataFolder(), "/playerblueprints/" + schematic + ".schem");
        
        try {
            if (!file.exists())
                file.createNewFile();
            
            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                writer.write(blockArrayClipboard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        BlueprintsPlugin.getInstance().addBlueprint(new PlayerBlueprint(item, schematic + ".schem", "NORMAL", player.getUniqueId(), container));
        ClipboardManager.getInstance().removeClipboard(player.getUniqueId());
    }
    
    private static ItemStack createItem(Player player, String name, String schematic) {
        return new ItemBuilder(Material.STONE)
                .setCustomModelData(4)
                .setLore("§7Place this block to spawn the building.",
                        "§7Use /clipboard to link barrels",
                        "§7or shulkerboxes to draw items from,",
                        "§7or use your inventory.",
                        "",
                        "§7Blueprint made by: §a" + player.getName(),
                        "",
                        "§o§2/cchelp blueprints",
                        "§o§cOnce placed it cannot be undone.")
                .setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Player Blueprint: " +
                        ChatColor.WHITE + (player.hasPermission("blueprints.create.color") ? ChatColor.translateAlternateColorCodes('&', name) : name))
                .addPersistentData(BlueprintsPlugin.getInstance(), "blueprint-id", schematic)
                .build();
    }
}
