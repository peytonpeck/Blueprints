package me.sizzlemcgrizzle.blueprints.conversation;

import me.sizzlemcgrizzle.blueprints.event.Blueprint;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/*
 * Made by SydMontague:
 * https://github.com/SydMontague/CLCore/tree/craftcitizen/src/main/java/de/craftlancer/core/conversation
 */
public class ConfirmationPrompt extends NewPrompt {
	
	private Blueprint blueprint;
	private String[] yes = new String[]{"yes", "1", "true", "y", "correct", "valid"};
	private String[] no = new String[]{"no", "0", "false", "n", "wrong", "invalid"};
	
	public ConfirmationPrompt(Blueprint blueprint) {
		super(ChatColor.YELLOW + "Place blueprint?");
		
		this.blueprint = blueprint;
		
	}
	
	@Override
	protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
		if (ArrayUtils.contains(yes, input.toLowerCase())) {
			try {
				blueprint.complete();
			} catch (IOException e) {
				e.printStackTrace();
			}
			blueprint = null;
			return Prompt.END_OF_CONVERSATION;
			
			
		} else if (ArrayUtils.contains(no, input.toLowerCase())) {
			blueprint.cancel();
			blueprint = null;
			return Prompt.END_OF_CONVERSATION;
			
			
		} else if (input.equalsIgnoreCase("right")) {
			blueprint.transform(1);
			return this;
		} else if (input.equalsIgnoreCase("left")) {
			blueprint.transform(-1);
			return this;
		} else if (input.equalsIgnoreCase("+x")) {
			if (blueprint.setOrigin(1, 0, 0))
				return Prompt.END_OF_CONVERSATION;
			return this;
		} else if (input.equalsIgnoreCase("-x")) {
			if (blueprint.setOrigin(-1, 0, 0))
				return Prompt.END_OF_CONVERSATION;
			return this;
		} else if (input.equalsIgnoreCase("+y")) {
			if (blueprint.setOrigin(0, 1, 0))
				return Prompt.END_OF_CONVERSATION;
			return this;
		} else if (input.equalsIgnoreCase("-y")) {
			if (blueprint.setOrigin(0, -1, 0))
				return Prompt.END_OF_CONVERSATION;
			return this;
		} else if (input.equalsIgnoreCase("+z")) {
			if (blueprint.setOrigin(0, 0, 1))
				return Prompt.END_OF_CONVERSATION;
			return this;
		} else if (input.equalsIgnoreCase("-z")) {
			if (blueprint.setOrigin(0, 0, -1))
				return Prompt.END_OF_CONVERSATION;
			return this;
		}
		return Prompt.END_OF_CONVERSATION;
	}
}

