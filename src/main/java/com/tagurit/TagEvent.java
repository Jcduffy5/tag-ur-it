package com.tagurit;

import lombok.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class TagEvent extends PartyMemberMessage {
    long timestamp;
    String taggee;
    String tagger;
    WorldPoint location;
}
