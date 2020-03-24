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
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.craftlancer.clclans.CLClans;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.conversation.ConfirmationPrompt;
import me.sizzlemcgrizzle.blueprints.conversation.FormattedConversable;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class Blueprint {
	private CLClans clans = (CLClans) Bukkit.getPluginManager().getPlugin("CLClans");
	private WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
	private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");

	private final String notInClaim = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BLUEPRINT_NOT_IN_CLAIM;
	private final String blocksInWay = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.SHOW_ERROR_TRUE_MESSAGE;
	private final String blocksInwayNoPreview = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.SHOW_ERROR_FALSE_MESSAGE;
	private final String schematicFileNoExist = Settings.Messages.MESSAGE_PREFIX + "&cThis blueprint is not valid! Please contact an administrator if you think this is an error.";
	private final String placementDenied = Settings.Messages.MESSAGE_PREFIX + "&eYou have cancelled the placement and your item has been returned.";
	private final String placementAccepted = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BUILD_SUCCESS;

	private Player player;
	private Location location;
	private ItemStack item;
	private Block block;
	private World world;
	private String schematic;
	private RegionContainer container;
	private RegionManager regions;
	private Clipboard clipboard;
	private ClipboardHolder holder;
	private int counter;
	private BossBar bossBar;
	private BukkitTask runnable;
	private BukkitTask clearErrorBlocks;
	private GameMode gameMode;
	private Inventory inventory;

	private Set<Location> pasteBlockSet = new HashSet<>();
	private Set<Location> errorBlockSet = new HashSet<>();

	Blueprint(Player player, Location loc, ItemStack item, String schematic, Block block, World world, GameMode gameMode) {
		this.item = item;
		this.item.setAmount(1);
		this.player = player;
		this.schematic = schematic;
		this.location = loc;
		this.block = block;
		this.world = world;
		this.inventory = player.getInventory();
		this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		this.regions = container.get(new BukkitWorld(world));
		this.clipboard = getClipboard();
		this.holder = new ClipboardHolder(clipboard);
		this.bossBar = blueprintsPlugin.getBossBar();
		this.gameMode = gameMode;
	}

	boolean start() {
		block.setType(Material.AIR);

		if (player.isConversing()) {
			Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&ePlease confirm/deny your existing placement before making a new one!");
			return true;
		}

		if (isInRegion(location)) {
			return true;
		}


		if (clipboard == null) {
			Common.tell(player, schematicFileNoExist);
			if (Settings.PLAY_SOUNDS)
				player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
			return true;
		}

		getBlocksInWay();

		if (errorBlockSet.size() > 0) {
			if (Settings.Block.SHOW_ERROR_PREVIEW)
				Common.tell(player, blocksInWay);
			else
				Common.tell(player, blocksInwayNoPreview);
			if (Settings.PLAY_SOUNDS)
				player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
			showErrorBlocks();
			return true;
		}

		sendFakeBlocks();

		if (Settings.PLAY_SOUNDS)
			player.playSound(player.getLocation(), CompSound.NOTE_BASS.getSound(), 2.0F, 0.8F);

		spawnBossBar();
		beginConversation();
		return false;
	}

	private void getBlocksInWay() {
		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
			Operation testOperation = holder.createPaste(new AbstractDelegateExtent(editSession) {
				@Override
				public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) {
					Location pasteLocation = new Location(world, location.getX(), location.getY(), location.getZ());
					if ((!pasteLocation.getBlock().getType().isAir() && !Settings.Block.IGNORE_BLOCKS.contains(pasteLocation.getBlock().getType()))
							|| isInRegion(pasteLocation)
							|| !isTrusted(pasteLocation))
						errorBlockSet.add(pasteLocation);
					return true;
				}
			})
					.to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
					.copyBiomes(false)
					.copyEntities(false)
					.ignoreAirBlocks(true)
					.build();

			Operations.complete(testOperation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
	}

	private void sendFakeBlocks() {
		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
			Operation previewOperation = holder.createPaste(new AbstractDelegateExtent(editSession) {
				@Override
				public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) {
					com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
					wePlayer.sendFakeBlock(location, block);
					pasteBlockSet.add(new Location(world, location.getX(), location.getY(), location.getZ()));
					return true;
				}
			})
					.to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
					.copyBiomes(false)
					.copyEntities(false)
					.ignoreAirBlocks(true)
					.build();
			Operations.complete(previewOperation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
	}

	public boolean setOrigin(int x, int y, int z) {
		if (clearErrorBlocks != null) {
			clearErrorBlocks.cancel();
			clearErrorBlocks();
		}
		location.setX(location.getX() + x);
		location.setY(location.getY() + y);
		location.setZ(location.getZ() + z);
		clearFakeBlocks();
		getBlocksInWay();
		if (errorBlockSet.size() > 0) {
			Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eTranslation cancelled. The blueprint cannot be moved here!");
			showErrorBlocks();
			location.setX(location.getX() - x);
			location.setY(location.getY() - y);
			location.setZ(location.getZ() - z);
		}
		sendFakeBlocks();
		return false;
	}

	public boolean transform(String operation) {
		if (clearErrorBlocks != null) {
			clearErrorBlocks.cancel();
			clearErrorBlocks();
		}
		if (operation.equals("+")) {
			counter += 1;
			holder.setTransform(new AffineTransform().rotateY(counter * 90));
		} else if (operation.equals("-")) {
			counter -= 1;
			holder.setTransform(new AffineTransform().rotateY(counter * 90));
		}
		clearFakeBlocks();
		getBlocksInWay();
		if (errorBlockSet.size() > 0) {
			Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&ePlacement cancelled. The blueprint cannot be moved here!");
			showErrorBlocks();
			if (operation.equals("+"))
				counter -= 1;
			else
				counter += 1;
			holder.setTransform(new AffineTransform().rotateY(counter * 90));
		}
		sendFakeBlocks();
		return false;
	}

	private Clipboard getClipboard() {
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

	private boolean isInRegion(Location loc) {
		if (player.isOp())
			return false;

		if (WorldGuard.getInstance() != null && regions != null && !player.isOp()) {
			BlockVector3 position = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
			ApplicableRegionSet set = regions.getApplicableRegions(position);
			if (set.size() != 0) {
				Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&cYou cannot place a blueprint anywhere in an admin claim!");
				return true;
			}
		}
		return false;
	}

	public void complete() {
		for (Location location : pasteBlockSet) {
			player.sendBlockChange(location, location.getBlock().getBlockData());
		}
		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
			Operation operation = holder
					.createPaste(editSession)
					.to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
					.ignoreAirBlocks(true)
					.copyEntities(false)
					.copyBiomes(false)
					.build();

			Operations.complete(operation);
			blueprintsPlugin.logs().addToLogs(player, location, schematic, "confirmed");
			bossBar.removePlayer(player);
			blueprintsPlugin.removeBlueprint(player);
			if (Settings.PLAY_SOUNDS)
				player.playSound(player.getLocation(), CompSound.ANVIL_USE.getSound(), 1F, 0.7F);
			Common.tell(player, placementAccepted);
		} catch (WorldEditException | IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isTrusted(Location loc) {
		if (GriefPrevention.instance.isEnabled() && !block.getType().equals(Material.LILY_PAD)) {
			Claim claim = null;
			for (Claim c : GriefPrevention.instance.dataStore.getClaims())
				if (c.contains(loc, true, false))
					claim = c;
			if (claim == null)
				return true;
			if (claim.getOwnerName().equals(player.getName()))
				return true;
			if (claim.allowBuild(player, loc.getBlock().getType()) == null)
				return true;
		}
		return false;
	}

	private void clearFakeBlocks() {
		for (Location location : pasteBlockSet)
			player.sendBlockChange(location, location.getBlock().getBlockData());
		pasteBlockSet.clear();
	}

	private void clearErrorBlocks() {
		for (Location location : errorBlockSet)
			player.sendBlockChange(location, location.getBlock().getBlockData());
		errorBlockSet.clear();
	}

	private void showErrorBlocks() {

		this.clearErrorBlocks = new BukkitRunnable() {
			@Override
			public void run() {
				clearErrorBlocks();
			}
		}.runTaskLater(blueprintsPlugin, Settings.Block.BLOCK_TIMEOUT * 20);

		if (Settings.Block.SHOW_ERROR_PREVIEW) {
			for (Location location : errorBlockSet) {
				player.sendBlockChange(location, Settings.Block.ERROR_BLOCK.createBlockData());
			}
		}
	}

	private void spawnBossBar() {
		bossBar.addPlayer(player);
		bossBar.setVisible(true);

		DecimalFormat format = new DecimalFormat("##.00");
		this.runnable = new BukkitRunnable() {
			double duration = (double) Settings.Block.BOSSBAR_DURATION;
			double secondsLeft = duration;

			@Override
			public void run() {
				secondsLeft = Math.round(secondsLeft * 100.0) / 100.0;
				if (secondsLeft == 0.0) {
					cancel();
				}
				if (secondsLeft % 2.5 == 0 && secondsLeft != duration && Settings.PLAY_SOUNDS)
					player.playSound(player.getLocation(), CompSound.NOTE_BASS.getSound(), 2.0F, 0.8F);
				bossBar.setTitle(ChatColor.GREEN + "Confirm Placement: " + ChatColor.RED + format.format(secondsLeft) + ChatColor.GREEN + " seconds left");
				bossBar.setProgress(secondsLeft / duration);
				secondsLeft -= 0.05;
			}
		}.runTaskTimer(blueprintsPlugin, 0, 1);
	}

	private void beginConversation() {
		ConversationFactory conversation = new ConversationFactory(blueprintsPlugin)
				.withLocalEcho(false)
				.withModality(false)
				.withTimeout(Settings.Block.BOSSBAR_DURATION)
				.withFirstPrompt(new ConfirmationPrompt(this))
				.addConversationAbandonedListener(conversationAbandonedEvent -> {
					if (!conversationAbandonedEvent.gracefulExit()) {
						Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eYour placement has been cancelled.");
						try {
							blueprintsPlugin.logs().addToLogs(player, block.getLocation(), schematic, "abandoned");
						} catch (IOException e) {
							e.printStackTrace();
						}
						player.getInventory().addItem(item);
						if (Settings.PLAY_SOUNDS)
							player.playSound(player.getLocation(), CompSound.LEVEL_UP.getSound(), 1F, 1F);
					}
					makeBossBarInvisible();
					clearErrorBlocks();
					clearFakeBlocks();
					blueprintsPlugin.removeBlueprint(player);
					runnable.cancel();
				});
		Conversation convo = conversation.buildConversation(new FormattedConversable(player));
		convo.begin();
	}

	private void makeBossBarInvisible() {
		bossBar.removePlayer(player);
	}

	public void cancel() {
		if (Settings.PLAY_SOUNDS)
			player.playSound(player.getLocation(), CompSound.LEVEL_UP.getSound(), 1F, 1F);
		Common.tell(player, placementDenied);
		makeBossBarInvisible();
		player.getInventory().addItem(item);

	}
}
