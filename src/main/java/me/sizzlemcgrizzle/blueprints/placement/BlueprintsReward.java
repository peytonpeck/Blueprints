package me.sizzlemcgrizzle.blueprints.placement;

import me.sizzlemcgrizzle.blueprints.util.SchematicUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mineacademy.fo.Common;

import java.util.HashMap;
import java.util.Map;

public class BlueprintsReward implements ConfigurationSerializable {
    private String id;
    private String baseSchematic;
    private String titleMessage = "";
    private String subtitleMessage = "";
    private String chatMessage = "";
    private String rewardSchematic;
    
    public BlueprintsReward(String baseSchematic, String rewardSchematic, String id) {
        this.baseSchematic = baseSchematic;
        this.rewardSchematic = rewardSchematic;
        this.id = id;
    }
    
    public BlueprintsReward(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.baseSchematic = (String) map.get("baseSchematic");
        this.titleMessage = (String) map.get("titleMessage");
        this.subtitleMessage = (String) map.get("subtitleMessage");
        this.chatMessage = (String) map.get("chatMessage");
        this.rewardSchematic = (String) map.get("rewardSchematic");
    }
    
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("id", id);
        map.put("baseSchematic", baseSchematic);
        map.put("titleMessage", titleMessage);
        map.put("subtitleMessage", subtitleMessage);
        map.put("chatMessage", chatMessage);
        map.put("rewardSchematic", rewardSchematic);
        
        return map;
    }
    
    public void reward(Player player) {
        if (!chatMessage.equals(""))
            Common.tell(player, chatMessage);
        if (!subtitleMessage.equals("") || !titleMessage.equals(""))
            player.sendTitle(ChatColor.translateAlternateColorCodes('&', titleMessage),
                    ChatColor.translateAlternateColorCodes('&', subtitleMessage),
                    10,
                    70,
                    20);
        ItemStack item = SchematicUtil.getBlueprint(rewardSchematic).get(0);
        if (item != null)
            player.getInventory().addItem(item).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
    }
    
    public String getBaseSchematic() {
        return baseSchematic;
    }
    
    public String getId() {
        return id;
    }
    
    public String getTitleMessage() {
        return titleMessage;
    }
    
    public void setTitleMessage(String titleMessage) {
        this.titleMessage = titleMessage;
    }
    
    public String getSubtitleMessage() {
        return subtitleMessage;
    }
    
    public void setSubtitleMessage(String subtitleMessage) {
        this.subtitleMessage = subtitleMessage;
    }
    
    public String getChatMessage() {
        return chatMessage;
    }
    
    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }
}
