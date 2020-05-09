package com.minelittlepony.hdskins.common;

import com.google.common.collect.ForwardingMap;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class EventHookedNetworkPlayerMap<T> extends ForwardingMap<UUID, T> {

    private final Map<UUID, T> delegate;
    private final Consumer<T> callback;

    public EventHookedNetworkPlayerMap(Map<UUID, T> delegate, Consumer<T> callback) {
        this.delegate = delegate;
        this.callback = callback;
    }

    @Override
    protected Map<UUID, T> delegate() {
        return delegate;
    }

    @Override
    public T put(UUID key, T value) {
        this.callback.accept(value);
        return super.put(key, value);
    }
}
