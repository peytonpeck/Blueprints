package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BlueprintsConfigCommand extends SimpleSubCommand {
    private File file = new File(BlueprintsPlugin.getData().getAbsolutePath() + File.separator + "/settings.yml");
    private YamlConfiguration config = new YamlConfiguration();
    
    private List<String> commmands = Arrays.asList("setPreviewDuration", "setPrefix", "setErrorBlock", "setErrorBlockPreviewTime", "playSounds");
    
    public BlueprintsConfigCommand(final SimpleCommandGroup parent) {
        super(parent, "config");
        setMinArguments(1);
        setPermission("blueprints.config");
    }
    
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1)
            return completeLastWord(commmands);
        if (args.length == 2)
            if (args[0].equalsIgnoreCase("playsounds"))
                return completeLastWord(Arrays.asList("true", "false"));
        return super.tabComplete();
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        if (!commmands.contains(args[0])) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must put a valid subcommand in! (Use tab-complete to find your way around)");
            return;
        }
        if (args.length == 1 && !args[0].equalsIgnoreCase("setErrorBlock")) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must provide an argument after '" + args[0] + "'.");
            return;
        }
        switch (args[0]) {
            case "setPreviewDuration":
                setPreviewDuration();
                break;
            case "setPrefix":
                setPrefix();
                break;
            case "setErrorBlock":
                setErrorBlock();
                break;
            case "setErrorBlockPreviewTime":
                setErrorBlockPreviewTime();
                break;
            case "playSounds":
                playSounds();
                break;
            default:
                tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must put a subcommand in! (Use tab-complete to find your way around)");
        }
        
    }
    
    private void playSounds() {
        boolean bool;
        if (args[1].equalsIgnoreCase("true"))
            bool = true;
        else if (args[1].equalsIgnoreCase("false"))
            bool = false;
        else {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must input 'true' or 'false'!");
            return;
        }
        try {
            config.load(file);
            
            config.set("Play_Sounds", bool);
            config.save(file);
            
            if (bool)
                tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully enabled sounds.");
            else
                tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully disabled sounds.");
            tell(Settings.Messages.MESSAGE_PREFIX + "&7Use &6/blueprints reload&7 to save changes.");
            
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    private void setErrorBlockPreviewTime() {
        int i;
        try {
            i = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must input a number!");
            return;
        }
        try {
            config.load(file);
            
            config.getConfigurationSection("Block").set("Block_Timeout", i);
            config.save(file);
            
            tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully changed preview time to " + args[1] + " seconds.");
            
            tell(Settings.Messages.MESSAGE_PREFIX + "&7Use &6/blueprints reload&7 to save changes.");
            
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    private void setErrorBlock() {
        if (!getPlayer().getInventory().getItemInMainHand().getType().isBlock()) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must hold an actual block!");
            return;
        }
        try {
            config.load(file);
            
            config.getConfigurationSection("Block").set("Error_Block", getPlayer().getInventory().getItemInMainHand().getType().toString());
            
            config.save(file);
            
            tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully changed error block to " + getPlayer().getInventory().getItemInMainHand().getType() + "&a.");
            
            tell(Settings.Messages.MESSAGE_PREFIX + "&7Use &6/blueprints reload&7 to save changes.");
            
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    private void setPrefix() {
        try {
            config.load(file);
            
            config.getConfigurationSection("Messages").set("Message_Prefix", args[1] + " ");
            
            config.save(file);
            
            tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully changed prefix to " + args[1] + "&a.");
            
            tell(Settings.Messages.MESSAGE_PREFIX + "&7Use &6/blueprints reload&7 to save changes.");
            
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    private void setPreviewDuration() {
        int i;
        try {
            i = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            tell(Settings.Messages.MESSAGE_PREFIX + "&cYou must input a number!");
            return;
        }
        try {
            config.load(file);
            
            config.getConfigurationSection("Block").set("Bossbar_Duration", i);
            config.save(file);
            
            tell(Settings.Messages.MESSAGE_PREFIX + "&aSuccessfully changed timer to " + args[1] + " seconds.");
            tell(Settings.Messages.MESSAGE_PREFIX + "&7Use &6/blueprints reload&7 to save changes.");
            
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
