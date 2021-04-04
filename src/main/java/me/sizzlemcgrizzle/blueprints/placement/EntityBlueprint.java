package me.sizzlemcgrizzle.blueprints.placement;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EntityBlueprint extends Blueprint {
    
    private boolean canRotate;
    private boolean canRotate45Degrees;
    
    public EntityBlueprint(ItemStack item, String schematic, String type, boolean canRotate, boolean canRotate45Degrees) {
        super(item, schematic, type);
        
        this.canRotate45Degrees = canRotate45Degrees;
        this.canRotate = canRotate;
        System.out.println(canRotate45Degrees);
    }
    
    public EntityBlueprint(Map<String, Object> map) {
        super(map);
        
        this.canRotate = (boolean) map.get("canRotate");
        this.canRotate45Degrees = (boolean) map.get("canRotate45Degrees");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("canRotate", canRotate);
        map.put("canRotate45Degrees", canRotate45Degrees);
        
        return map;
    }
    
    public boolean canRotate() {
        return canRotate;
    }
    
    public boolean canRotate45Degrees() {
        return canRotate45Degrees;
    }
}
