package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import me.sizzlemcgrizzle.blueprints.util.SchematicCache;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.ArrayList;
import java.util.List;

public class BlueprintsAddCommand extends SimpleSubCommand {
    private BlueprintsPlugin blueprintsPlugin = (BlueprintsPlugin) Bukkit.getPluginManager().getPlugin("Blueprints");
    
    public BlueprintsAddCommand(final SimpleCommandGroup parent) {
        super(parent, "add");
        setMinArguments(1);
        setUsage("<schematicname.schematic> <type>");
        setPermission("blueprints.add");
    }
    
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1)
            return completeLastWord(SchematicCache.getSchematics());
        if (args.length == 2)
            return completeLastWord(Settings.TYPES);
        return new ArrayList<>();
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        final ItemStack blueprint = getPlayer().getInventory().getItemInMainHand();
        String type = "NORMAL";
        
        if (args.length > 1) {
            type = args[1];
            if (!Settings.TYPES.contains(type)) {
                tell(Settings.Messages.MESSAGE_PREFIX + "&6" + args[1] + "&e is not a valid blueprint type! Use &6/blueprints addtype &eto add a type.");
                return;
            }
            
        }
        
        Common.tell(getPlayer(), blueprintsPlugin.schematicCache().addBlueprint(args[0], blueprint, type));
        blueprintsPlugin.cacheBlueprints();
        
    }
}