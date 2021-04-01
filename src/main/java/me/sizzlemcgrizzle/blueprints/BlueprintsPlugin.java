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
import me.sizzlemcgrizzle.blueprints.command.BlueprintsRewardCommandGroup;
import me.sizzlemcgrizzle.blueprints.command.PlayerBlueprintCommandGroup;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintMenu;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintRemoveGUI;
import me.sizzlemcgrizzle.blueprints.placement.Blueprint;
import me.sizzlemcgrizzle.blueprints.placement.BlueprintListener;
import me.sizzlemcgrizzle.blueprints.placement.BlueprintsReward;
import me.sizzlemcgrizzle.blueprints.placement.InventoryLink;
import me.sizzlemcgrizzle.blueprints.placement.MaterialContainer;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import net.milkbowl.vault.economy.Economy;
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
import java.util.List;
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
    private List<PlayerBlueprintMenu> playerBlueprintMenus = new ArrayList<>();
    private List<BlueprintsReward> rewards;
    
    @Override
    public void onPluginStart() {
        ConfigurationSerialization.registerClass(Blueprint.class);
        ConfigurationSerialization.registerClass(PlayerBlueprint.class);
        ConfigurationSerialization.registerClass(MaterialContainer.class);
        ConfigurationSerialization.registerClass(BlueprintsReward.class);
        
        instance = this;
        
        if (Settings.USE_ECONOMY)
            setupEconomy();
        
        loadBlueprints();
        
        registerEvents(new BlueprintListener(this));
        registerCommands("adminblueprints", Arrays.asList("ablueprints", "blueprintsadmin"), new BlueprintsCommandGroup());
        registerCommands("blueprint", Collections.singletonList("playerblueprint"), new PlayerBlueprintCommandGroup());
        registerCommands("blueprintsreward", new BlueprintsRewardCommandGroup());
        
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
        rewards = (List<BlueprintsReward>) config.getList("rewards", new ArrayList<>());
    }
    
    private void saveBlueprints() {
        if (!blueprintFile.exists())
            FileUtil.extract(blueprintFile.getName());
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(blueprintFile);
        
        config.getKeys(false).forEach(key -> config.set(key, null));
        config.set("blueprints", blueprints.stream().filter(blueprint -> blueprint != null && !(blueprint instanceof PlayerBlueprint)).collect(Collectors.toList()));
        config.set("playerBlueprints", blueprints.stream().filter(blueprint -> blueprint instanceof PlayerBlueprint).collect(Collectors.toList()));
        config.set("rewards", rewards);
        
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
        
        if (blueprint instanceof PlayerBlueprint)
            reloadBlueprintMenu(((PlayerBlueprint) blueprint).getOwner());
    }
    
    public void removeBlueprint(Blueprint blueprint) {
        blueprints.remove(blueprint);
        
        if (blueprint instanceof PlayerBlueprint)
            reloadBlueprintMenu(((PlayerBlueprint) blueprint).getOwner());
    }
    
    public void reloadBlueprintMenu(UUID owner) {
        PlayerBlueprintMenu menu = getPlayerBlueprintMenu(owner);
        menu.setPageItems(PlayerBlueprint.getPageItems(owner));
        menu.reload();
    }
    
    public void setBlueprints(List<Blueprint> list) {
        blueprints = list;
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
        Optional<PlayerBlueprintMenu> optional = playerBlueprintMenus.stream().filter(gui -> gui.getOwner().equals(uuid)).findFirst();
        
        if (optional.isPresent())
            return optional.get();
        
        PlayerBlueprintMenu menu = new PlayerBlueprintMenu(uuid);
        playerBlueprintMenus.add(menu);
        return menu;
    }
    
    public List<BlueprintsReward> getRewards() {
        return rewards;
    }
    
    public void addReward(BlueprintsReward reward) {
        rewards.add(reward);
    }
    
    public void removeReward(BlueprintsReward reward) {
        rewards.remove(reward);
    }
    
    public Optional<BlueprintsReward> getReward(String id) {
        return rewards.stream().filter(r -> r.getId().equals(id)).findFirst();
    }
    
    @Override
    public List<Class<? extends YamlStaticConfig>> getSettings() {
        return Arrays.asList(Settings.class);
    }
    
    public static BlueprintsPlugin getInstance() {
        return instance;
    }
}