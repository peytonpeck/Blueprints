package me.sizzlemcgrizzle.blueprints.event;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import de.craftlancer.clclans.CLClans;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.conversation.ConfirmationPrompt;
import me.sizzlemcgrizzle.blueprints.conversation.FormattedConversable;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlueprintBuilder implements Listener {

	private ArrayList<Location> pasteBlockList = new ArrayList<>();
	private HashMap<Location, Player> fakeBlockMap = new HashMap<>();
	private HashMap<Location, Player> pasteFakeBlockmap = new HashMap<>();

	private CLClans clans = (CLClans) Bukkit.getPluginManager().getPlugin("CLClans");
	private WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
	private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");

	//Messages for states of placing the schematic
	private static String notInClaim = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BLUEPRINT_NOT_IN_CLAIM;
	private static String blocksInWay = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.SHOW_ERROR_TRUE_MESSAGE;
	private static String blocksInwayNoPreview = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.SHOW_ERROR_FALSE_MESSAGE;
	private static String schematicFileNoExist = Settings.Messages.MESSAGE_PREFIX + "&cThis blueprint is not valid! Please contact an administrator if you think this is an error.";

	/*
	 * Gets the schematic from a given string inputted by the player. If the player places a block, it will check
	 * which schematic this block uses.
	 */
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

	private void clearFakeBlocks() {
		for (Map.Entry<Location, Player> entry : fakeBlockMap.entrySet()) {
			entry.getValue().sendBlockChange(entry.getKey(), entry.getKey().getBlock().getBlockData());
		}
		fakeBlockMap.clear();
	}

	/*
	 * Build the blueprint and check for all errors that may occur.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void buildBlueprint(BlockPlaceEvent event) throws IOException, InvalidConfigurationException, WorldEditException {
		Player player = event.getPlayer();
		ItemStack item = event.getItemInHand();
		Block block = event.getBlockPlaced();
		World world = player.getWorld();
		String schematic;
		Clipboard clipboard;

		/*
		 * Is there a blueprint in the file for this block?
		 */
		if (blueprintsPlugin.schematicCache().getSchematicFor(item) != null) {
			//Store the schematic from the block in a variable, and set the blueprint block to air so it cannot be duped.
			schematic = blueprintsPlugin.schematicCache().getSchematicFor(event.getItemInHand());
			event.getBlockPlaced().setType(Material.AIR);
			clipboard = getSchematic(schematic, player);
		} else
			return;

		/*
		 * Check if the block's location is not in a claim.
		 */
		if (GriefPrevention.instance.isEnabled()) {
			Claim claim = null;
			for (Claim c : GriefPrevention.instance.dataStore.getClaims())
				if (c.contains(block.getLocation(), true, false))
					claim = c;
			if (claim == null) {
				Common.tell(player, notInClaim);
				if (Settings.PLAY_SOUNDS)
					player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
				event.setCancelled(true);
				return;
			}
		}

		/*
		 * If there are any error blocks present, remove them.
		 */
		if (fakeBlockMap.size() != 0)
			clearFakeBlocks();

		//Creating the operation that will be used to place the schematic. Basically doing //copy on a build.
		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(event.getPlayer().getWorld()), -1)) {

			//If a file in the schematics folder doesn't exist for this file, cancel the placement.
			if (clipboard == null) {
				Common.tell(player, schematicFileNoExist);
				if (Settings.PLAY_SOUNDS)
					player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
				event.setCancelled(true);
				return;
			}

			/*
			 * Get all the blocks that will be placed by the blueprint. These will be checked for air later.
			 */
			Operation testOperation = new ClipboardHolder(clipboard).createPaste(new AbstractDelegateExtent(editSession) {
				@Override
				public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) {
					Location pasteLocation = new Location(event.getBlockPlaced().getWorld(), location.getX(), location.getY(), location.getZ());
					pasteBlockList.add(pasteLocation);
					return true;
				}
			})
					.to(BlockVector3.at(block.getX(), block.getY(), block.getZ()))
					.copyBiomes(false)
					.copyEntities(false)
					.ignoreAirBlocks(true)
					.build();

			Operations.complete(testOperation);


			/*
			 * Check all of the blocks that will be pasted an determine if they aren't air or
			 * they are in the list of ignored blocks.
			 */
			Iterator<Location> pasteIterator = pasteBlockList.iterator();
			while (pasteIterator.hasNext()) {
				Location location = pasteIterator.next();
				if (!location.getBlock().getType().equals(Material.AIR) && !Settings.Block.IGNORE_BLOCKS.contains(location.getBlock().getType())) {
					fakeBlockMap.put(location, player);
					if (Settings.Block.SHOW_ERROR_PREVIEW)
						player.sendBlockChange(location, Settings.Block.ERROR_BLOCK.createBlockData());
					pasteIterator.remove();
				}
			}

			/*
			 * Send the player a message if there are error blocks present
			 */
			if (fakeBlockMap.size() != 0) {
				if (Settings.Block.SHOW_ERROR_PREVIEW)
					Common.tell(player, blocksInWay);
				else
					Common.tell(player, blocksInwayNoPreview);
				if (Settings.PLAY_SOUNDS)
					player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);


				/*
				 * Clear the ghost blocks
				 */
				Runnable runnable = this::clearFakeBlocks;

				if (Settings.Block.SHOW_ERROR_PREVIEW)
					Common.runLater(Settings.Block.BLOCK_TIMEOUT * 20, runnable);
				else
					fakeBlockMap.clear();
				event.setCancelled(true);
				return;
			}

			/*
			 * If the player is already conversing, cancel the placement.
			 */
			if (player.isConversing()) {
				Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&ePlease confirm/deny your existing placement before making a new one!");
				event.setCancelled(true);
				return;
			}

			/*
			 * Operation to send fake blocks to the player as a preview
			 */
			Operation previewOperation = new ClipboardHolder(clipboard).createPaste(new AbstractDelegateExtent(editSession) {
				@Override
				public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) {
					com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
					wePlayer.sendFakeBlock(location, block);
					pasteFakeBlockmap.put(new Location(world, location.getX(), location.getY(), location.getZ()), player);
					return true;
				}
			})
					.to(BlockVector3.at(block.getX(), block.getY(), block.getZ()))
					.copyBiomes(false)
					.copyEntities(false)
					.ignoreAirBlocks(true)
					.build();
			Operations.complete(previewOperation);

			if (Settings.PLAY_SOUNDS)
				player.playSound(player.getLocation(), CompSound.NOTE_BASS.getSound(), 2.0F, 0.8F);

			//Item to return, as we can't cancel the event from another class.
			ItemStack returnItem = item.clone();
			returnItem.setAmount(1);

			//Bossbar for countdown
			BossBar bossBar = blueprintsPlugin.getServer().createBossBar(ChatColor.GREEN + "Confirm Placement Timer", BarColor.GREEN, BarStyle.SEGMENTED_12, BarFlag.CREATE_FOG);
			bossBar.addPlayer(player);
			bossBar.setVisible(true);

			DecimalFormat format = new DecimalFormat("##.00");
			BukkitTask runnable = new BukkitRunnable() {
				double secondsLeft = 30.0;

				@Override
				public void run() {
					secondsLeft = Math.round(secondsLeft * 100.0) / 100.0;
					if (secondsLeft == 0.0)
						cancel();
					if (secondsLeft % 2.5 == 0 && secondsLeft != 30.0 && Settings.PLAY_SOUNDS)
						player.playSound(player.getLocation(), CompSound.NOTE_BASS.getSound(), 2.0F, 0.8F);
					bossBar.setTitle(ChatColor.GREEN + "Confirm Placement - " + format.format(secondsLeft) + " seconds left");
					bossBar.setProgress(secondsLeft / 30.0);
					secondsLeft -= 0.05;
				}
			}.runTaskTimer(blueprintsPlugin, 0, 1);

			ConversationFactory conversation = new ConversationFactory(blueprintsPlugin)
					.withLocalEcho(false)
					.withModality(false)
					.withTimeout(30)
					.withFirstPrompt(new ConfirmationPrompt(player, item, pasteFakeBlockmap, clipboard, block.getLocation(), schematic, bossBar))
					.addConversationAbandonedListener(conversationAbandonedEvent -> {
						if (!conversationAbandonedEvent.gracefulExit()) {
							Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eYour placement has been cancelled.");
							for (Map.Entry<Location, Player> entry : pasteFakeBlockmap.entrySet()) {
								player.sendBlockChange(entry.getKey(), entry.getKey().getBlock().getBlockData());
							}
							pasteFakeBlockmap.clear();
							try {
								blueprintsPlugin.logs().addToLogs(player, block.getLocation(), schematic, "abandoned");
							} catch (IOException e) {
								e.printStackTrace();
							}
							player.getInventory().addItem(returnItem);
						}
						runnable.cancel();
					});
			Conversation convo = conversation.buildConversation(new FormattedConversable(player));
			convo.begin();
		}
	}

	/*
	 * On reload, clear all error blocks that may be present.
	 */
	@EventHandler
	public void onReload(ReloadEvent event) {
		if (fakeBlockMap.size() == 0)
			return;
		clearFakeBlocks();
	}
}