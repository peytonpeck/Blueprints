package me.sizzlemcgrizzle.blueprints;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.sizzlemcgrizzle.blueprints.command.BlueprintsCommandGroup;
import me.sizzlemcgrizzle.blueprints.command.PlayerBlueprintCommandGroup;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintMenu;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintRemoveGUI;
import me.sizzlemcgrizzle.blueprints.placement.Blueprint;
import me.sizzlemcgrizzle.blueprints.placement.BlueprintCreationSession;
import me.sizzlemcgrizzle.blueprints.placement.BlueprintListener;
import me.sizzlemcgrizzle.blueprints.placement.InventoryLink;
import me.sizzlemcgrizzle.blueprints.placement.MaterialContainer;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlueprintsPlugin extends SimplePlugin {
    
    private final File blueprintFile = new File(getDataFolder(), "blueprints.yml");
    
    private static BlueprintsPlugin instance;
    private static Economy econ = null;
    
    private PlayerBlueprintRemoveGUI playerBlueprintRemoveGUI;
    private BossBar bossBar;
    
    private List<Blueprint> blueprints;
    private List<InventoryLink> inventoryLinks = new ArrayList<>();
    private List<PlayerBlueprintMenu> playerBlueprintListGUIs = new ArrayList<>();
    
    private Map<Player, BlueprintCreationSession> creationSessions = new HashMap<>();
    
    @Override
    public void onPluginStart() {
        ConfigurationSerialization.registerClass(Blueprint.class);
        ConfigurationSerialization.registerClass(PlayerBlueprint.class);
        ConfigurationSerialization.registerClass(MaterialContainer.class);
        
        instance = this;
        
        if (Settings.USE_ECONOMY)
            setupEconomy();
        
        loadBlueprints();
        
        registerEvents(new BlueprintListener());
        registerCommands("blueprints", new BlueprintsCommandGroup());
        registerCommands("blueprint", Collections.singletonList("playerblueprint"), new PlayerBlueprintCommandGroup());
        
        bossBar = getServer().createBossBar(ChatColor.GREEN + "Confirm Placement Timer", BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);
        playerBlueprintRemoveGUI = new PlayerBlueprintRemoveGUI();
    }
    
    @Override
    protected void onPluginStop() {
        saveBlueprints();
    }
    
    @Override
    protected void onPluginReload() {
        instance = this;
    }
    
    public static Economy getEconomy() {
        return econ;
    }
    
    public static boolean isInRegion(Player player, Location loc) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(loc.getWorld()));
        
        if (player.isOp())
            return false;
        
        if (WorldGuard.getInstance() != null && regions != null && !player.isOp()) {
            BlockVector3 position = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
            ApplicableRegionSet set = regions.getApplicableRegions(position);
            return set.size() != 0;
        }
        return false;
    }
    
    public static boolean isTrusted(Player player, Location loc) {
        if (!GriefPrevention.instance.isEnabled())
            return true;
        
        Claim claim = null;
        for (Claim c : GriefPrevention.instance.dataStore.getClaims())
            if (c.contains(loc, true, false))
                claim = c;
        
        return claim == null
                || claim.getOwnerName().equals(player.getName())
                || claim.allowBuild(player, loc.getBlock().getType()) == null;
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
            FileUtil.extract(blueprintFile.getName());
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(blueprintFile);
        blueprints = (List<Blueprint>) config.getList("blueprints", new ArrayList<>());
        blueprints.addAll((List<PlayerBlueprint>) config.getList("playerBlueprints", new ArrayList<>()));
        config.getKeys(false).stream().filter(key -> !key.equalsIgnoreCase("blueprints") && !key.equalsIgnoreCase("playerBlueprints"))
                .forEach(key -> addBlueprint(new Blueprint(
                        config.getConfigurationSection(key).getItemStack("Blueprint"),
                        config.getConfigurationSection(key).getString("Schematic"),
                        config.getConfigurationSection(key).getString("Type", "NORMAL"))));
    }
    
    private void saveBlueprints() {
        if (!blueprintFile.exists())
            FileUtil.extract(blueprintFile.getName());
        
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
    
    public BossBar getBossBar() {
        return bossBar;
    }
    
    public List<Blueprint> getBlueprints() {
        return blueprints;
    }
    
    public PlayerBlueprintRemoveGUI getPlayerBlueprintRemoveGUI() {
        return playerBlueprintRemoveGUI;
    }
    
    public List<PlayerBlueprint> getPlayerBlueprints() {
        return blueprints.stream().filter(blueprint -> blueprint instanceof PlayerBlueprint).map(blueprint -> (PlayerBlueprint) blueprint).collect(Collectors.toList());
    }
    
    public void addBlueprint(Blueprint blueprint) {
        blueprints.add(blueprint);
        if (!(blueprint instanceof PlayerBlueprint))
            return;
        
        //Dealing with player blueprint guis and updating them
        UUID owner = ((PlayerBlueprint) blueprint).getOwner();
        
        Optional<PlayerBlueprintMenu> optional = getPlayerBlueprintListGUIFor(owner);
        if (optional.isPresent()) {
            optional.get().setPageItems(PlayerBlueprint.getPageItems(((PlayerBlueprint) blueprint).getOwner()));
            optional.get().reload();
        } else {
            PlayerBlueprintMenu gui = new PlayerBlueprintMenu(BlueprintsPlugin.getInstance(),
                    ChatColor.DARK_PURPLE + Bukkit.getOfflinePlayer(owner).getName() + "'s Player Blueprints",
                    true,
                    6,
                    PlayerBlueprint.getPageItems(owner),
                    true,
                    owner);
            
            BlueprintsPlugin.getInstance().addPlayerBlueprintListGUI(gui);
        }
    }
    
    public void removeBlueprint(Blueprint blueprint) {
        blueprints.remove(blueprint);
    }
    
    public void setBlueprints(List<Blueprint> list) {
        blueprints = list;
    }
    
    public Map<Player, BlueprintCreationSession> getCreationSessions() {
        return creationSessions;
    }
    
    public void addCreationSession(Player player, BlueprintCreationSession creationSession) {
        creationSessions.put(player, creationSession);
    }
    
    //Remove the creation session and cancel the particle runnable, if applicable
    public void removeCreationSession(Player player) {
        if (!creationSessions.containsKey(player))
            return;
        
        creationSessions.get(player).remove();
        
        creationSessions.remove(player);
    }
    
    public BlueprintCreationSession getCreationSession(Player player) {
        return creationSessions.get(player);
    }
    
    public List<InventoryLink> getInventoryLinks() {
        return inventoryLinks;
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
    
    public Optional<PlayerBlueprintMenu> getPlayerBlueprintListGUIFor(UUID player) {
        return playerBlueprintListGUIs.stream().filter(gui -> gui.getOwner().equals(player)).findFirst();
    }
    
    public void addPlayerBlueprintListGUI(PlayerBlueprintMenu gui) {
        playerBlueprintListGUIs.add(gui);
    }
    
    @Override
    public List<Class<? extends YamlStaticConfig>> getSettings() {
        return Arrays.asList(Settings.class);
    }
    
    public static BlueprintsPlugin getInstance() {
        return instance;
    }
}