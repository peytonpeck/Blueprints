package me.sizzlemcgrizzle.blueprints.command;

import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

public class PlayerBlueprintListCommand extends SimpleSubCommand {
    
    protected PlayerBlueprintListCommand(SimpleCommandGroup parent) {
        super(parent, "list");
        setPermission("blueprints.create");
    }
    
    @Override
    protected void onCommand() {
    
    }
}
