package me.sizzlemcgrizzle.blueprints.util;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public class MaterialUtil {
	public static boolean isSign(Material material) {
		switch (material) {
			case ACACIA_SIGN:
			case SPRUCE_SIGN:
			case SPRUCE_WALL_SIGN:
			case ACACIA_WALL_SIGN:
			case BIRCH_SIGN:
			case BIRCH_WALL_SIGN:
			case DARK_OAK_SIGN:
			case DARK_OAK_WALL_SIGN:
			case JUNGLE_SIGN:
			case JUNGLE_WALL_SIGN:
			case OAK_SIGN:
			case OAK_WALL_SIGN:
				return true;
			default:
				return false;
		}
	}

	public static DyeColor getDyeColor(Material material) {
		switch (material) {
			case BLACK_BANNER:
			case BLACK_WALL_BANNER:
				return DyeColor.BLACK;
			case BLUE_BANNER:
			case BLUE_WALL_BANNER:
				return DyeColor.BLUE;
			case BROWN_BANNER:
			case BROWN_WALL_BANNER:
				return DyeColor.BROWN;
			case CYAN_BANNER:
			case CYAN_WALL_BANNER:
				return DyeColor.CYAN;
			case GRAY_BANNER:
			case GRAY_WALL_BANNER:
				return DyeColor.GRAY;
			case GREEN_BANNER:
			case GREEN_WALL_BANNER:
				return DyeColor.GREEN;
			case LIGHT_BLUE_BANNER:
			case LIGHT_BLUE_WALL_BANNER:
				return DyeColor.LIGHT_BLUE;
			case LIGHT_GRAY_BANNER:
			case LIGHT_GRAY_WALL_BANNER:
				return DyeColor.LIGHT_GRAY;
			case LIME_BANNER:
			case LIME_WALL_BANNER:
				return DyeColor.LIME;
			case MAGENTA_BANNER:
			case MAGENTA_WALL_BANNER:
				return DyeColor.MAGENTA;
			case ORANGE_BANNER:
			case ORANGE_WALL_BANNER:
				return DyeColor.ORANGE;
			case PINK_BANNER:
			case PINK_WALL_BANNER:
				return DyeColor.PINK;
			case PURPLE_BANNER:
			case PURPLE_WALL_BANNER:
				return DyeColor.PURPLE;
			case RED_BANNER:
			case RED_WALL_BANNER:
				return DyeColor.RED;
			case YELLOW_BANNER:
			case YELLOW_WALL_BANNER:
				return DyeColor.YELLOW;
			default:
				return DyeColor.WHITE;
		}
	}

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

	public static boolean isHead(Material material) {
		switch (material) {
			case PLAYER_HEAD:
			case CREEPER_HEAD:
			case CREEPER_WALL_HEAD:
			case DRAGON_HEAD:
			case DRAGON_WALL_HEAD:
			case PISTON_HEAD:
			case PLAYER_WALL_HEAD:
			case ZOMBIE_HEAD:
			case ZOMBIE_WALL_HEAD:
				return true;
			default:
				return false;
		}
	}
}
