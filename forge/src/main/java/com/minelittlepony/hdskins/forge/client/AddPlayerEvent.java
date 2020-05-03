package com.minelittlepony.hdskins.forge.client;

import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraftforge.eventbus.api.Event;

public class AddPlayerEvent extends Event {
    private final NetworkPlayerInfo player;

    public AddPlayerEvent(NetworkPlayerInfo player) {

        this.player = player;
    }

    public NetworkPlayerInfo getPlayer() {
        return player;
    }
}
