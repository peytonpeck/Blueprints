package me.sizzlemcgrizzle.blueprints.event;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.craftlancer.clclans.CLClans;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlueprintBuilder implements Listener {

	private HashMap<Location, Player> originalBlockMap = new HashMap<>();

	private CLClans clans = (CLClans) Bukkit.getPluginManager().getPlugin("CLClans");
	private WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
	private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");

	//Messages for states of placing the schematic
	private static String notInClaim = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BLUEPRINT_NOT_IN_CLAIM;
	private static String notTrusted = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.PLAYER_NOT_TRUSTED;
	private static String adminClaim = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BLUEPRINT_IN_ADMIN_CLAIM;
	private static String blocksInWay = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.SHOW_ERROR_TRUE_MESSAGE;
	private static String blocksInwayNoPreview = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.SHOW_ERROR_FALSE_MESSAGE;
	private static String schematicFileNoExist = Settings.Messages.MESSAGE_PREFIX + "&cThis blueprint is not valid! Please contact an administrator if you think this is an error.";


	private Clipboard getSchematic(String schematic, Player player) {
		Clipboard clipboard = null;
		File file = new File(worldEditPlugin.getDataFolder().getAbsolutePath() + File.separator + "/schematics" + File.separator + "/" + schematic);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			clipboard = reader.read();

			if (clans.isEnabled())
				if (clans.getClan(Bukkit.getOfflinePlayer(player.getUniqueId())) != null) {
					ChatColor color = clans.getClan(Bukkit.getOfflinePlayer(player.getUniqueId())).getColor();
					for (BlockVector3 blockVector3 : clipboard.getRegion()) {
						if (clipboard.getBlock(blockVector3).getBlockType().equals(BlockTypes.WHITE_WOOL))
							clipboard.setBlock(blockVector3, ColorCache.getWoolColor(color));
						if (clipboard.getBlock(blockVector3).getBlockType().equals(BlockTypes.WHITE_CONCRETE))
							clipboard.setBlock(blockVector3, ColorCache.getConcreteColor(color));
					}
				}


		} catch (IOException | WorldEditException e) {
			Common.log(player.getName() + " tried to place a blueprint using " + schematic + ", however this schematic isn't valid!");
		}
		return clipboard;
	}

	private Boolean checkAdminClaim(double x, double y, double z, Player player, RegionManager regions) {
		if (Settings.Block.NO_PLACE_ADMIN_CLAIM && WorldGuardPlugin.inst().isEnabled())
			for (Map.Entry<String, ProtectedRegion> region : regions.getRegions().entrySet()) {
				//Getting the biggest and smallest points in the region to see if the location above is in there
				BlockVector3 regionMinPoint = region.getValue().getMinimumPoint();
				BlockVector3 regionMaxPoint = region.getValue().getMaximumPoint();
				//Variables defined to shorten the lenght of the if statement
				double minX = regionMinPoint.getX(), maxX = regionMaxPoint.getX(),
						minY = regionMinPoint.getY(), maxY = regionMaxPoint.getY(),
						minZ = regionMinPoint.getZ(), maxZ = regionMaxPoint.getZ();

				//Checks if any of the schematic blocks would be in a world guard claim.
				if (minX <= x && x <= maxX && minY <= y && y <= maxY && minZ <= z && z <= maxZ && !player.isOp()) {
					Common.tell(player, adminClaim);
					if (Settings.PLAY_SOUNDS)
						player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
					return true;
				}
			}
		return false;
	}

	@EventHandler
	public void buildBlueprint(BlockPlaceEvent event) throws IOException, InvalidConfigurationException, WorldEditException {
		Player player = event.getPlayer();
		ItemStack item = event.getItemInHand();
		Block block = event.getBlockPlaced();
		World world = player.getWorld();

		if (blueprintsPlugin.schematicCache().getSchematicFor(item) != null) {
			//Store the schematic from the block in a variable, and set the blueprint block to air so it cannot be duped.
			String schematic = blueprintsPlugin.schematicCache().getSchematicFor(event.getItemInHand());
			event.getBlockPlaced().setType(Material.AIR);

			//"random" world guard/world edit information regarding getting the schematic and checking to see if it will be placed
			//inside of a world guard claim.
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			RegionManager regions = container.get(new BukkitWorld(event.getPlayer().getWorld()));

			//Creating the operation that will be used to place the schematic. Basically doing //copy on a build.
			try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(event.getPlayer().getWorld()), -1)) {

				//If a file in the schematics folder doesn't exist for this file, cancel the placement.
				if (getSchematic(schematic, player) == null) {
					Common.tell(player, schematicFileNoExist);
					if (Settings.PLAY_SOUNDS)
						player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
					event.setCancelled(true);
					return;
				}

				Operation operation = new ClipboardHolder(getSchematic(schematic, player))
						.createPaste(editSession)
						.to(BlockVector3.at(event.getBlockPlaced().getX(), event.getBlockPlaced().getY(), event.getBlockPlaced().getZ()))
						.ignoreAirBlocks(true)
						.copyEntities(false)
						.copyBiomes(false)
						.build();


				//Get the max and min point of where the schematic will be placed. the blueprint will be the center of this.
				Location maxPoint = new Location(world, block.getX() + getSchematic(schematic, player).getDimensions().getX() / 2F,
						block.getY() + getSchematic(schematic, player).getDimensions().getY() - 1,
						block.getZ() + getSchematic(schematic, player).getDimensions().getZ() / 2F);
				Location minPoint = new Location(world, block.getX() - getSchematic(schematic, player).getDimensions().getX() / 2F,
						block.getY(),
						block.getZ() - getSchematic(schematic, player).getDimensions().getZ() / 2F);

				if (GriefPrevention.instance.isEnabled()) {
					Claim claim = null;
					//Get all the claims in the server
					for (Claim eventClaim : GriefPrevention.instance.dataStore.getClaims()) {
						if (eventClaim.contains(event.getBlockPlaced().getLocation(), true, true)) {
							claim = eventClaim;
							break;
						}
					}

					if (claim == null && Settings.Block.NO_PLACE_OUTSIDE_CLAIMS) {
						Common.tell(player, notInClaim);
						if (Settings.PLAY_SOUNDS)
							player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
						event.setCancelled(true);

					} else if (claim != null && claim.allowEdit(player) != null && !event.isCancelled() && !player.isOp()) {
						if (!claim.getOwnerName().equals(player.getName())) {
							Common.tell(player, notTrusted);
							if (Settings.PLAY_SOUNDS)
								player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
							event.setCancelled(true);
						}
					}
				}
				if (!event.isCancelled()) {
					//The three for loops below are iterating through EVERY block where the schematic will be placed
					for (double x = minPoint.getX(); x <= maxPoint.getX(); x++) {
						for (double y = minPoint.getY(); y <= maxPoint.getY(); y++) {
							for (double z = minPoint.getZ(); z <= maxPoint.getZ(); z++) {

								//Location of the block in the certain iteration
								Location location = new Location(event.getBlockPlaced().getWorld(), x, y, z);

								//Getting every world guard region in the world to check if ANY of the schematic will be placed in an admin claim,
								//because we don't want that.
								if (checkAdminClaim(x, y, z, player, regions)) {
									event.setCancelled(true);
									return;
								}

								//If there is a block in the way of where the schematic would place, disable the placement
								//and send a "ghost block" to the player that is defined in the configuration.
								if (!event.isCancelled() && !location.getBlock().getType().equals(Material.AIR) && Settings.Block.NO_PLACE_BLOCK_IN_WAY) {
									if (!originalBlockMap.containsKey(location))
										originalBlockMap.put(location, player);
									if (Settings.Block.SHOW_ERROR_PREVIEW)
										player.sendBlockChange(location, Settings.Block.ERROR_BLOCK.createBlockData());
								}
							}
						}
					}
				}

				//If there are no blocks blocking, and nothing has cancelled the event, we are good to paste the schematic!
				if (originalBlockMap.size() == 0 && !event.isCancelled()) {
					if (Settings.PLAY_SOUNDS)
						player.playSound(player.getLocation(), CompSound.ANVIL_USE.getSound(), 1F, 0.7F);
					Common.tell(player, Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BUILD_SUCCESS);
					Operations.complete(operation);

					//If there are blocks in the way, tell the player, and remove them in x amount of seconds.
					//There are events set up to remove these blocks if someone interacts with them or tries to blow
					//them up (basically trying to dupe them).
				} else if (!event.isCancelled()) {
					if (Settings.Block.SHOW_ERROR_PREVIEW)
						Common.tell(player, blocksInWay);
					else
						Common.tell(player, blocksInwayNoPreview);
					if (Settings.PLAY_SOUNDS)
						player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
					event.setCancelled(true);


					//Clear the "ghost blocks"
					Runnable runnable = () -> {
						for (Map.Entry<Location, Player> entry : originalBlockMap.entrySet())
							entry.getValue().sendBlockChange(entry.getKey(), entry.getKey().getBlock().getBlockData());
						originalBlockMap.clear();
					};
					if (Settings.Block.SHOW_ERROR_PREVIEW)
						Common.runLater(Settings.Block.BLOCK_TIMEOUT * 20, runnable);
					else
						originalBlockMap.clear();
				}
			}
		}

	}

	@EventHandler
	public void onReload(ReloadEvent event) {
		if (originalBlockMap.size() == 0)
			return;
		for (Map.Entry<Location, Player> entry : originalBlockMap.entrySet())
			entry.getValue().sendBlockChange(entry.getKey(), entry.getKey().getBlock().getBlockData());
		originalBlockMap.clear();
	}
}