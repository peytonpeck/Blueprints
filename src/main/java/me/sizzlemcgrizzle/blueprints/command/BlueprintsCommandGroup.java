package me.sizzlemcgrizzle.blueprints.command;

import org.mineacademy.fo.command.SimpleCommandGroup;

public class BlueprintsCommandGroup extends SimpleCommandGroup {
	@Override
	protected void registerSubcommands() {
		registerSubcommand(new BlueprintsAddCommand(this));
		registerSubcommand(new BlueprintsRemoveCommand(this));
		registerSubcommand(new BlueprintsReloadCommand(this));
		registerSubcommand(new BlueprintsListCommand(this));
		registerSubcommand(new BlueprintsGetCommand(this));
		registerSubcommand(new BlueprintsSettimerCommand(this));
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
