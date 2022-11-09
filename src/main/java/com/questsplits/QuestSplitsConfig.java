package com.questsplits;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface QuestSplitsConfig extends Config
{
	@ConfigItem(
		keyName = "items",
		name = "Key Items",
		description = "Splits when you recieve these items"
	)
	default String getKeyItems()
	{
		return "Hello";
	}
}
