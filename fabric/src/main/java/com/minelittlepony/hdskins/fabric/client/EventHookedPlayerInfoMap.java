package com.minelittlepony.hdskins.fabric.client;

import com.google.common.collect.ForwardingMap;
import com.minelittlepony.hdskins.fabric.client.callbacks.AddPlayerCallback;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Map;
import java.util.UUID;

public class EventHookedPlayerInfoMap extends ForwardingMap<UUID, PlayerListEntry> {

    private final Map<UUID, PlayerListEntry> delegate;

    public EventHookedPlayerInfoMap(Map<UUID, PlayerListEntry> playerInfoMap) {
        this.delegate = playerInfoMap;
    }

    @Override
    protected Map<UUID, PlayerListEntry> delegate() {
        return delegate;
    }

    @Override
    public PlayerListEntry put(UUID key, PlayerListEntry value) {
        AddPlayerCallback.EVENT.invoker().onAddPlayer(value);
        return super.put(key, value);
    }
}
