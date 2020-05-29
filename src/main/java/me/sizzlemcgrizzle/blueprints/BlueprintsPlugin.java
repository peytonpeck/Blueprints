package me.sizzlemcgrizzle.blueprints;

import me.sizzlemcgrizzle.blueprints.command.BlueprintsCommandGroup;
import me.sizzlemcgrizzle.blueprints.event.BlueprintListener;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import me.sizzlemcgrizzle.blueprints.util.SchematicUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BlueprintsPlugin extends SimplePlugin {
	
	private BossBar bossBar;
	private Set<String> blueprints;
	
	public static File blueprintsFile;
	public static BlueprintsPlugin instance;
	
	@Override
	public void onPluginStart() {
		
		instance = this;
		
		registerEvents(new BlueprintListener());
		
		registerCommands("blueprints", new BlueprintsCommandGroup());
		
		if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
			Common.log("Successfully hooked into World Edit!");
		
		if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention"))
			Common.log("Successfully hooked into Grief Prevention!");
		
		if (Bukkit.getPluginManager().isPluginEnabled("CLClans"))
			Common.log("Successfully hooked into CLClans!");
		
		setBlueprints();
		
		this.bossBar = this.getServer().createBossBar(ChatColor.GREEN + "Confirm Placement Timer", BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);
	}
	
	@Override
	protected void onPluginReload() {
		setBlueprints();
		
		blueprintsFile = new File(this.getDataFolder(), "blueprints.yml");
		instance = this;
	}
	
	public void setBlueprints() {
		blueprints = SchematicUtil.cacheBlueprints();
	}
	
	public Set<String> getBlueprints() {
		return this.blueprints;
	}
	
	public BossBar getBossBar() {
		return bossBar;
	}
	
	@Override
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Arrays.asList(Settings.class);
	}
	
	
}