package me.sizzlemcgrizzle.blueprints.conversation;

import me.sizzlemcgrizzle.blueprints.BlueprintPlacementSession;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
 * Made by SydMontague:
 * https://github.com/SydMontague/CLCore/tree/craftcitizen/src/main/java/de/craftlancer/core/conversation
 */
public class ConfirmationPrompt extends NewPrompt {
    
    private BlueprintPlacementSession blueprintPlacementSession;
    private String[] yes = new String[]{"yes", "1", "true", "y", "correct", "valid"};
    private String[] no = new String[]{"no", "0", "false", "n", "wrong", "invalid"};
    
    public ConfirmationPrompt(BlueprintPlacementSession blueprintPlacementSession) {
        super(ChatColor.YELLOW + "Place blueprint?");
        
        this.blueprintPlacementSession = blueprintPlacementSession;
        
    }
    
    @Override
    protected @Nullable
    Prompt acceptValidatedInput(@Nonnull ConversationContext conversationContext, @Nonnull String input) {
        if (ArrayUtils.contains(yes, input.toLowerCase())) {
            blueprintPlacementSession.complete();
            blueprintPlacementSession = null;
            return Prompt.END_OF_CONVERSATION;
            
            
        } else if (ArrayUtils.contains(no, input.toLowerCase())) {
            blueprintPlacementSession.cancel();
            blueprintPlacementSession = null;
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

