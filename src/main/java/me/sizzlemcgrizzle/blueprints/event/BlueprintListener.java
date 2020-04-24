package me.sizzlemcgrizzle.blueprints.event;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;

import java.io.IOException;
import java.util.List;

public class BlueprintListener implements Listener {
    
    private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void buildBlueprint(BlockPlaceEvent event) throws IOException, InvalidConfigurationException {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand().clone();
        Block block = event.getBlockPlaced();
        Location blockLocation = event.getBlockPlaced().getLocation();
        World world = blockLocation.getWorld();
        String schematic;
        String type;
        
        if (item.getItemMeta() == null)
            return;
        
        if (blueprintsPlugin.getBlueprints().contains(item.getItemMeta().getDisplayName())) {
            List<String> list = blueprintsPlugin.schematicCache().getSchematicFor(event.getItemInHand());
            schematic = list.get(0);
            type = list.get(1);
            
            Blueprint blueprint = new Blueprint(player, blockLocation, item, schematic, block, world, player.getGameMode(), type);
            
            if (validateBlueprint(blueprint)) {
                event.setCancelled(true);
                return;
            }
            
            Runnable setAir = () -> blueprint.getLocation().getBlock().setType(Material.AIR);
            Runnable startBlueprint = blueprint::start;
            
            Common.runLater(2, setAir);
            Common.runLater(3, startBlueprint);
        }
        
    }
    
    private boolean validateBlueprint(Blueprint blueprint) {
        Player player = blueprint.getPlayer();
        
        if (player.isConversing()) {
            Common.tell(player, Blueprint.IS_CONVERSING);
            return true;
        }
        
        if (blueprint.isInRegion(blueprint.getLocation())) {
            return true;
        }
        
        
        if (blueprint.getClipboard() == null) {
            Common.tell(player, Blueprint.SCHEMATIC_FILE_NO_EXIST);
            if (Settings.PLAY_SOUNDS)
                player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
            return true;
        }
        return false;
    }
    
}