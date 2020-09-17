package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintMenu;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.Optional;

public class PlayerBlueprintListCommand extends SimpleSubCommand {
    
    protected PlayerBlueprintListCommand(SimpleCommandGroup parent) {
        super(parent, "list");
        setPermission("blueprints.create");
        setDescription("Lists all the blueprints you own");
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        
        Player player = getPlayer();
        
        Optional<PlayerBlueprintMenu> optional = BlueprintsPlugin.getInstance().getPlayerBlueprintListGUIFor(player.getUniqueId());
        
        if (optional.isPresent()) {
            optional.get().display(player);
            return;
        }
        
        PlayerBlueprintMenu gui = new PlayerBlueprintMenu(BlueprintsPlugin.getInstance(),
                ChatColor.DARK_PURPLE + player.getName() + "'s Player Blueprints",
                true,
                5,
                PlayerBlueprint.getPageItems(player.getUniqueId()),
                true,
                player.getUniqueId());
        
        BlueprintsPlugin.getInstance().addPlayerBlueprintListGUI(gui);
        
        gui.display(player);
    }
}
