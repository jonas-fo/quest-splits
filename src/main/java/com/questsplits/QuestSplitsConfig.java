package com.questsplits;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("questsplits")
public interface QuestSplitsConfig extends Config
{
	@ConfigItem(
		keyName = "splitItems",
		name = "Split Items",
		description = "Splits when you receive these items, with best times, write 0:00.00 for no best time, usage (item)_(time),(item)_(time)"
	)
	default String getSplitItems()
	{
		return "egg_0:00.00,pot of flour_0:00.00,bucket of milk_0:00.00";
	}
	@ConfigItem(
			keyName = "splitItems",
			name = "",
			description = ""
	)
	void setSplitItems(String key);

	@ConfigItem(
			keyName = "showSplits",
			name = "Show Splits",
			description = "Shows the splits on the side"
	)
	default boolean showSplits()
	{
		return true;
	}
}
