package me.sizzlemcgrizzle.blueprints.util;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.Blueprint;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SchematicUtil {
    
    private static YamlConfiguration config;
    
    /**
     * Gets a map of all the blueprints
     *
     * @return a map containing each blueprint's ItemStack and schematic name
     */
    public static HashMap<ItemStack, String> listBlueprints() {
        
        HashMap<ItemStack, String> listBlueprintMap = new HashMap<>();
        
        BlueprintsPlugin.getInstance().getBlueprints().stream().filter(blueprint -> !(blueprint instanceof PlayerBlueprint))
                .forEach(blueprint -> listBlueprintMap.put(blueprint.getItem(), blueprint.getSchematic()));
        
        return listBlueprintMap;
        
    }
    
    public static List<String> getSchematics() {
        File newFile = new File(Bukkit.getPluginManager().getPlugin("WorldEdit").getDataFolder(), "schematics");
        
        return !newFile.exists() || newFile.listFiles() == null
                ? new ArrayList<>()
                : Arrays.stream(newFile.listFiles()).map(File::getName).collect(Collectors.toList());
    }
    
    public static List<ItemStack> getBlueprint(String schematic) {
        return BlueprintsPlugin.getInstance().getBlueprints().stream().filter(blueprint -> blueprint.getSchematic().equals(schematic)).map(Blueprint::getItem).collect(Collectors.toList());
    }
    
    
}
