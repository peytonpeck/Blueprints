package me.sizzlemcgrizzle.blueprints.conversation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;

/*
 * Made by SydMontague:
 * https://github.com/SydMontague/CLCore/tree/craftcitizen/src/main/java/de/craftlancer/core/conversation
 */
public abstract class ClickableBooleanPrompt extends BooleanPrompt {
	protected final BaseComponent promptText;

	public ClickableBooleanPrompt(String text) {
		BaseComponent yesComp = new TextComponent("[Yes]");
		yesComp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo yes"));
		yesComp.setColor(ChatColor.GREEN);
		yesComp.setBold(true);
		BaseComponent noComp = new TextComponent("[No]");
		noComp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/convo no"));
		noComp.setColor(ChatColor.RED);
		noComp.setBold(true);

		this.promptText = new TextComponent(text);
		this.promptText.addExtra(" ");
		this.promptText.addExtra(yesComp);
		this.promptText.addExtra(" ");
		this.promptText.addExtra(noComp);
	}

	@Override
	public String getPromptText(ConversationContext context) {
		return ComponentSerializer.toString(promptText);
	}
}
