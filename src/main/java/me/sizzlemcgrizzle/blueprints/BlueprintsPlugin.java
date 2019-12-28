package me.sizzlemcgrizzle.blueprints;

import me.sizzlemcgrizzle.blueprints.command.BlueprintsCommandGroup;
import me.sizzlemcgrizzle.blueprints.event.BlueprintBuilder;
import me.sizzlemcgrizzle.blueprints.event.ColorCache;
import me.sizzlemcgrizzle.blueprints.event.ReloadEvent;
import me.sizzlemcgrizzle.blueprints.settings.SchematicCache;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Bukkit;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.util.Arrays;
import java.util.List;

public class BlueprintsPlugin extends SimplePlugin {

	private ReloadEvent event = new ReloadEvent();
	private SchematicCache schematicCache;
	private ColorCache colorCache;

	@Override
	public void onPluginStart() {

		registerEvents(new BlueprintBuilder());

		registerCommands("blueprints", new BlueprintsCommandGroup());

		if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
			Common.log("Successfully hooked into World Edit!");

		if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention"))
			Common.log("Successfully hooked into Grief Prevention!");

		if (Bukkit.getPluginManager().isPluginEnabled("CLClans"))
			Common.log("Successfully hooked into CLClans!");

		this.schematicCache = new SchematicCache();
		this.colorCache = new ColorCache();

	}

	@Override
	protected void onPluginReload() {
		Bukkit.getPluginManager().callEvent(event);
	}

	public SchematicCache schematicCache() {
		return schematicCache;
	}

	public ColorCache colorCache() {
		return colorCache;
	}

	@Override
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Arrays.asList(Settings.class);
	}


}