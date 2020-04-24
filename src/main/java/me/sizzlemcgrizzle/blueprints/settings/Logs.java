package me.sizzlemcgrizzle.blueprints.settings;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mineacademy.fo.TimeUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logs {
    
    public static void addToLogs(Player player, Location location, String schematic, String reason) throws IOException {
        File file = new File(BlueprintsPlugin.getData().getAbsolutePath() + File.separator + "/logs.txt");
        
        if (!file.exists())
            file.createNewFile();
        
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(BlueprintsPlugin.getData().getAbsolutePath() + File.separator + "/logs.txt", true)
        );
        writer.newLine();
        writer.write(player.getUniqueId() + " (" + player.getName() + ")"
                + " placed " + schematic
                + " at location (" + (int) location.getX() + ", " + (int) location.getY() + ", " + (int) location.getZ() + ") at time ("
                + TimeUtil.getFormattedDate() + ")."
                + " Placement was " + reason + ".");
        writer.close();
        
    }
}
