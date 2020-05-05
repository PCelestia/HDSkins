package com.minelittlepony.hdskins.fabric.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;

import java.util.function.Consumer;

public interface InitScreenCallback {

    Event<InitScreenCallback> EVENT = EventFactory.createArrayBacked(InitScreenCallback.class, listeners -> (screen, addButton) -> {
        for (InitScreenCallback callback : listeners) {
            callback.onScreenInit(screen, addButton);
        }
    });

    void onScreenInit(Screen screen, Consumer<AbstractButtonWidget> addButton);
}
