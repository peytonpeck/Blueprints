package me.sizzlemcgrizzle.blueprints.event;

import com.sk89q.worldedit.WorldEditException;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

public class BlueprintListener implements Listener {

	private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void buildBlueprint(BlockPlaceEvent event) throws IOException, InvalidConfigurationException, WorldEditException {
		Player player = event.getPlayer();
		ItemStack item = event.getItemInHand().clone();
		Block block = event.getBlockPlaced();
		Location blockLocation = event.getBlockPlaced().getLocation();
		World world = blockLocation.getWorld();
		String schematic;
		String type;

		if (blueprintsPlugin.schematicCache().getSchematicFor(item) != null) {
			List<String> list = blueprintsPlugin.schematicCache().getSchematicFor(event.getItemInHand());
			schematic = list.get(0);
			type = list.get(1);

			if (new Blueprint(player, blockLocation, item, schematic, block, world, player.getGameMode(), type).start())
				event.setCancelled(true);
		}

	}

}