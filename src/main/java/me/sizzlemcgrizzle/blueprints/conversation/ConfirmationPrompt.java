package me.sizzlemcgrizzle.blueprints.conversation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/*
 * Made by SydMontague:
 * https://github.com/SydMontague/CLCore/tree/craftcitizen/src/main/java/de/craftlancer/core/conversation
 */
public class ConfirmationPrompt extends NewPrompt {

	private static String placementDenied = Settings.Messages.MESSAGE_PREFIX + "&eYou have cancelled the placement and your item has been returned.";
	private static String placementAccepted = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BUILD_SUCCESS;

	private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");

	private Player player;
	private ItemStack item;
	private HashMap<Player, Set<Location>> pasteFakeBlockMap;
	private Location blockLocation;
	private String schematic;
	private BossBar bossBar;
	private ClipboardHolder holder;
	private Set<Location> previewLocationSet = new HashSet<>();
	private String[] yes = new String[]{"yes", "1", "true", "y", "correct", "valid"};
	private String[] no = new String[]{"no", "0", "false", "n", "wrong", "invalid"};

	private int counter = 0;

	public ConfirmationPrompt(Player player, ItemStack item, HashMap<Player, Set<Location>> ghostBlockMap, Clipboard clipboard, Location location, String schematic, BossBar bossBar) {
		super(ChatColor.YELLOW + "Place blueprint?");

		this.player = player;
		this.item = item.clone();
		this.item.setAmount(1);
		this.pasteFakeBlockMap = (HashMap<Player, Set<Location>>) ghostBlockMap.clone();
		this.blockLocation = location;
		this.schematic = schematic;
		this.bossBar = bossBar;
		this.holder = new ClipboardHolder(clipboard);
	}

	private void completeFakeOperation() {
		for (Location location : pasteFakeBlockMap.get(player)) {
			player.sendBlockChange(location, location.getBlock().getBlockData());
		}
		pasteFakeBlockMap.remove(player);

		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(blockLocation.getWorld()), -1)) {
			Operation previewOperation = holder.createPaste(new AbstractDelegateExtent(editSession) {
				@Override
				public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) {
					com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
					wePlayer.sendFakeBlock(location, block);
					previewLocationSet.add(new Location(blockLocation.getWorld(), location.getX(), location.getY(), location.getZ()));
					return true;
				}
			})
					.to(BlockVector3.at(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ()))
					.copyBiomes(false)
					.copyEntities(false)
					.ignoreAirBlocks(true)
					.build();
			Operations.complete(previewOperation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}

		if (previewLocationSet.size() != 0) {
			pasteFakeBlockMap.put(player, previewLocationSet);
		}
	}

	@Override
	protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
		if (ArrayUtils.contains(yes, input.toLowerCase())) {
			for (Location location : pasteFakeBlockMap.get(player)) {
				player.sendBlockChange(location, location.getBlock().getBlockData());
			}
			pasteFakeBlockMap.remove(player);
			try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(blockLocation.getWorld()), -1)) {
				Operation operation = holder
						.createPaste(editSession)
						.to(BlockVector3.at(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ()))
						.ignoreAirBlocks(true)
						.copyEntities(false)
						.copyBiomes(false)
						.build();

				Operations.complete(operation);
				blueprintsPlugin.logs().addToLogs(player, blockLocation, schematic, "confirmed");
				bossBar.removePlayer(player);
				if (Settings.PLAY_SOUNDS)
					player.playSound(player.getLocation(), CompSound.ANVIL_USE.getSound(), 1F, 0.7F);
				Common.tell(player, placementAccepted);
			} catch (WorldEditException | IOException e) {
				e.printStackTrace();
			}
			return Prompt.END_OF_CONVERSATION;


		} else if (ArrayUtils.contains(no, input.toLowerCase())) {
			for (Location location : pasteFakeBlockMap.get(player)) {
				player.sendBlockChange(location, location.getBlock().getBlockData());
			}
			pasteFakeBlockMap.remove(player);

			try {
				blueprintsPlugin.logs().addToLogs(player, blockLocation, schematic, "denied");
			} catch (IOException e) {
				e.printStackTrace();
			}
			bossBar.removePlayer(player);
			player.getInventory().addItem(item);
			Common.tell(player, placementDenied);
			if (Settings.PLAY_SOUNDS)
				player.playSound(player.getLocation(), CompSound.LEVEL_UP.getSound(), 1F, 0.7F);
			return Prompt.END_OF_CONVERSATION;


		} else if (input.equalsIgnoreCase("right")) {
			counter += 1;
			holder.setTransform(new AffineTransform().rotateY(counter * 90));
			//Create new operation to show preview
			completeFakeOperation();
			return this;


		} else if (input.equalsIgnoreCase("left")) {
			counter -= 1;
			holder.setTransform(new AffineTransform().rotateY(counter * -90));
			//Create new operation to show preview
			completeFakeOperation();
			return this;
		}
		return Prompt.END_OF_CONVERSATION;
	}
}

