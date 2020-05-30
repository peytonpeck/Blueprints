package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.settings.Settings;
import me.sizzlemcgrizzle.blueprints.util.SchematicUtil;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.ArrayList;
import java.util.List;

public class BlueprintsGetCommand extends SimpleSubCommand {
    
    protected BlueprintsGetCommand(SimpleCommandGroup parent) {
        super(parent, "get");
        setMinArguments(1);
        setUsage("<schematicname.schematic>");
        setPermission("blueprints.get");
    }
    
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1)
            return completeLastWord(SchematicUtil.getSchematics());
        return new ArrayList<>();
    }
    
    @Override
    protected void onCommand() {
        List<ItemStack> itemList = SchematicUtil.getBlueprint(args[0]);
        if (itemList.size() == 0)
            tell(Settings.Messages.MESSAGE_PREFIX + "&cThere are no blueprints using this schematic. See the list of blueprints with &4/blueprints list&c.");
        else {
            if (itemList.size() > 1)
                tell(Settings.Messages.MESSAGE_PREFIX + "&eThere are multiple blueprints using this schematic so here are all of them.");
            for (ItemStack item : itemList) {
                getPlayer().getInventory().addItem(item);
                tell(Settings.Messages.MESSAGE_PREFIX + "&7You have been given " + item.getItemMeta().getDisplayName() + " &7blueprint.");
            }
        }
    }
}
