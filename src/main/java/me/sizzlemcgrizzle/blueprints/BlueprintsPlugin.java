package me.sizzlemcgrizzle.blueprints;

import me.sizzlemcgrizzle.blueprints.command.BlueprintsCommandGroup;
import me.sizzlemcgrizzle.blueprints.event.BlueprintListener;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import me.sizzlemcgrizzle.blueprints.util.SchematicCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BlueprintsPlugin extends SimplePlugin {
    
    private SchematicCache schematicCache;
    private BossBar bossBar;
    private Set<String> blueprints;
    
    @Override
    public void onPluginStart() {
        
        registerEvents(new BlueprintListener());
        
        registerCommands("blueprints", new BlueprintsCommandGroup());
        
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
            Common.log("Successfully hooked into World Edit!");
        
        if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention"))
            Common.log("Successfully hooked into Grief Prevention!");
        
        if (Bukkit.getPluginManager().isPluginEnabled("CLClans"))
            Common.log("Successfully hooked into CLClans!");
        
        cacheBlueprints();
        
        this.schematicCache = new SchematicCache();
        this.bossBar = this.getServer().createBossBar(ChatColor.GREEN + "Confirm Placement Timer", BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);
        
    }
    
    @Override
    protected void onPluginReload() {
        cacheBlueprints();
    }
    
    public SchematicCache schematicCache() {
        return schematicCache;
    }
    
    public Set<String> getBlueprints() {
        return this.blueprints;
    }
    
    public void cacheBlueprints() {
        File file = new File(this.getDataFolder(), "blueprints.yml");
        YamlConfiguration config = new YamlConfiguration();
        
        try {
            if (!file.exists())
                file.createNewFile();
            
            config.load(file);
            
            Common.log("Caching blueprints for optimization...");
            blueprints = config.getKeys(false);
            
            config.save(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    public BossBar getBossBar() {
        return bossBar;
    }
    
    @Override
    public List<Class<? extends YamlStaticConfig>> getSettings() {
        return Arrays.asList(Settings.class);
    }
    
    
}