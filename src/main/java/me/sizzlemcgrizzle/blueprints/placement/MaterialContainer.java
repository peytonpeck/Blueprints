package me.sizzlemcgrizzle.blueprints.placement;

import de.craftlancer.core.menu.ConditionalPagedMenu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MaterialUtil;
import de.craftlancer.core.util.Tuple;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Used to store materials in a player blueprint in a nice, clean manner.
 */
public class MaterialContainer implements ConfigurationSerializable {
    private Map<Material, Integer> materialMap;
    private ConditionalPagedMenu menu;
    
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
        
        materialMap.entrySet().removeIf(entry -> !entry.getKey().isItem());
    }
    
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
                    if (material == Material.AIR
                            || material == Material.WATER
                            || material == Material.LAVA
                            || material == Material.RED_MUSHROOM_BLOCK
                            || material == Material.BROWN_MUSHROOM_BLOCK
                            || !material.isItem())
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
    
    public void display(Player player) {
        if (menu == null)
            createMenu();
        
        menu.display(player, ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default");
    }
    
    private void createMenu() {
        this.menu = new ConditionalPagedMenu(BlueprintsPlugin.getInstance(), 6, getPageItems(), true, true,
                Arrays.asList(new Tuple<>("default", "Materials"),
                        new Tuple<>("resource", "§f" + TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "\uE300" + TranslateSpaceFont.getSpecificAmount(-165) + "§8Materials")));
        
        menu.setInventoryUpdateHandler(c -> c.fillBorders(new MenuItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("").build()), false, "default"));
        menu.setInfoItem(getInfoItem());
    }
    
    private List<MenuItem> getPageItems() {
        return materialMap.entrySet().stream().map(entry -> new MenuItem(new ItemBuilder(entry.getKey())
                .setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "ITEM: " + ChatColor.AQUA + entry.getKey().name().toLowerCase().replace('_', ' '))
                .setLore("",
                        ChatColor.GRAY + "Amount needed: " + ChatColor.GREEN + entry.getValue(),
                        ChatColor.GRAY + "Total material price: " + ChatColor.GREEN + "$" + Settings.PlayerBlueprint.PLAYER_BLUEPRINT_MATERIAL_PRICE_MULTIPLIER
                                .getOrDefault(entry.getKey(), Settings.PlayerBlueprint.PLAYER_BLUEPRINT_PRICE_MULTIPLIER) * entry.getValue()
                )
                .setAmount(Math.min(entry.getKey().getMaxStackSize(), entry.getValue())).build())).collect(Collectors.toList());
    }
    
    private MenuItem getInfoItem() {
        return new MenuItem(new ItemBuilder(Material.STONE).setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "What is this?")
                .setLore("",
                        ChatColor.GRAY + "This page shows all the materials",
                        ChatColor.GRAY + "involved in creating this player",
                        ChatColor.GRAY + "blueprint.")
                .setCustomModelData(5)
                .build());
    }
}
