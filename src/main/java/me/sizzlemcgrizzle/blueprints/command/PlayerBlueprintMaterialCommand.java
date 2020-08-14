package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.Optional;

public class PlayerBlueprintMaterialCommand extends SimpleSubCommand {
    
    protected PlayerBlueprintMaterialCommand(SimpleCommandGroup parent) {
        super(parent, "materials");
        setPermission("blueprints.create");
        setDescription("Lists all the materials the blueprint needs");
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        
        Player player = getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&eYou must hold a blueprint!");
            return;
        }
        
        Optional<PlayerBlueprint> optional = BlueprintsPlugin.instance.getPlayerBlueprints().stream().filter(blueprint -> blueprint.getItem().isSimilar(item)).findFirst();
        
        if (!optional.isPresent()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cThis item is not a player blueprint!");
            return;
        }
        
        optional.get().getMaterialGUI().display(player);
    }
}
