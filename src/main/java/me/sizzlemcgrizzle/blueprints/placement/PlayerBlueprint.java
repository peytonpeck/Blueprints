package me.sizzlemcgrizzle.blueprints.placement;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class PlayerBlueprint extends Blueprint {
    
    private MaterialContainer materialContainer;
    private UUID originalOwner;
    private UUID owner;
    private double cost;
    
    public PlayerBlueprint(ItemStack item, String schematic, String type, UUID owner, MaterialContainer container) {
        super(item, schematic, type, true, true, false);
        
        this.originalOwner = owner;
        this.owner = owner;
        this.materialContainer = container;
        setCost();
    }
    
    public PlayerBlueprint(Map<String, Object> map) {
        super(map);
        
        this.owner = UUID.fromString((String) map.get("owner"));
        this.originalOwner = UUID.fromString((String) map.getOrDefault("originalOwner", owner.toString()));
        this.cost = (double) map.get("cost");
        this.materialContainer = (MaterialContainer) map.get("materialContainer");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("originalOwner", originalOwner.toString());
        map.put("owner", owner.toString());
        map.put("cost", cost);
        map.put("materialContainer", materialContainer);
        
        return map;
    }
    
    @Override
    public ClipboardHolder getHolder(Player player) {
        Clipboard copy = getClipboard();
        return new ClipboardHolder(copy);
    }
    
    @Override
    protected File getSchematicFile() {
        return new File(BlueprintsPlugin.getInstance().getDataFolder(), "/playerblueprints/" + getSchematic());
    }
    
    private void setCost() {
        cost = materialContainer.getCost();
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public double getCost() {
        return cost;
    }
    
    public MaterialContainer getMaterialContainer() {
        return materialContainer;
    }
    
    public void remove() {
        owner = UUID.randomUUID();
    }
    
    public boolean charge(Player player) {
        if (!Settings.USE_ECONOMY)
            return true;
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        
        if (!BlueprintsPlugin.getEconomy().has(offlinePlayer, getCost())) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.5F, 1F);
            MessageUtil.sendMessage(BlueprintsPlugin.getInstance(), player, MessageLevel.INFO, "You cannot afford the §6$" + getCost() + " §efor this blueprint!");
            return false;
        } else {
            BlueprintsPlugin.getEconomy().withdrawPlayer(offlinePlayer, getCost());
            MessageUtil.sendMessage(BlueprintsPlugin.getInstance(), player, MessageLevel.SUCCESS, "You have purchased this blueprint for §2$" + getCost() + "§a!");
            return true;
        }
    }
    
    public static int getAmount(Player player) {
        return (int) BlueprintsPlugin.getInstance().getPlayerBlueprints().stream().filter(b -> b.getOwner().equals(player.getUniqueId())).count();
    }
    
    public static int getLimit(Player player) {
        int limit = -1;
        
        for (String l : Settings.PlayerBlueprint.LIMITS) {
            if (!player.hasPermission(l))
                continue;
            
            int num = Integer.parseInt(l.split("blueprints.placement.")[1]);
            
            if (num == -1)
                return -1;
            if (num > limit)
                limit = num;
        }
        return limit;
    }
    
    @Override
    public boolean canCopyEntities() {
        return false;
    }
}
