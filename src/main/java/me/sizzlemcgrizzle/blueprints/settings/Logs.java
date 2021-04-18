package me.sizzlemcgrizzle.blueprints.settings;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logs {
    
    public static void addToLogs(BlueprintsPlugin plugin, Player player, Location location, String schematic, String reason) {
        File file = new File(plugin.getDataFolder(), "logs.txt");
        
        try {
            if (!file.exists())
                file.createNewFile();
            
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(plugin.getDataFolder() + "/logs.txt", true)
            );
            writer.newLine();
            writer.write(player.getUniqueId() + " (" + player.getName() + ")"
                    + " placed " + schematic
                    + " at location (" + (int) location.getX() + ", " + (int) location.getY() + ", " + (int) location.getZ() + ") at time ("
                    + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()) + ")."
                    + " Placement was " + reason + ".");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
