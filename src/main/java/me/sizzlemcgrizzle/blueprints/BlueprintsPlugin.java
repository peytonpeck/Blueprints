package me.sizzlemcgrizzle.blueprints;

import de.craftlancer.clapi.blueprints.AbstractBlueprint;
import de.craftlancer.clapi.blueprints.AbstractBlueprintsPlugin;
import de.craftlancer.clapi.clclans.AbstractCLClans;
import me.sizzlemcgrizzle.blueprints.command.BlueprintsCommandHandler;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BlueprintsPlugin extends JavaPlugin implements AbstractBlueprintsPlugin {
    
    private final File blueprintFile = new File(getDataFolder(), "blueprints.yml");
    
    private static BlueprintsPlugin instance;
    private static Economy econ = null;
    
    private List<AbstractBlueprint> blueprints;
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
        Bukkit.getServicesManager().register(AbstractBlueprintsPlugin.class,this,this, ServicePriority.Highest);
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
        blueprints = (List<AbstractBlueprint>) config.getList("blueprints", new ArrayList<>());
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
    
    public List<AbstractBlueprint> getBlueprints() {
        return blueprints;
    }

    @Override
    public Optional<AbstractBlueprint> getBlueprint(String schematic) {
        return blueprints.stream().filter(b -> !(b instanceof PlayerBlueprint))
                .filter(b -> b.getSchematic().equals(schematic))
                .findFirst();
    }

    @Override
    public Optional<AbstractBlueprint> getBlueprint(ItemStack itemStack) {
        return blueprints.stream().filter(b -> !(b instanceof PlayerBlueprint))
                .filter(b -> ((Blueprint) b).compareItem(itemStack))
                .findFirst();
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

    public static List<String> getSchematics() {
        File newFile = new File(Bukkit.getPluginManager().getPlugin("WorldEdit").getDataFolder(), "schematics");

        return !newFile.exists() || newFile.listFiles() == null
                ? new ArrayList<>()
                : Arrays.stream(newFile.listFiles()).map(File::getName).collect(Collectors.toList());
    }

    public AbstractCLClans getClans() {
        return Bukkit.getServicesManager().load(AbstractCLClans.class);
    }

    public static BlueprintsPlugin getInstance() {
        return instance;
    }
}