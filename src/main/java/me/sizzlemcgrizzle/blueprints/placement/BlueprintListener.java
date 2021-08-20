package me.sizzlemcgrizzle.blueprints.placement;

import de.craftlancer.clapi.blueprints.AbstractBlueprint;
import de.craftlancer.clapi.blueprints.BlueprintPostPasteEvent;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.Utils;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
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
        
        if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName())
            return;
        
        Optional<AbstractBlueprint> optional = plugin.getBlueprints().stream().filter(b -> b.compareItem(item)).findFirst();
        
        if (!optional.isPresent())
            return;
        
        if (!beginBlueprint((Blueprint) optional.get(), item, player, block))
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        final Player player = event.getPlayer();
        final ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        final Block block = event.getClickedBlock().getRelative(event.getBlockFace());
        
        if (item.getType().isBlock())
            return;
        
        if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName())
            return;
        
        Optional<AbstractBlueprint> optional = plugin.getBlueprints().stream().filter(b -> b.compareItem(item)).findFirst();
        
        if (!optional.isPresent())
            return;
        
        if (beginBlueprint((Blueprint) optional.get(), item.clone(), player, block) && player.getGameMode() != GameMode.CREATIVE)
            item.setAmount(item.getAmount() - 1);
    }
    
    private boolean beginBlueprint(Blueprint blueprint, ItemStack item, Player player, Block block) {
        
        BlueprintPlacementSession session = new BlueprintPlacementSession(plugin, blueprint, player, block.getLocation(), item,
                block, block.getWorld(), player.getGameMode(), blueprint.getType(), player.getFacing());
        
        if (validateBlueprint(session))
            return false;
        
        new LambdaRunnable(() -> session.getLocation().getBlock().setType(Material.AIR)).runTaskLater(plugin, 2);
        new LambdaRunnable(session::start).runTaskLater(plugin, 3);
        return true;
    }
    
    private boolean validateBlueprint(BlueprintPlacementSession blueprintPlacementSession) {
        Player player = blueprintPlacementSession.getPlayer();
        
        if (player.isConversing()) {
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.IS_CONVERSING);
            return true;
        }
        
        boolean bool = !player.isOp() && Utils.isInAdminRegion(blueprintPlacementSession.getLocation());
        
        if (bool)
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "You cannot place a blueprint anywhere in an " +
                    "admin claim!");
        
        return bool;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBlueprintPaste(BlueprintPostPasteEvent event) {
        if (event.getType().equalsIgnoreCase("SHIP")) {
            event.getBlocksPasted().stream()
                    .filter(location -> location.getBlock().getType() == Material.BARRIER)
                    .forEach(location -> location.getBlock().setType(Material.AIR));
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        plugin.getLink(player).ifPresent(l -> plugin.removeInventoryLink(player));
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
        
        Optional<InventoryLink> optional = plugin.getLink(player);
        
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