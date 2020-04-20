package me.sizzlemcgrizzle.blueprints.api;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called when a blueprint placement is confirmed.
 **/
public class BlueprintPostPasteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private String type;
    private Player player;
    private String schematic;
    private GameMode gamemode;
    private ItemStack item;
    private Location location;
    private List<Location> pasteSet;
    
    public BlueprintPostPasteEvent(String type, Player player, String schematic, GameMode gamemode, ItemStack item, Location location, List<Location> set) {
        this.type = type;
        this.player = player;
        this.schematic = schematic;
        this.gamemode = gamemode;
        this.item = item;
        this.location = location;
        this.pasteSet = set;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    /**
     * @return "NORMAL", "PORTAL", or "STONECRUSHER"
     **/
    public String getType() {
        return type;
    }
    
    /**
     * @return Origin block (lecterns for portals)
     */
    public Location getFeatureLocation() {
        return location;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    /**
     * @return Name of the schematic file
     */
    public String getSchematic() {
        return schematic;
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public GameMode getGamemode() {
        return gamemode;
    }
    
    /**
     * @return A list of ALL blocks pasted, including the origin block
     */
    public List<Location> getBlocksPasted() {
        return pasteSet;
    }
}
