package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.Blueprint;
import me.sizzlemcgrizzle.blueprints.placement.BlueprintsReward;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BlueprintsRewardAddCommand extends SimpleSubCommand {
    
    public BlueprintsRewardAddCommand(final SimpleCommandGroup parent) {
        super(parent, "add");
        setMinArguments(3);
        setUsage("<id> <base_schematic> <reward_schematic>");
        setPermission("blueprintsrewards.admin");
    }
    
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1)
            return Collections.singletonList("id");
        if (args.length == 2)
            return completeLastWord(BlueprintsPlugin.getInstance().getBlueprints().stream().filter(blueprint -> !(blueprint instanceof PlayerBlueprint)).map(Blueprint::getSchematic).collect(Collectors.toList()));
        if (args.length == 3)
            return completeLastWord(BlueprintsPlugin.getInstance().getBlueprints().stream().filter(blueprint -> !(blueprint instanceof PlayerBlueprint)).map(Blueprint::getSchematic).collect(Collectors.toList()));
        return Collections.emptyList();
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        
        Player player = getPlayer();
        String id = args[0];
        String baseSchematic = args[1];
        String rewardSchematic = args[2];
        
        
        Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&aReward added!");
        BlueprintsPlugin.getInstance().addReward(new BlueprintsReward(baseSchematic, rewardSchematic, id));
    }
}