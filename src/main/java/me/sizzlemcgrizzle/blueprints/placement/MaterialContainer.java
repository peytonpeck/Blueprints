package me.sizzlemcgrizzle.blueprints.placement;

import me.sizzlemcgrizzle.blueprints.settings.Settings;
import me.sizzlemcgrizzle.blueprints.util.MaterialUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to store materials in a player blueprint in a nice, clean manner.
 */
public class MaterialContainer implements ConfigurationSerializable {
    private Map<Material, Integer> materialMap;
    
    public MaterialContainer(BoundingBox box, World world) {
        calculateMaterialMap(box, world);
    }
    
    public MaterialContainer(Map<String, Object> map) {
        materialMap = new HashMap<>();
        
        map.forEach((k, v) -> {
            try {
                materialMap.put(Material.valueOf(k), (Integer) v);
            } catch (IllegalArgumentException ignored) {
            
            }
        });
    }
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        materialMap.forEach((k, v) -> map.put(k.name(), v));
        return map;
    }
    
    private void calculateMaterialMap(BoundingBox box, World world) {
        materialMap = new HashMap<>();
        for (double x = box.getMinX(); x <= box.getMaxX(); x++)
            for (double y = box.getMinY(); y <= box.getMaxY(); y++)
                for (double z = box.getMinZ(); z <= box.getMaxZ(); z++) {
                    Material material = (new Location(world, x, y, z)).getBlock().getType();
                    if (material == Material.AIR || material == Material.WATER || material == Material.LAVA)
                        continue;
                    if (material.name().contains("WALL"))
                        material = MaterialUtil.replaceWallMaterial(material);
                    materialMap.compute(material, (k, v) -> materialMap.containsKey(k) ? materialMap.get(k) + 1 : 1);
                }
    }
    
    public double getCost() {
        double cost = 0.0;
        for (Map.Entry<Material, Integer> entry : materialMap.entrySet()) {
            Material material = entry.getKey();
            int amount = entry.getValue();
            double multiplier = Settings.PlayerBlueprint.PLAYER_BLUEPRINT_MATERIAL_PRICE_MULTIPLIER.getOrDefault(material, Settings.PlayerBlueprint.PLAYER_BLUEPRINT_PRICE_MULTIPLIER);
            
            cost += multiplier * amount;
        }
        
        return cost;
    }
    
    public Map<Material, Integer> getMaterialMap() {
        return materialMap;
    }
}
