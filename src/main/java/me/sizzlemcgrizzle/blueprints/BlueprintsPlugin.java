package me.sizzlemcgrizzle.blueprints;

import me.sizzlemcgrizzle.blueprints.newcommand.BlueprintsCommandHandler;
import me.sizzlemcgrizzle.blueprints.placement.Blueprint;
import me.sizzlemcgrizzle.blueprints.placement.BlueprintListener;
import me.sizzlemcgrizzle.blueprints.placement.InventoryLink;
import me.sizzlemcgrizzle.blueprints.placement.MaterialContainer;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprintMenu;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlueprintsPlugin extends JavaPlugin {
    
    private final File blueprintFile = new File(getDataFolder(), "blueprints.yml");
    
    private static BlueprintsPlugin instance;
    private static Economy econ = null;
    
    private List<Blueprint> blueprints;
    private List<InventoryLink> inventoryLinks = new ArrayList<>();
    private Map<UUID, PlayerBlueprintMenu> playerBlueprintMenus = new HashMap<>();
    
    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(Blueprint.class);
        ConfigurationSerialization.registerClass(PlayerBlueprint.class);
        ConfigurationSerialization.registerClass(MaterialContainer.class);
        
        instance = this;
        
        Settings.load(this);
        
        if (Settings.USE_ECONOMY)
            setupEconomy();
        
        loadBlueprints();
        
        Bukkit.getPluginManager().registerEvents(new BlueprintListener(this), this);
        getCommand("blueprints").setExecutor(new BlueprintsCommandHandler(this));
    }
    
    @Override
    public void onDisable() {
        saveBlueprints();
    }
    
    public static Economy getEconomy() {
        return econ;
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    private void loadBlueprints() {
        if (!blueprintFile.exists())
            saveResource(blueprintFile.getName(), false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(blueprintFile);
        blueprints = (List<Blueprint>) config.getList("blueprints", new ArrayList<>());
        blueprints.addAll((List<PlayerBlueprint>) config.getList("playerBlueprints", new ArrayList<>()));
    }
    
    private void saveBlueprints() {
        if (!blueprintFile.exists())
            saveResource(blueprintFile.getName(), false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(blueprintFile);
        
        config.getKeys(false).forEach(key -> config.set(key, null));
        config.set("blueprints", blueprints.stream().filter(blueprint -> blueprint != null && !(blueprint instanceof PlayerBlueprint)).collect(Collectors.toList()));
        config.set("playerBlueprints", blueprints.stream().filter(blueprint -> blueprint instanceof PlayerBlueprint).collect(Collectors.toList()));
        
        try {
            config.save(blueprintFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<Blueprint> getBlueprints() {
        return blueprints;
    }
    
    public List<PlayerBlueprint> getPlayerBlueprints() {
        return blueprints.stream().filter(blueprint -> blueprint instanceof PlayerBlueprint).map(blueprint -> (PlayerBlueprint) blueprint).collect(Collectors.toList());
    }
    
    public void addBlueprint(Blueprint blueprint) {
        blueprints.add(blueprint);
        
        if (blueprint instanceof PlayerBlueprint)
            getPlayerBlueprintMenu(((PlayerBlueprint) blueprint).getOwner()).update();
    }
    
    public void removeInventoryLink(Player player) {
        inventoryLinks.removeIf(link -> link.getOwner().equals(player));
    }
    
    public void addInventoryLink(InventoryLink link) {
        inventoryLinks.add(link);
    }
    
    public Optional<InventoryLink> getLink(Player player) {
        return inventoryLinks.stream().filter(link -> link.getOwner().equals(player)).findFirst();
    }
    
    public PlayerBlueprintMenu getPlayerBlueprintMenu(UUID uuid) {
        PlayerBlueprintMenu menu = playerBlueprintMenus.get(uuid);
        
        if (menu != null)
            return menu;
        
        menu = new PlayerBlueprintMenu(this, uuid);
        playerBlueprintMenus.put(uuid, menu);
        return menu;
    }
    
    
    public static BlueprintsPlugin getInstance() {
        return instance;
    }
}