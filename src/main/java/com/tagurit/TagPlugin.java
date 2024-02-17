package com.tagurit;

import com.google.common.cache.LoadingCache;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@PluginDescriptor(
	name = "Tag Ur It"
)
public class TagPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TagConfig config;

	@Inject
	private ScheduledExecutorService executor;
	@Inject
	private ChatMessageManager chatMessageManager;

	private List<String> playerList = new CopyOnWriteArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		executor.execute(this::reset);
		log.info("Tag started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Tag stopped!");
	}

	@Provides
	TagConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TagConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getKey().equals("players")) {
			executor.execute(this::reset);
		}
	}

	@Subscribe
	public void onTradeReceived(ChatMessage event){
		if (event.getType() == ChatMessageType.TRADEREQ) {
            String sender = event.getName();
            if (playerList.contains(sender)) {
				 //you've been tagged, broadcast message, log event.
				System.out.println("sender: " + event.getSender());
				System.out.println("name: " + event.getName());
				System.out.println("time: " + event.getTimestamp());
				TagEvent tagEvent = new TagEvent(client.getLocalPlayer().getName(),sender, Instant.now().getNano(), client.getLocalPlayer().getWorldLocation());
            	broadcastTag(tagEvent);
				//TODO: log event somehow
			}
        }
	}

	public void broadcastTag(TagEvent event){
		addChatMessage(event.getTaggee(), event.getTaggee() + " has been tagged by " + event.getTagger() + "!!", ChatMessageType.CLAN_GIM_CHAT);
	}

	private void addChatMessage(String sender, String message, ChatMessageType type)
	{
		String chatMessage = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append(message)
				.build();

		chatMessageManager.queue(QueuedMessage.builder()
				.type(type)
				.sender("Tag")
				.name(sender)
				.runeLiteFormattedMessage(chatMessage)
				.timestamp((int) (System.currentTimeMillis() / 1000))
				.build());
	}

	private void reset(){
		// gets list of players from text box in the config
		playerList = Text.fromCSV(config.getPlayers());
	}
}
