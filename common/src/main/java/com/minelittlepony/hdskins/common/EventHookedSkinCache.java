package com.minelittlepony.hdskins.common;

import com.google.common.cache.ForwardingLoadingCache;
import com.minelittlepony.hdskins.common.skins.SkinCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class EventHookedSkinCache extends ForwardingLoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> {

    private final Supplier<SkinCache> skinCache;

    protected EventHookedSkinCache(Supplier<SkinCache> skinServerList) {
        this.skinCache = skinServerList;
    }

    @Override
    public Map<Type, MinecraftProfileTexture> getUnchecked(GameProfile key) {
        CompletableFuture<Map<Type, MinecraftProfileTexture>> future = skinCache.get().getPayload(key)
                .thenApply(MinecraftTexturesPayload::getTextures);

        // it may take some time to get the skins.
        Map<Type, MinecraftProfileTexture> skins = super.getUnchecked(key);
        skins.putAll(future.getNow(Collections.emptyMap()));
        return skins;
    }
}
