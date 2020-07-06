package me.sizzlemcgrizzle.blueprints.util;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.sizzlemcgrizzle.blueprints.Blueprint;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.PlayerBlueprint;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
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
        
        BlueprintsPlugin.instance.getBlueprints().stream().filter(blueprint -> !(blueprint instanceof PlayerBlueprint))
                .forEach(blueprint -> listBlueprintMap.put(blueprint.getItem(), blueprint.getSchematic()));
        
        return listBlueprintMap;
        
    }
    
    public static List<String> getSchematics() {
        WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        File newFile = new File(worldEditPlugin.getDataFolder() + File.separator + "schematics");
        
        List<String> list = new ArrayList<>();
        
        if (!newFile.exists() || newFile.listFiles() == null) {
            return list;
        }
        
        for (File file : newFile.listFiles())
            list.add(file.getName());
        return list;
    }
    
    public static List<ItemStack> getBlueprint(String schematic) {
        return BlueprintsPlugin.instance.getBlueprints().stream().filter(blueprint -> blueprint.getSchematic().equals(schematic)).map(Blueprint::getItem).collect(Collectors.toList());
    }
    
    
}
