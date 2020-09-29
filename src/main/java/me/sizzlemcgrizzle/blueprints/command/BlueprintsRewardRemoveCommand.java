package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.BlueprintsReward;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlueprintsRewardRemoveCommand extends SimpleSubCommand {
    
    public BlueprintsRewardRemoveCommand(final SimpleCommandGroup parent) {
        super(parent, "remove");
        setMinArguments(1);
        setUsage("<id>");
        setPermission("blueprintsrewards.admin");
    }
    
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1)
            return completeLastWord(BlueprintsPlugin.getInstance().getRewards().stream().map(BlueprintsReward::getId).collect(Collectors.toList()));
        return Collections.emptyList();
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        
        Player player = getPlayer();
        
        Optional<BlueprintsReward> optional = BlueprintsPlugin.getInstance().getReward(args[0]);
        
        if (!optional.isPresent()) {
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eThe reward id you entered is not valid!");
            return;
        }
        
        BlueprintsPlugin.getInstance().removeReward(optional.get());
        Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&aReward removed.");
    }
}