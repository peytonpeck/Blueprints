package me.sizzlemcgrizzle.blueprints.placement;

import de.craftlancer.core.LambdaRunnable;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;

import java.util.Optional;

public class BlueprintListener implements Listener {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintListener(BlueprintsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void buildBlueprint(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getPlayer().getInventory().getItem(event.getHand()).clone();
        final Block block = event.getBlockPlaced();
        final Location blockLocation = event.getBlockPlaced().getLocation();
        final World world = blockLocation.getWorld();
        final BlockFace face = player.getFacing();
        
        
        if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName())
            return;
        
        Optional<Blueprint> optional = BlueprintsPlugin.getInstance().getBlueprints().stream().filter(b -> b.getItem().isSimilar(item)).findFirst();
        
        if (!optional.isPresent())
            return;
        
        Blueprint blueprint = optional.get();
        
        BlueprintPlacementSession session = new BlueprintPlacementSession(blueprint, player, blockLocation, item, block, world, player.getGameMode(), blueprint.getType(), face);
        
        if (validateBlueprint(session)) {
            event.setCancelled(true);
            return;
        }
        
        Runnable setAir = () -> session.getLocation().getBlock().setType(Material.AIR);
        Runnable startBlueprint = session::start;
        
        new LambdaRunnable(() -> session.getLocation().getBlock().setType(Material.AIR)).runTaskLater(plugin, 2);
        new LambdaRunnable(session::start).runTaskLater(plugin, 3);
        
    }
    
    private boolean validateBlueprint(BlueprintPlacementSession blueprintPlacementSession) {
        Player player = blueprintPlacementSession.getPlayer();
        
        if (player.isConversing()) {
            Common.tell(player, BlueprintPlacementSession.IS_CONVERSING);
            return true;
        }
        
        boolean bool = BlueprintsPlugin.isInRegion(player, blueprintPlacementSession.getLocation());
        
        if (bool)
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&cYou cannot place a blueprint anywhere in an admin claim!");
        
        return bool;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBlueprintPaste(BlueprintPostPasteEvent event) {
        if (event.getType().equalsIgnoreCase("SHIP")) {
            event.getBlocksPasted().stream()
                    .filter(location -> location.getBlock().getType() == Material.BARRIER)
                    .forEach(location -> location.getBlock().setType(Material.AIR));
        }
        
        plugin.getRewards().stream().filter(r -> r.getBaseSchematic().equals(event.getSchematic())).forEach(r -> r.reward(event.getPlayer()));
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        BlueprintsPlugin.getInstance().getLink(player).ifPresent(l -> plugin.removeInventoryLink(player));
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getLink(player).isPresent())
            plugin.addInventoryLink(new InventoryLink(player));
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        if (block.getType() != Material.BARREL && !block.getType().name().contains("SHULKER_BOX"))
            return;
        
        Optional<InventoryLink> optional = BlueprintsPlugin.getInstance().getLink(player);
        
        if (!optional.isPresent())
            return;
        
        Inventory inventory;
        if (block.getType().name().contains("SHULKER_BOX"))
            inventory = ((ShulkerBox) block.getState()).getInventory();
        else
            inventory = ((Barrel) block.getState()).getInventory();
        
        optional.get().remove(inventory);
    }
    
}