package me.sizzlemcgrizzle.blueprints.command;

import org.mineacademy.fo.command.SimpleCommandGroup;

public class PlayerBlueprintCommandGroup extends SimpleCommandGroup {
    @Override
    protected void registerSubcommands() {
        registerSubcommand(new PlayerBlueprintCompleteCommand(this));
        registerSubcommand(new PlayerBlueprintCancelCommand(this));
        registerSubcommand(new PlayerBlueprintCreateCommand(this));
        registerSubcommand(new PlayerBlueprintListCommand(this));
        registerSubcommand(new PlayerBlueprintLinkCommand(this));
    }
    
    @Override
    protected String getCredits() {
        return "";
    }
    
    @Override
    protected String getHeaderPrefix() {
        return "&3&l";
    }
}
