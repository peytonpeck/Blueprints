package me.sizzlemcgrizzle.blueprints.event;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;

import java.util.List;

public class BlueprintListener implements Listener {
	
	private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void buildBlueprint(final BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		final ItemStack item = event.getItemInHand().clone();
		final Block block = event.getBlockPlaced();
		final Location blockLocation = event.getBlockPlaced().getLocation();
		final World world = blockLocation.getWorld();
		final String schematic;
		final String type;
		final BlockFace face = player.getFacing();
		
		
		if (item.getItemMeta() == null)
			return;
		
		if (blueprintsPlugin.getBlueprints().contains(item.getItemMeta().getDisplayName())) {
			List<String> list = blueprintsPlugin.schematicCache().getSchematicFor(event.getItemInHand());
			schematic = list.get(0);
			type = list.get(1);
			
			Blueprint blueprint = new Blueprint(player, blockLocation, item, schematic, block, world, player.getGameMode(), type, face);
			
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