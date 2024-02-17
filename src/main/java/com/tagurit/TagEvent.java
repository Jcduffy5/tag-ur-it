package com.tagurit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
@NoArgsConstructor
public class TagEvent {
    private int timestamp;
    private String taggee;
    private String tagger;
    private WorldPoint location;

    public TagEvent(String taggee, String tagger, int timestamp, WorldPoint location){
        this.taggee = taggee;
        this.tagger = tagger;
        this.timestamp = timestamp;
        this.location = location;
    }
}
