package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.gui.PlayerBlueprintMenu;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

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
        
        PlayerBlueprintMenu gui = BlueprintsPlugin.getInstance().getPlayerBlueprintMenu(player.getUniqueId());
        
        gui.display(player);
    }
}
