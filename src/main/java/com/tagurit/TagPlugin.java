package com.tagurit;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.text.html.HTML;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.time.Instant;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Tag Ur It"
)
public class TagPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private TagConfig config;

	@Inject
	private WSClient wsClient;

	@Inject
	private PartyService party;

	private TagEvent whosit;

	@Override
	protected void startUp() throws Exception
	{
		//initialize who's it if stored in config
		if(!config.getWhosIt().isEmpty()) {
			this.whosit = deserializeTagEvent(config.getWhosIt());
		}

		wsClient.registerMessage(TagEvent.class);
		wsClient.registerMessage(SyncEvent.class);
		log.info("Tag started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		config.setWhosIt(serializeTagEvent(whosit));
		wsClient.unregisterMessage(TagEvent.class);
		wsClient.unregisterMessage(SyncEvent.class);

		log.info("Tag stopped!");
	}

	@Provides
	TagConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TagConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals("tagurit")) {
            if (event.getKey().equals("whosit")) {
				if(this.whosit == null) {
					this.whosit = new TagEvent(Instant.now().getNano(), client.getLocalPlayer().getName(), "None", new WorldPoint(0, 0, 0));
				}
            }
        }
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event){
		if(event.getGameState().equals(GameState.LOGGED_IN)){
			party.send(new SyncEvent(this.whosit));
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event){
		if (event.getType() == ChatMessageType.TRADEREQ) {
            String sender = event.getName();
			if(party.getMemberByDisplayName(event.getName()) != null && event.getName().equals(whosit.getTaggee())){
				 //you've been tagged, broadcast message, log event.
				System.out.println("name: " + event.getName());
				System.out.println("time: " + event.getTimestamp());
				TagEvent tagEvent = new TagEvent(Instant.now().getEpochSecond(), client.getLocalPlayer().getName(), sender, client.getLocalPlayer().getWorldLocation());
            	party.send(tagEvent);
			}
        }
		System.out.println("message: " + event.getMessage());
		if(event.getMessage().equalsIgnoreCase("!whosit")){
			if(whosit != null) {
				client.addChatMessage(ChatMessageType.CLAN_GIM_CHAT, client.getLocalPlayer().getName(), whosit.getTaggee() + " is currently it.", "", true);
			}else{
				client.addChatMessage(ChatMessageType.CLAN_GIM_CHAT, client.getLocalPlayer().getName(), "Dunno.", "", true);
			}
		}
		if(event.getMessage().equalsIgnoreCase("!lasttag")){
			if(whosit != null) {
				long seconds = Instant.now().getEpochSecond() - whosit.getTimestamp();
				long dayDiff = seconds / 86400;
				long hourDiff = seconds / 3600 % 24;
				long minDiff = seconds / 60 % 60;
				client.addChatMessage(ChatMessageType.CLAN_GIM_CHAT, client.getLocalPlayer().getName(), whosit.getTaggee() + " was tagged by " + whosit.getTagger() + " " + dayDiff + " Days, " + hourDiff + " hours, and " + minDiff + " minutes ago.", "", true);
			}else{
				client.addChatMessage(ChatMessageType.CLAN_GIM_CHAT, client.getLocalPlayer().getName(), "Dunno.", "", true);
			}
		}
	}

	@Subscribe
	public void onTagEvent(TagEvent event){
		System.out.println("tag event received.");
		this.whosit = event;
		clientThread.invoke(() ->
		{
			client.addChatMessage(ChatMessageType.BROADCAST, event.getTaggee(), event.getTaggee() + " has been tagged by " + event.getTagger() + "!! " + event.getTaggee() + " is it!", "", true);
		});
	}

	@Subscribe
	public void onSyncEvent(SyncEvent event){
		System.out.println("Syncing...");
		if(event.getTagEvent() == null){
			party.send(new SyncEvent(this.whosit));
		}
		else if(event.getTagEvent().getTimestamp() > this.whosit.getTimestamp()){
			this.whosit = event.getTagEvent();
		}
	}

	private String serializeTagEvent(TagEvent event){
		return event.getTimestamp() + "," + event.getTaggee() + "," + event.getTagger() + "," + event.getLocation().getX() + "," + event.getLocation().getY() + "," + event.getLocation().getPlane();
	}

	private TagEvent deserializeTagEvent(String str){
		List<String> tokens = List.of(str.split(","));
		return new TagEvent(Integer.parseInt(tokens.get(0)), tokens.get(1), tokens.get(2), new WorldPoint(Integer.parseInt(tokens.get(3)), Integer.parseInt(tokens.get(4)), Integer.parseInt(tokens.get(5))));
	}
}
