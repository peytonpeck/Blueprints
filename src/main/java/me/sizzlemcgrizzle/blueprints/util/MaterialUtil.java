package me.sizzlemcgrizzle.blueprints.util;

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class MaterialUtil {
    
    public static boolean isBanner(Material material) {
        switch (material) {
            case BLACK_BANNER:
            case BLACK_WALL_BANNER:
            case BLUE_BANNER:
            case BLUE_WALL_BANNER:
            case BROWN_BANNER:
            case BROWN_WALL_BANNER:
            case CYAN_BANNER:
            case CYAN_WALL_BANNER:
            case GRAY_BANNER:
            case GRAY_WALL_BANNER:
            case GREEN_BANNER:
            case GREEN_WALL_BANNER:
            case LIGHT_BLUE_BANNER:
            case LIGHT_BLUE_WALL_BANNER:
            case LIGHT_GRAY_BANNER:
            case LIGHT_GRAY_WALL_BANNER:
            case LIME_BANNER:
            case LIME_WALL_BANNER:
            case MAGENTA_BANNER:
            case MAGENTA_WALL_BANNER:
            case ORANGE_BANNER:
            case ORANGE_WALL_BANNER:
            case PINK_BANNER:
            case PINK_WALL_BANNER:
            case PURPLE_BANNER:
            case PURPLE_WALL_BANNER:
            case RED_BANNER:
            case RED_WALL_BANNER:
            case WHITE_BANNER:
            case WHITE_WALL_BANNER:
            case YELLOW_BANNER:
            case YELLOW_WALL_BANNER:
                return true;
            default:
                return false;
        }
    }
    
    public static BlockState getWoolColor(ChatColor color) {
        switch (color) {
            case AQUA:
                return BlockTypes.LIGHT_BLUE_WOOL.getDefaultState();
            case RED:
            case DARK_RED:
                return BlockTypes.RED_WOOL.getDefaultState();
            case BLUE:
            case DARK_BLUE:
                return BlockTypes.BLUE_WOOL.getDefaultState();
            case GOLD:
                return BlockTypes.ORANGE_WOOL.getDefaultState();
            case GRAY:
                return BlockTypes.LIGHT_GRAY_WOOL.getDefaultState();
            case BLACK:
                return BlockTypes.BLACK_WOOL.getDefaultState();
            case GREEN:
                return BlockTypes.LIME_WOOL.getDefaultState();
            case YELLOW:
                return BlockTypes.YELLOW_WOOL.getDefaultState();
            case DARK_AQUA:
                return BlockTypes.CYAN_WOOL.getDefaultState();
            case DARK_GRAY:
                return BlockTypes.GRAY_WOOL.getDefaultState();
            case DARK_GREEN:
                return BlockTypes.GREEN_WOOL.getDefaultState();
            case DARK_PURPLE:
                return BlockTypes.PURPLE_WOOL.getDefaultState();
            case LIGHT_PURPLE:
                return BlockTypes.MAGENTA_WOOL.getDefaultState();
            default:
                return BlockTypes.WHITE_WOOL.getDefaultState();
        }
    }
    
    public static BlockState getConcreteColor(ChatColor color) {
        switch (color) {
            case AQUA:
                return BlockTypes.LIGHT_BLUE_CONCRETE.getDefaultState();
            case RED:
            case DARK_RED:
                return BlockTypes.RED_CONCRETE.getDefaultState();
            case BLUE:
            case DARK_BLUE:
                return BlockTypes.BLUE_CONCRETE.getDefaultState();
            case GOLD:
                return BlockTypes.ORANGE_CONCRETE.getDefaultState();
            case GRAY:
                return BlockTypes.LIGHT_GRAY_CONCRETE.getDefaultState();
            case BLACK:
                return BlockTypes.BLACK_CONCRETE.getDefaultState();
            case GREEN:
                return BlockTypes.LIME_CONCRETE.getDefaultState();
            case YELLOW:
                return BlockTypes.YELLOW_CONCRETE.getDefaultState();
            case DARK_AQUA:
                return BlockTypes.CYAN_CONCRETE.getDefaultState();
            case DARK_GRAY:
                return BlockTypes.GRAY_CONCRETE.getDefaultState();
            case DARK_GREEN:
                return BlockTypes.GREEN_CONCRETE.getDefaultState();
            case DARK_PURPLE:
                return BlockTypes.PURPLE_CONCRETE.getDefaultState();
            case LIGHT_PURPLE:
                return BlockTypes.MAGENTA_CONCRETE.getDefaultState();
            default:
                return BlockTypes.WHITE_CONCRETE.getDefaultState();
        }
    }
    
    public static Material replaceWallMaterial(Material material) {
        if (material.name().contains("_WALL_BANNER")
                || material.name().contains("_WALL_SIGN")
                || material.name().contains("_WALL_HEAD")
                || material.name().contains("_WALL_SKULL")
                || material.name().contains("_WALL_FAN"))
            return Material.valueOf(material.name().replace("_WALL", ""));
        switch (material) {
            case WALL_TORCH:
                return Material.TORCH;
            case REDSTONE_WALL_TORCH:
                return Material.REDSTONE_TORCH;
            default:
                return material;
        }
    }
    
}
