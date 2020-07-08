package me.sizzlemcgrizzle.blueprints.event;

import de.craftlancer.core.LambdaRunnable;
import me.sizzlemcgrizzle.blueprints.Blueprint;
import me.sizzlemcgrizzle.blueprints.BlueprintCreationSession;
import me.sizzlemcgrizzle.blueprints.BlueprintPlacementSession;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.InventoryLink;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BlueprintListener implements Listener {
    
    private List<UUID> interactCooldown = new ArrayList<>();
    
    @EventHandler(ignoreCancelled = true)
    public void onBlockClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        
        if (!player.hasMetadata("blueprint_create") || interactCooldown.contains(player.getUniqueId()))
            return;
        
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;
        
        if (!BlueprintsPlugin.instance.getCreationSessions().containsKey(player)) {
            player.removeMetadata("blueprint_create", BlueprintsPlugin.instance);
            return;
        }
        
        BlueprintCreationSession session = BlueprintsPlugin.instance.getCreationSession(player);
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            session.setPosition2(event.getClickedBlock().getLocation().clone());
        else
            session.setPosition1(event.getClickedBlock().getLocation().clone());
        
        player.playSound(player.getLocation(), CompSound.NOTE_PLING.getSound(), 1F, 2F);
        interactCooldown.add(player.getUniqueId());
        new LambdaRunnable(() -> interactCooldown.remove(player.getUniqueId())).runTaskLater(BlueprintsPlugin.instance, 3);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void buildBlueprint(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItemInHand().clone();
        final Block block = event.getBlockPlaced();
        final Location blockLocation = event.getBlockPlaced().getLocation();
        final World world = blockLocation.getWorld();
        final BlockFace face = player.getFacing();
        
        
        if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName())
            return;
        
        Optional<Blueprint> optional = BlueprintsPlugin.instance.getBlueprints().stream().filter(b -> b.getItem().isSimilar(item)).findFirst();
        
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
        
        Common.runLater(2, setAir);
        Common.runLater(3, startBlueprint);
        
    }
    
    private boolean validateBlueprint(BlueprintPlacementSession blueprintPlacementSession) {
        Player player = blueprintPlacementSession.getPlayer();
        
        if (player.isConversing()) {
            Common.tell(player, BlueprintPlacementSession.IS_CONVERSING);
            return true;
        }
        
        return blueprintPlacementSession.isInRegion(blueprintPlacementSession.getLocation());
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBlueprintPaste(BlueprintPostPasteEvent event) {
        if (!event.getType().equalsIgnoreCase("SHIP"))
            return;
        
        event.getBlocksPasted().stream()
                .filter(location -> location.getBlock().getType() == Material.BARRIER)
                .forEach(location -> location.getBlock().setType(Material.AIR));
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (BlueprintsPlugin.instance.getLink(player).isPresent())
            BlueprintsPlugin.instance.removeInventoryLink(player);
        
        if (player.hasMetadata("blueprint_create") && BlueprintsPlugin.instance.getCreationSessions().containsKey(player))
            BlueprintsPlugin.instance.removeCreationSession(player);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        if (!player.hasMetadata("blueprint_create") && !BlueprintsPlugin.instance.getCreationSessions().containsKey(player))
            return;
        
        if (!BlueprintsPlugin.instance.getCreationSessions().containsKey(player)) {
            player.removeMetadata("blueprint_create", BlueprintsPlugin.instance);
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "Your blueprint creation session has been cancelled due to you logging out.");
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        if (block.getType() != Material.BARREL && block.getType() != Material.SHULKER_BOX)
            return;
        
        Optional<InventoryLink> optional = BlueprintsPlugin.instance.getLink(player);
        
        if (!optional.isPresent())
            return;
        
        Inventory inventory;
        if (block.getType() == Material.SHULKER_BOX)
            inventory = ((ShulkerBox) block.getState()).getInventory();
        else
            inventory = ((Barrel) block.getState()).getInventory();
        
        optional.get().remove(inventory);
    }
    
}