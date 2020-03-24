package me.sizzlemcgrizzle.blueprints;

import me.sizzlemcgrizzle.blueprints.command.BlueprintsCommandGroup;
import me.sizzlemcgrizzle.blueprints.event.Blueprint;
import me.sizzlemcgrizzle.blueprints.event.BlueprintListener;
import me.sizzlemcgrizzle.blueprints.event.ReloadEvent;
import me.sizzlemcgrizzle.blueprints.settings.Logs;
import me.sizzlemcgrizzle.blueprints.settings.SchematicCache;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BlueprintsPlugin extends SimplePlugin {

	private ReloadEvent event = new ReloadEvent();
	private SchematicCache schematicCache;
	private Logs logs;
	private HashMap<Player, Blueprint> map = new HashMap<>();
	private BossBar bossBar;

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

		this.schematicCache = new SchematicCache();
		this.logs = new Logs();
		this.bossBar = this.getServer().createBossBar(ChatColor.GREEN + "Confirm Placement Timer", BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);

	}

	@Override
	protected void onPluginReload() {
		Bukkit.getPluginManager().callEvent(event);
	}

	public SchematicCache schematicCache() {
		return schematicCache;
	}

	public Logs logs() {
		return logs;
	}

	public void addBlueprint(Player player, Blueprint blueprint) {
		map.put(player, blueprint);
	}

	public boolean isExistingBlueprint(Player player) {
		return map.containsKey(player);
	}

	public void removeBlueprint(Player player) {
		Blueprint blueprint = map.get(player);
		map.remove(player);
		blueprint = null;
	}

	public BossBar getBossBar() {
		return bossBar;
	}

	@Override
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Arrays.asList(Settings.class);
	}


}