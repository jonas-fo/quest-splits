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

	private List<String> inventoryItems = new ArrayList<>();
	private Queue<String> keyItems;
	private Map<String, String> times;
	private int splitNumber = 0;
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
		if(client.getWidget(46596101) == null) {
			//topWidget = null;
		} else {
			//topWidget = client.getWidget(46596101).getParent();
		}
		//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.getKeyItems(), null);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if(client.getWidget(46596101) != null)
		{
			topWidget = client.getWidget(46596101).getParent();
			textFields = client.getWidget(46596101).getChildren();
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
		//client.getWidget(46596101).getParent().setHidden(true);
		//overlay.setTextFields(textFields);
		//overlay.revalidate();
		//topWidget.revalidate();
		if (client.getWidget(46596101) == null) return;
		//For some reason the timer doesn't update without the next 2 lines
		textFields = client.getWidget(46596101).getChildren();
		overlay.setTextFields(textFields);
		for(Widget widget : client.getWidget(46596101).getChildren())
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
		changeKeyItems();
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
				times.put(item.toLowerCase(), "" + textFields[2].getText());
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
		String[] items = config.getKeyItems().split(",");
		keyItems.addAll(Arrays.asList(items));
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

	public QuestSplitsConfig getConfig() {
		return config;
	}
}
