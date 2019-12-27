package me.sizzlemcgrizzle.blueprints.event;

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.ChatColor;

class ColorCache {
	BlockState getWoolColor(ChatColor color) {
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

	BlockState getConcreteColor(ChatColor color) {
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
}
