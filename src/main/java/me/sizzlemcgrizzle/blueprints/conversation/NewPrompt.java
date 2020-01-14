package me.sizzlemcgrizzle.blueprints.conversation;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.model.SimpleComponent;

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
				.append("&6&l[-90°]")
				.onClickRunCmd("/convo right")
				.build();
	}

	@NotNull
	@Override
	public String getPromptText(ConversationContext context) {
		return ComponentSerializer.toString(promptText);
	}

	@Override
	protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
		String[] accepted = new String[]{"true", "false", "on", "off", "yes", "no", "y", "n", "1", "0", "right", "wrong", "correct", "incorrect", "valid", "invalid", "right", "left"};
		return ArrayUtils.contains(accepted, input.toLowerCase());
	}

	@Override
	protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {
		return this.acceptValidatedInput(conversationContext, s);
	}
}
