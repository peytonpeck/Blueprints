package me.sizzlemcgrizzle.blueprints.settings;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.SimpleSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Settings extends SimpleSettings {
    @Override
    protected int getConfigVersion() {
        return 0;
    }
    
    @Override
    protected String[] getHeader() {
        String[] header = new String[6];
        header[0] = " ------------------------------------------------------------------------------------------- #";
        header[1] = "                                                                                             #";
        header[2] = " Welcome to the settings file for Blueprints.                                                #";
        header[3] = " For documentation, visit: https://github.com/SizzleMcGrizzle/Blueprints/wiki/settings.yml   #";
        header[4] = "                                                                                             #";
        header[5] = " ------------------------------------------------------------------------------------------- #";
        return header;
    }
    
    public static Boolean PLAY_SOUNDS;
    public static List<String> TYPES;
    public static Boolean USE_ECONOMY;
    
    private static void init() {
        pathPrefix(null);
        PLAY_SOUNDS = getBoolean("Play_Sounds");
        TYPES = getStringList("Blueprint_Types");
        USE_ECONOMY = getBoolean("Use_Economy");
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
        
        private static void init() {
            pathPrefix("Messages");
            MESSAGE_PREFIX = getString("Message_Prefix");
            RELOAD_SUCCESS = getString("Reload_Success");
            BUILD_SUCCESS = getString("Build_Success");
            SHOW_ERROR_TRUE_MESSAGE = getString("Show_Error_True_Message");
            SHOW_ERROR_FALSE_MESSAGE = getString("Show_Error_False_Message");
            PLACEMENT_DENIED = getString("Placement_Denied");
            IS_CONVERSING = getString("Is_Conversing");
            INVALID_SCHEMATIC = getString("Invalid_Schematic");
            ABOVE_Y_255 = getString("Above_Y_255");
        }
        
    }
    
    public static class PlayerBlueprint {
        public static List<String> LIMITS;
        public static Integer PLAYER_BLUEPRINT_MAX_SIZE;
        public static Double PLAYER_BLUEPRINT_PRICE_MULTIPLIER;
        public static Map<Material, Double> PLAYER_BLUEPRINT_MATERIAL_PRICE_MULTIPLIER;
        
        private static void init() {
            pathPrefix("Player_Blueprint");
            
            LIMITS = getStringList("Player_Blueprint_Limits");
            LIMITS.forEach(limit -> {
                if (Bukkit.getPluginManager().getPermission(limit) == null)
                    Bukkit.getPluginManager().addPermission(new Permission(limit));
            });
            PLAYER_BLUEPRINT_MAX_SIZE = getInteger("Player_Blueprint_Max_Size");
            PLAYER_BLUEPRINT_PRICE_MULTIPLIER = getDouble("Player_Blueprint_Price_Multiplier");
            ConfigurationSection section = getConfig().getConfigurationSection("Player_Blueprint").getConfigurationSection("Player_Blueprint_Material_Price_Multiplier");
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
        
        private static void init() {
            pathPrefix("Block");
            
            SHOW_ERROR_PREVIEW = getBoolean("Show_Error_Preview");
            BLOCK_TIMEOUT = getInteger("Block_Timeout");
            ERROR_BLOCK = Material.getMaterial(getString("Error_Block"));
            IGNORE_BLOCKS = getMaterialList("Ignore_Blocks").getSource().stream().map(CompMaterial::getMaterial).collect(Collectors.toList());
            NO_PLACE_OUTSIDE_CLAIMS = getBoolean("No_Place_Outside_Claims");
            NO_PLACE_BLOCK_IN_WAY = getBoolean("No_Place_Block_In_Way");
            NO_PLACE_ADMIN_CLAIM = getBoolean("No_Place_Admin_Claim");
            BOSSBAR_DURATION = getInteger("Bossbar_Duration");
            
        }
    }
    
    
}
