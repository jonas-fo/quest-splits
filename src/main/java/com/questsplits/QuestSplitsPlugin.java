/*
 * Copyright (c) 2022, Jónas
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.questsplits;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.questsplits.overlays.QuestSplitsOverlay;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Quest Splits"
)
public class QuestSplitsPlugin extends Plugin
{
	private int widgetId = 46596101;
	private boolean worldChange = false;
	private List<String> inventoryItems = new ArrayList<>();
	private Queue<String> keyItems;
	private Map<String, String> times;
	private Map<String, String> bestTimes = new LinkedHashMap<>();
	private Widget[] textFields;
	private QuestSplitsOverlay overlay = new QuestSplitsOverlay(this, textFields);

	private Widget topWidget;

	@Inject
	private Client client;

	@Inject
	private QuestSplitsConfig config;

	@Inject
	private OverlayManager overlayManager;

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
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if(client.getWidget(widgetId) != null)
		{
			topWidget = client.getWidget(widgetId).getParent();
			textFields = client.getWidget(widgetId).getChildren();
			overlay.setTextFields(textFields);
			overlayManager.add(overlay);
			if(keyItems == null || times == null)
			{
				changeKeyItems();
			}
			worldChange = false;
		} else if (!worldChange){
			keyItems = null;
		}
	}

	@Subscribe
	public void onWorldChanged(WorldChanged worldChanged)
	{
		// This is so the times don't reset when changing worlds
		worldChange = true;
	}

	@Subscribe
	public void onGameTick(GameTick gameStateChanged)
	{
		if (client.getWidget(widgetId) == null) return;
		//For some reason the timer doesn't update without the next 2 lines
		textFields = client.getWidget(widgetId).getChildren();
		overlay.setTextFields(textFields);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		inventoryItems = new ArrayList<>();
		if(!(itemContainerChanged.getContainerId() == InventoryID.INVENTORY.getId() || itemContainerChanged.getContainerId() == InventoryID.EQUIPMENT.getId()))
		{
			return;
		}
		for( Item item : itemContainerChanged.getItemContainer().getItems())
		{
			addItemToInventory(item);
		}
		for( String item : inventoryItems){
			// Quests like Dragon Slayer have multiple items with the same name (silverlight key for DS). This allows to use either item ids or names
			String[] dupeCheck = item.split(";");
			if(dupeCheck[0].equalsIgnoreCase(keyItems.peek()) || item.equalsIgnoreCase(keyItems.peek()))
			{
				String foundItem = keyItems.remove().toLowerCase();
				String time = "" + textFields[2].getText();
				if(foundItem.contains(";")) times.put(item.toLowerCase(), time);
				else times.put(dupeCheck[0].toLowerCase(), time);
				StringBuilder newSplits = new StringBuilder();
				String[] splits = config.getSplitItems().split(",");
				for(String split : splits)
				{
					String[] seperated = split.split("_");
					if(seperated[0].equalsIgnoreCase(foundItem) && (timeToInt(seperated[1]) > timeToInt(time) || timeToInt(seperated[1]) == 0))
					{
						seperated[1] = time;
						bestTimes.put(dupeCheck[0].toLowerCase(),time);
					}
					if(foundItem.toLowerCase().endsWith(seperated[0].toLowerCase()) && (timeToInt(seperated[1]) > timeToInt(time) || timeToInt(seperated[1]) == 0))
					{
						seperated[1] = time;
						bestTimes.put(item.toLowerCase(),time);
					}
					newSplits.append(seperated[0]).append("_").append(seperated[1]);
					if(!split.equals(splits[splits.length - 1]))
					{
						newSplits.append(",");
					}
				}
				config.setSplitItems(newSplits.toString());
				return;
			}
		}
	}

	@Provides
	QuestSplitsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QuestSplitsConfig.class);
	}

	void changeKeyItems()
	{
		keyItems = new LinkedList<>();
		bestTimes = new LinkedHashMap<>();
		String[] items = config.getSplitItems().split(",");
		for(String item : items)
		{
			String[] splitted = item.split("_");
			try
			{
				int id = Integer.parseInt(splitted[0]);
				splitted[0] = client.getItemDefinition(id).getName().toLowerCase() + ";" + id;
			} catch (Exception ignored)
			{
			}
			keyItems.add(splitted[0]);
			bestTimes.put(splitted[0].toLowerCase(), splitted[1]);
		}
		times = new LinkedHashMap<String, String>();
		for(String keyItem : keyItems)
		{
			times.put(keyItem.toLowerCase(), "0:00.00");
		}
	}
	void addItemToInventory(Item item)
	{
		String newItem = client.getItemDefinition(item.getId()).getName() + ";" + item.getId();
		if(!inventoryItems.contains(newItem) && !Objects.equals(newItem, "null"))
		{
			inventoryItems.add(newItem);
		}
	}

	public Map<String, String> getTimes()
	{
		return times;
	}

	public Map<String, String> getBestTimes()
	{
		return bestTimes;
	}

	int timeToInt(String time)
	{
		String result = time.replace(":","");
		result = result.replace(".","");
		return Integer.parseInt(result);
	}

	public QuestSplitsConfig getConfig()
	{
		return config;
	}
}
