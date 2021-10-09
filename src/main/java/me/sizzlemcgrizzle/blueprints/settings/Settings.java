package me.sizzlemcgrizzle.blueprints.settings;

import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Settings {
    
    public static Boolean PLAY_SOUNDS;
    public static Boolean USE_ECONOMY;
    
    public static void load(BlueprintsPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "settings.yml");
        
        if (!file.exists())
            plugin.saveResource("settings.yml", false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        init(config);
        Block.init(config.getConfigurationSection("Block"));
        Messages.init(config.getConfigurationSection("Messages"));
        PlayerBlueprint.init(config.getConfigurationSection("Player_Blueprint"));
        
        MessageUtil.register(plugin, new TextComponent(Settings.Messages.MESSAGE_PREFIX));
        
    }
    
    private static void init(ConfigurationSection config) {
        PLAY_SOUNDS = config.getBoolean("Play_Sounds");
        USE_ECONOMY = config.getBoolean("Use_Economy");
    }
    
    public static class Messages {
        public static String MESSAGE_PREFIX;
        public static String RELOAD_SUCCESS;
        public static String BUILD_SUCCESS;
        public static String SHOW_ERROR_TRUE_MESSAGE;
        public static String SHOW_ERROR_FALSE_MESSAGE;
        public static String PLACEMENT_DENIED;
        public static String IS_CONVERSING;
        public static String INVALID_SCHEMATIC;
        public static String ABOVE_Y_255;
        
        private static void init(ConfigurationSection config) {
            MESSAGE_PREFIX = ChatColor.translateAlternateColorCodes('&', config.getString("Message_Prefix"));
            RELOAD_SUCCESS = ChatColor.translateAlternateColorCodes('&', config.getString("Reload_Success"));
            BUILD_SUCCESS = ChatColor.translateAlternateColorCodes('&', config.getString("Build_Success"));
            SHOW_ERROR_TRUE_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("Show_Error_True_Message"));
            SHOW_ERROR_FALSE_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("Show_Error_False_Message"));
            PLACEMENT_DENIED = ChatColor.translateAlternateColorCodes('&', config.getString("Placement_Denied"));
            IS_CONVERSING = ChatColor.translateAlternateColorCodes('&', config.getString("Is_Conversing"));
            INVALID_SCHEMATIC = ChatColor.translateAlternateColorCodes('&', config.getString("Invalid_Schematic"));
            ABOVE_Y_255 = ChatColor.translateAlternateColorCodes('&', config.getString("Above_Y_255"));
        }
        
    }
    
    public static class PlayerBlueprint {
        public static List<String> LIMITS;
        public static List<Material> DISABLED_MATERIALS;
        public static Integer PLAYER_BLUEPRINT_MAX_SIZE;
        public static Double PLAYER_BLUEPRINT_PRICE_MULTIPLIER;
        public static Map<Material, Double> PLAYER_BLUEPRINT_MATERIAL_PRICE_MULTIPLIER;
        
        private static void init(ConfigurationSection config) {
            LIMITS = config.getStringList("Player_Blueprint_Limits");
            LIMITS.stream().filter(limit -> Bukkit.getPluginManager().getPermission(limit) == null)
                    .forEach(limit -> Bukkit.getPluginManager().addPermission(new Permission(limit)));
            List<String> disabledStringList = config.getStringList("Disabled_Materials");
            DISABLED_MATERIALS = new ArrayList<>();
            disabledStringList.removeIf(s -> {
                try {
                    DISABLED_MATERIALS.add(Material.valueOf(s));
                } catch (IllegalArgumentException e) {
                    return false;
                }
                return true;
            });
            disabledStringList.forEach(s -> DISABLED_MATERIALS
                    .addAll(Arrays.stream(Material.values()).filter(material -> material.name().contains(s)).collect(Collectors.toList())));
            
            PLAYER_BLUEPRINT_MAX_SIZE = config.getInt("Player_Blueprint_Max_Size");
            PLAYER_BLUEPRINT_PRICE_MULTIPLIER = config.getDouble("Player_Blueprint_Price_Multiplier");
            ConfigurationSection section = config.getConfigurationSection("Player_Blueprint_Material_Price_Multiplier");
            PLAYER_BLUEPRINT_MATERIAL_PRICE_MULTIPLIER = new HashMap<>();
            for (String key : section.getKeys(false))
                PLAYER_BLUEPRINT_MATERIAL_PRICE_MULTIPLIER.put(Material.valueOf(key), section.getDouble(key));
        }
    }
    
    public static class Block {
        public static Boolean SHOW_ERROR_PREVIEW;
        public static Integer BLOCK_TIMEOUT;
        public static Material ERROR_BLOCK;
        public static List<Material> IGNORE_BLOCKS;
        public static Boolean NO_PLACE_OUTSIDE_CLAIMS;
        public static Boolean NO_PLACE_BLOCK_IN_WAY;
        public static Boolean NO_PLACE_ADMIN_CLAIM;
        public static Integer BOSSBAR_DURATION;
        
        private static void init(ConfigurationSection config) {
            SHOW_ERROR_PREVIEW = config.getBoolean("Show_Error_Preview");
            BLOCK_TIMEOUT = config.getInt("Block_Timeout");
            ERROR_BLOCK = Material.getMaterial(config.getString("Error_Block"));
            IGNORE_BLOCKS = config.getStringList("Ignore_Blocks").stream().map(Material::valueOf).collect(Collectors.toList());
            NO_PLACE_OUTSIDE_CLAIMS = config.getBoolean("No_Place_Outside_Claims");
            NO_PLACE_BLOCK_IN_WAY = config.getBoolean("No_Place_Block_In_Way");
            NO_PLACE_ADMIN_CLAIM = config.getBoolean("No_Place_Admin_Claim");
            BOSSBAR_DURATION = config.getInt("Bossbar_Duration");
            
        }
    }
    
    
}
