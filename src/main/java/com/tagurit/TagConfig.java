package com.tagurit;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tagurit")
public interface TagConfig extends Config
{
	@ConfigItem(
		keyName = "players",
		name = "Who's playing?",
		description = "Enter comma-separated list of participants rsn's."
	)
	default String getPlayers()
	{
		return "";
	}

	@ConfigItem(
			keyName = "players",
			name = "",
			description = ""
	)
	void setPlayers(String key);


}
