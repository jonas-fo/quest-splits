package com.questsplits;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemMapping;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@PluginDescriptor(
	name = "Quest Splits"
)
public class QuestSplitsPlugin extends Plugin
{

	private List<Item> keyItems = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private QuestSplitsConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.getKeyItems(), null);
	}

	@Subscribe
	public void onGameTick(GameTick gameStateChanged)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + getKeyItems()[0], null);
	}

	@Provides
	QuestSplitsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QuestSplitsConfig.class);
	}

	String[] getKeyItems(){
		String[] items = config.getKeyItems().split(",");
		return items;
	}
}
