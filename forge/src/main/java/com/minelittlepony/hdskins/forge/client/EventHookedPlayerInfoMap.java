package com.minelittlepony.hdskins.forge.client;

import com.google.common.collect.ForwardingMap;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;
import java.util.UUID;

public class EventHookedPlayerInfoMap extends ForwardingMap<UUID, NetworkPlayerInfo> {

    private final Map<UUID, NetworkPlayerInfo> delegate;

    public EventHookedPlayerInfoMap(Map<UUID, NetworkPlayerInfo> playerInfoMap) {
        this.delegate = playerInfoMap;
    }

    @Override
    protected Map<UUID, NetworkPlayerInfo> delegate() {
        return delegate;
    }

    @Override
    public NetworkPlayerInfo put(UUID key, NetworkPlayerInfo value) {
        MinecraftForge.EVENT_BUS.post(new AddPlayerEvent(value));
        return super.put(key, value);
    }
}
