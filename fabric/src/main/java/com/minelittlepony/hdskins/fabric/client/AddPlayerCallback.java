package com.minelittlepony.hdskins.fabric.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.PlayerListEntry;

public interface AddPlayerCallback {

    Event<AddPlayerCallback> EVENT = EventFactory.createArrayBacked(AddPlayerCallback.class, listeners -> player -> {
        for (AddPlayerCallback callback : listeners) {
            callback.onAddPlayer(player);
        }
    });

    void onAddPlayer(PlayerListEntry player);
}
