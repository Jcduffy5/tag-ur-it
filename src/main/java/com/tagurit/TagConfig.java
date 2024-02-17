package com.tagurit;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tagurit")
public interface TagConfig extends Config
{
	@ConfigItem(
		keyName = "whosit",
		name = "tag event information",
		description = "information about most recent tag event."
	)
	default String getWhosIt()
	{
		return "";
	}

	@ConfigItem(
			keyName = "whosit",
			name = "",
			description = ""
	)
	void setWhosIt(String key);


}
