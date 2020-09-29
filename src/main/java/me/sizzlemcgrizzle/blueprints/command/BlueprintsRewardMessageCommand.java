package me.sizzlemcgrizzle.blueprints.command;

import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.BlueprintsReward;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlueprintsRewardMessageCommand extends SimpleSubCommand {
    
    public BlueprintsRewardMessageCommand(final SimpleCommandGroup parent) {
        super(parent, "message");
        setMinArguments(3);
        setUsage("<id> <message_type> <message>");
        setPermission("blueprintsrewards.admin");
    }
    
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1)
            return completeLastWord(BlueprintsPlugin.getInstance().getRewards().stream().map(BlueprintsReward::getId).collect(Collectors.toList()));
        if (args.length == 2)
            return completeLastWord(Arrays.asList("titleMessage", "subtitleMessage", "chatMessage"));
        if (args.length > 2)
            return Collections.singletonList("message");
        return Collections.emptyList();
    }
    
    @Override
    protected void onCommand() {
        checkConsole();
        
        Player player = getPlayer();
        
        Optional<BlueprintsReward> optional = BlueprintsPlugin.getInstance().getReward(args[0]);
        String type = args[1];
        String input = (String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
        
        if (!optional.isPresent()) {
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eThe reward id you entered is not valid!");
            return;
        }
        if (Arrays.asList("titleMessage", "subtitleMessage", "chatMessage").stream().noneMatch(s -> s.toLowerCase().equals(type.toLowerCase()))) {
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eThe message type you entered is not valid!");
            return;
        }
        
        switch (type.toLowerCase()) {
            case "titlemessage":
                optional.get().setTitleMessage(input);
                break;
            case "subtitlemessage":
                optional.get().setSubtitleMessage(input);
                break;
            case "chatmessage":
                optional.get().setChatMessage(input);
                break;
            default:
                Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eThe message type you entered is not valid!");
                return;
        }
        
        Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&aYou have set the " + type.toLowerCase() + ".");
    }
}