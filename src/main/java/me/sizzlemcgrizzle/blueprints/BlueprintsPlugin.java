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
import me.sizzlemcgrizzle.blueprints.event.BlueprintListener;
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
import org.mineacademy.fo.Common;
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
import java.util.stream.Collectors;

public class BlueprintsPlugin extends SimplePlugin {
    
    public static File BLUEPRINTS_FILE;
    public static BlueprintsPlugin instance;
    private static Economy econ = null;
    
    private BossBar bossBar;
    
    private List<Blueprint> blueprints;
    private List<InventoryLink> inventoryLinks;
    
    private Map<Player, BlueprintCreationSession> creationSessions = new HashMap<>();
    
    @Override
    public void onPluginStart() {
        instance = this;
        BLUEPRINTS_FILE = new File(getDataFolder(), "blueprints.yml");
        inventoryLinks = new ArrayList<>();
        
        ConfigurationSerialization.registerClass(Blueprint.class);
        ConfigurationSerialization.registerClass(PlayerBlueprint.class);
        
        if (Settings.USE_ECONOMY)
            setupEconomy();
        
        loadBlueprints();
        
        registerEvents(new BlueprintListener());
        
        registerCommands("blueprints", new BlueprintsCommandGroup());
        registerCommands("blueprint", Collections.singletonList("playerblueprint"), new PlayerBlueprintCommandGroup());
        
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
            Common.log("Successfully hooked into World Edit!");
        
        if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention"))
            Common.log("Successfully hooked into Grief Prevention!");
        
        if (Bukkit.getPluginManager().isPluginEnabled("CLClans"))
            Common.log("Successfully hooked into CLClans!");
        this.bossBar = this.getServer().createBossBar(ChatColor.GREEN + "Confirm Placement Timer", BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);
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
        if (!BLUEPRINTS_FILE.exists())
            FileUtil.extract(BLUEPRINTS_FILE.getName());
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(BLUEPRINTS_FILE);
        
        blueprints = config.contains("blueprints") ? (List<Blueprint>) config.getList("blueprints") : Collections.emptyList();
        blueprints.addAll(config.contains("playerBlueprints") ? (List<Blueprint>) config.getList("playerBlueprints") : Collections.emptyList());
    }
    
    private void saveBlueprints() {
        if (!BLUEPRINTS_FILE.exists())
            FileUtil.extract(BLUEPRINTS_FILE.getName());
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(BLUEPRINTS_FILE);
        
        config.set("blueprints", blueprints.stream().filter(blueprint -> !(blueprint instanceof PlayerBlueprint)).collect(Collectors.toList()));
        config.set("playerBlueprints", blueprints.stream().filter(blueprint -> blueprint instanceof PlayerBlueprint).collect(Collectors.toList()));
        
        try {
            config.save(BLUEPRINTS_FILE);
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
    
    public List<PlayerBlueprint> getPlayerBlueprints() {
        return blueprints.stream().filter(blueprint -> blueprint instanceof PlayerBlueprint).map(blueprint -> (PlayerBlueprint) blueprint).collect(Collectors.toList());
    }
    
    public void addBlueprint(Blueprint blueprint) {
        blueprints.add(blueprint);
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
    
    @Override
    public List<Class<? extends YamlStaticConfig>> getSettings() {
        return Arrays.asList(Settings.class);
    }
    
    
}