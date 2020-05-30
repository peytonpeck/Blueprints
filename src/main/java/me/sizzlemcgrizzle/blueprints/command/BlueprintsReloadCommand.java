package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.SimpleLocalization;

public class BlueprintsReloadCommand extends SimpleSubCommand {
    protected BlueprintsReloadCommand(SimpleCommandGroup parent) {
        super(parent, "reload");
        setPermission("blueprints.reload");
    }
    
    @Override
    protected void onCommand() {
        try {
            SimplePlugin.getInstance().reload();
            tell(Settings.Messages.MESSAGE_PREFIX + Settings.Messages.RELOAD_SUCCESS);
        } catch (Throwable t) {
            t.printStackTrace();
            
            tell(SimpleLocalization.Commands.RELOAD_FAIL.replace("{error}", t.toString()));
            
        }
    }
}
