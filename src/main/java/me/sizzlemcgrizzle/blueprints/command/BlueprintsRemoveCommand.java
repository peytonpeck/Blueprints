package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.stream.Collectors;

public class BlueprintsRemoveCommand extends SimpleSubCommand {
    
    private static String FAILURE_MESSAGE = Settings.Messages.MESSAGE_PREFIX + "&cA blueprint for this item does not exist.";
    private static String SUCCESS_MESSAGE = Settings.Messages.MESSAGE_PREFIX + "&7Blueprint has been successfully removed.";
    
    protected BlueprintsRemoveCommand(SimpleCommandGroup parent) {
        super(parent, "remove");
        setPermission("blueprints.remove");
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        ItemStack item = getPlayer().getInventory().getItemInMainHand();
        
        if (item.getType().isAir())
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou are not holding a block!");
        else if (BlueprintsPlugin.instance.getBlueprints().stream().noneMatch(blueprint -> blueprint.getItem().isSimilar(item)))
            tell(FAILURE_MESSAGE);
        else {
            BlueprintsPlugin.instance.setBlueprints(BlueprintsPlugin.instance.getBlueprints().stream().filter(blueprint -> !blueprint.getItem().isSimilar(item)).collect(Collectors.toList()));
            tell(SUCCESS_MESSAGE);
        }
    }
    
    
}
