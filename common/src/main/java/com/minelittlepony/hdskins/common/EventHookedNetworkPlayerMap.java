package com.minelittlepony.hdskins.common;

import com.google.common.collect.ForwardingMap;

import java.util.Map;
import java.util.UUID;

public abstract class EventHookedNetworkPlayerMap<T> extends ForwardingMap<UUID, T> {

    private final Map<UUID, T> delegate;

    public EventHookedNetworkPlayerMap(Map<UUID, T> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Map<UUID, T> delegate() {
        return delegate;
    }

    @Override
    public T put(UUID key, T value) {
        this.firePutEvent(value);
        return super.put(key, value);
    }

    protected abstract void firePutEvent(T value);
}
