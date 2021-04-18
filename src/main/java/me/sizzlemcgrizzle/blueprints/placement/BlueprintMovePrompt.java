package me.sizzlemcgrizzle.blueprints.placement;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlueprintMovePrompt extends FixedSetPrompt {
    
    private String[] yes = new String[]{"yes", "1", "true", "y", "correct", "valid"};
    private String[] no = new String[]{"no", "0", "false", "n", "wrong", "invalid"};
    
    private final BaseComponent[] promptText;
    private final BlueprintPlacementSession blueprintPlacementSession;
    
    public BlueprintMovePrompt(String text, BlueprintPlacementSession blueprintPlacementSession, boolean withRotation, boolean withTranslation) {
        this.blueprintPlacementSession = blueprintPlacementSession;
        ComponentBuilder builder = new ComponentBuilder(text)
                .append("§a§l [Yes] ")
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo yes"))
                .append("§c§l[No] ")
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo no"));
        if (withRotation)
            builder.append("§6§l[90°] ")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo left"))
                    .append("§6§l[-90°] ")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo right"));
        if (withTranslation)
            builder.append("\n§eMove blueprint? ")
                    .append("§9§l[+X] ")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo +x"))
                    .append("§9§l[-X] ")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo -x"))
                    .append("§9§l[+Z] ")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo +z"))
                    .append("§9§l[-Z] ")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo -z"))
                    .append("§b§l[Up] ")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo +y"))
                    .append("§b§l[Down] ")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo -y"));
        
        promptText = builder.create();
    }
    
    @Nonnull
    @Override
    public String getPromptText(ConversationContext context) {
        return ComponentSerializer.toString(promptText);
    }
    
    @Override
    protected boolean isInputValid(@Nonnull ConversationContext context, @Nonnull String input) {
        String[] accepted = new String[]{"true", "false", "on", "off", "yes", "no", "y", "n", "1", "0",
                "right", "wrong", "correct", "incorrect", "valid", "invalid", "right", "left",
                "+x", "-x", "+y", "-y", "+z", "-z"};
        return ArrayUtils.contains(accepted, input.toLowerCase());
    }
    
    @Override
    protected @Nullable
    Prompt acceptValidatedInput(@Nonnull ConversationContext conversationContext, @Nonnull String input) {
        if (ArrayUtils.contains(yes, input.toLowerCase())) {
            blueprintPlacementSession.complete();
            return Prompt.END_OF_CONVERSATION;
            
            
        } else if (ArrayUtils.contains(no, input.toLowerCase())) {
            blueprintPlacementSession.cancel();
            return Prompt.END_OF_CONVERSATION;
            
            
        } else if (input.equalsIgnoreCase("right")) {
            blueprintPlacementSession.transform(1);
            return this;
        } else if (input.equalsIgnoreCase("left")) {
            blueprintPlacementSession.transform(-1);
            return this;
        } else if (input.equalsIgnoreCase("+x")) {
            if (blueprintPlacementSession.setOrigin(1, 0, 0))
                return Prompt.END_OF_CONVERSATION;
            return this;
        } else if (input.equalsIgnoreCase("-x")) {
            if (blueprintPlacementSession.setOrigin(-1, 0, 0))
                return Prompt.END_OF_CONVERSATION;
            return this;
        } else if (input.equalsIgnoreCase("+y")) {
            if (blueprintPlacementSession.setOrigin(0, 1, 0))
                return Prompt.END_OF_CONVERSATION;
            return this;
        } else if (input.equalsIgnoreCase("-y")) {
            if (blueprintPlacementSession.setOrigin(0, -1, 0))
                return Prompt.END_OF_CONVERSATION;
            return this;
        } else if (input.equalsIgnoreCase("+z")) {
            if (blueprintPlacementSession.setOrigin(0, 0, 1))
                return Prompt.END_OF_CONVERSATION;
            return this;
        } else if (input.equalsIgnoreCase("-z")) {
            if (blueprintPlacementSession.setOrigin(0, 0, -1))
                return Prompt.END_OF_CONVERSATION;
            return this;
        }
        return Prompt.END_OF_CONVERSATION;
    }
}
