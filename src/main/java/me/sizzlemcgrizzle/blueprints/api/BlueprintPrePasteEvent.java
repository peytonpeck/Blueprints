package me.sizzlemcgrizzle.blueprints.api;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a blueprint placement is confirmed.
 **/
public class BlueprintPrePasteEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private String type;
	private Player player;
	private String schematic;
	private GameMode gamemode;
	private ItemStack item;
	private Location location;

	private boolean isCancelled = false;

	public BlueprintPrePasteEvent(String type, Player player, String schematic, GameMode gamemode, ItemStack item, Location location) {
		this.type = type;
		this.player = player;
		this.schematic = schematic;
		this.gamemode = gamemode;
		this.item = item;
		this.location = location;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		this.isCancelled = b;
	}

	public String getType() {
		return type;
	}

	public Location getFeatureLocation() {
		return location;
	}

	public Player getPlayer() {
		return player;
	}

	public String getSchematic() {
		return schematic;
	}

	public ItemStack getItem() {
		return item;
	}

	public GameMode getGamemode() {
		return gamemode;
	}
}
