package me.sizzlemcgrizzle.blueprints.command;

import org.mineacademy.fo.command.SimpleCommandGroup;

public class BlueprintsRewardCommandGroup extends SimpleCommandGroup {
    @Override
    protected void registerSubcommands() {
        registerSubcommand(new BlueprintsRewardMessageCommand(this));
        registerSubcommand(new BlueprintsRewardAddCommand(this));
        registerSubcommand(new BlueprintsRewardRemoveCommand(this));
    }
}
