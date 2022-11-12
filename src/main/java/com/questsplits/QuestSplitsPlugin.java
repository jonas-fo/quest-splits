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
import net.runelite.client.events.ConfigChanged;
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
	private boolean setConfigChanged = false;
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
		changeKeyItems();
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
		if(client.getWidget(widgetId) == null) {
			//topWidget = null;
		} else {
			//topWidget = client.getWidget(46596101).getParent();
		}
		//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.getKeyItems(), null);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if(client.getWidget(widgetId) != null)
		{
			topWidget = client.getWidget(widgetId).getParent();
			textFields = client.getWidget(widgetId).getChildren();
			System.out.println(topWidget.getId());
			//client.getWidget(46596101).getParent().setHidden(true);
			overlay.setTextFields(textFields);
			overlayManager.add(overlay);
			if(keyItems == null || times == null)
			{
				changeKeyItems();
			}
		} else {
			keyItems = null;
		}
	}


	@Subscribe
	public void onGameTick(GameTick gameStateChanged)
	{
		if (client.getWidget(widgetId) == null) return;
		//For some reason the timer doesn't update without the next 2 lines
		textFields = client.getWidget(widgetId).getChildren();
		overlay.setTextFields(textFields);
		for(Widget widget : client.getWidget(widgetId).getChildren())
		{
			//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", times[0], null);
			//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + widget.getRelativeX() + " y = " + widget.getRelativeY(), null);
			//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "" + widget.getText(), null);
			//System.out.println(widget.getText() + " " + widget.getId());
		}
		//client.getWidget(WidgetInfo.INVENTORY).setChildren(inventory);
		// 46596101 is the id of the speedrunning widget
		//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + getKeyItems()[0], null);

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if(Objects.equals(event.getKey(), "splitItems") && !setConfigChanged) {
			changeKeyItems();
		}
		setConfigChanged = false;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		inventoryItems = new ArrayList<>();
		if(!(itemContainerChanged.getContainerId() == InventoryID.INVENTORY.getId()))
		{
			return;
		}
		for( Item item : itemContainerChanged.getItemContainer().getItems())
		{
			addItemToInventory(item);
		}
		for( String item : inventoryItems){
			if(Objects.equals(item.toLowerCase(), keyItems.peek().toLowerCase()))
			{
				String time = "" + textFields[2].getText();
				times.put(item.toLowerCase(), time);
				StringBuilder newSplits = new StringBuilder();
				String[] splits = config.getSplitItems().split(",");
				for(String split : splits)
				{
					String[] seperated = split.split("_");
					if(split.toLowerCase().startsWith(item.toLowerCase()) && (timeToInt(seperated[1]) > timeToInt(time) || timeToInt(seperated[1]) == 0))
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
				setConfigChanged = true;
				config.setSplitItems(newSplits.toString());
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Match found in " + keyItems.remove(), null);
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
		for(String item : items){
			System.out.println(config.getSplitItems());
			keyItems.add(item.split("_")[0]);
			bestTimes.put(item.split("_")[0], item.split("_")[1]);
		}
		times = new LinkedHashMap<String, String>();
		for(String keyItem : keyItems)
		{
			times.put(keyItem.toLowerCase(), "0:00.00");
		}
	}
	void addItemToInventory(Item item)
	{
		String newItem = client.getItemDefinition(item.getId()).getName();
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

	// Remember to remove
	void printInventory()
	{
		String string = "";
		for(String item : inventoryItems)
		{
			string += item + ", ";
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", string, null);
	}

	int timeToInt(String time)
	{
		String result = time.replace(":","");
		result = result.replace(".","");
		return Integer.parseInt(result);
	}

	public QuestSplitsConfig getConfig() {
		return config;
	}
}
