package com.minelittlepony.hdskins.fabric.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayerEntity;

public interface ClientLogInCallback {

    Event<ClientLogInCallback> EVENT = EventFactory.createArrayBacked(ClientLogInCallback.class, listeners -> player -> {
        for (ClientLogInCallback callback : listeners) {
            callback.onClientLogin(player);
        }
    });

    void onClientLogin(ClientPlayerEntity player);
}
