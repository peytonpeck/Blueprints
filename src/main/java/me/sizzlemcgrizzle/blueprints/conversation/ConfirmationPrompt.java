package me.sizzlemcgrizzle.blueprints.conversation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;

import java.util.HashMap;
import java.util.Map;

/*
 * Made by SydMontague:
 * https://github.com/SydMontague/CLCore/tree/craftcitizen/src/main/java/de/craftlancer/core/conversation
 */
public class ConfirmationPrompt extends ClickableBooleanPrompt {

	private static String placementDenied = Settings.Messages.MESSAGE_PREFIX + "&eYou have cancelled the placement.";
	private static String placementAccepted = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BUILD_SUCCESS;

	private Player player;
	private ItemStack item;
	private HashMap<Location, BlockData> ghostBlockMap;
	private Clipboard clipboard;
	private Location location;

	public ConfirmationPrompt(Player player, ItemStack item, HashMap<Location, BlockData> ghostBlockMap, Clipboard clipboard, Location location) {
		super(ChatColor.YELLOW + "Do you want to place this blueprint here? Click:");

		this.player = player;
		this.item = item.clone();
		this.item.setAmount(1);
		this.ghostBlockMap = (HashMap<Location, BlockData>) ghostBlockMap.clone();
		this.clipboard = clipboard;
		this.location = location;
	}

	@Override
	protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, boolean input) {
		if (input) {
			for (Map.Entry<Location, BlockData> entry : ghostBlockMap.entrySet()) {
				if (entry.getValue().getMaterial() == entry.getKey().getBlock().getType())
					player.sendBlockChange(entry.getKey(), entry.getValue());
			}
			ghostBlockMap.clear();
			try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(location.getWorld()), -1)) {
				Operation operation = new ClipboardHolder(clipboard)
						.createPaste(editSession)
						.to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
						.ignoreAirBlocks(true)
						.copyEntities(false)
						.copyBiomes(false)
						.build();

				Operations.complete(operation);
				if (Settings.PLAY_SOUNDS)
					player.playSound(player.getLocation(), CompSound.ANVIL_USE.getSound(), 1F, 0.7F);
				Common.tell(player, placementAccepted);
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		} else {
			for (Map.Entry<Location, BlockData> entry : ghostBlockMap.entrySet()) {
				if (entry.getValue().getMaterial() == entry.getKey().getBlock().getType())
					player.sendBlockChange(entry.getKey(), entry.getValue());
			}
			ghostBlockMap.clear();
			player.getInventory().addItem(item);
			Common.tell(player, placementDenied);


		}
		return Prompt.END_OF_CONVERSATION;
	}

}
