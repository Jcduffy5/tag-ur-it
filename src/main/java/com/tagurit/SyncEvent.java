package com.tagurit;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class SyncEvent extends PartyMemberMessage {
    TagEvent tagEvent;
}
