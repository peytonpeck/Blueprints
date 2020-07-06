package me.sizzlemcgrizzle.blueprints.conversation;

import com.sun.istack.internal.NotNull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.mineacademy.fo.model.SimpleComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NewPrompt extends FixedSetPrompt {
    protected final BaseComponent promptText;
    
    public NewPrompt(String text) {
        this.promptText = SimpleComponent.of(text)
                .append("&a&l [Yes] ")
                .onClickRunCmd("/convo yes")
                .append("&c&l[No] ")
                .onClickRunCmd("/convo no")
                .append("&6&l[90°] ")
                .onClickRunCmd("/convo left")
                .append("&6&l[-90°] ")
                .onClickRunCmd("/convo right")
                .append("\n&eMove blueprint? ")
                .append("&9&l[+X] ")
                .onClickRunCmd("/convo +x")
                .append("&9&l[-X] ")
                .onClickRunCmd("/convo -x")
                .append("&9&l[+Z] ")
                .onClickRunCmd("/convo +z")
                .append("&9&l[-Z] ")
                .onClickRunCmd("/convo -z")
                .append("&b&l[Up] ")
                .onClickRunCmd("/convo +y")
                .append("&b&l[Down] ")
                .onClickRunCmd("/convo -y")
                .build();
    }
    
    @NotNull
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
    Prompt acceptValidatedInput(@Nonnull ConversationContext conversationContext, @Nonnull String s) {
        return this.acceptValidatedInput(conversationContext, s);
    }
}
