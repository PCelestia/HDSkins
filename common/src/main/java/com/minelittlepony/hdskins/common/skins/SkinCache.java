package com.minelittlepony.hdskins.common.skins;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SkinCache {

    private static final Logger logger = LogManager.getLogger();

    private final LoadingCache<GameProfile, CompletableFuture<MinecraftTexturesPayload>> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(CacheLoader.from(this::loadTexture));

    private final SkinServerList serverList;
    private final Supplier<MinecraftSessionService> sessionGetter;

    public SkinCache(SkinServerList servers, Supplier<MinecraftSessionService> sessionGetter) {
        this.serverList = servers;
        this.sessionGetter = sessionGetter;
    }

    private CompletableFuture<MinecraftTexturesPayload> loadTexture(GameProfile profile) {
        MinecraftSessionService sessionService = sessionGetter.get();
        return CompletableFuture.supplyAsync(() -> {

            Map<Type, MinecraftProfileTexture> textures = new EnumMap<>(Type.class);
            for (SkinServer server : this.serverList.getSkinServers()) {
                if (server.getFeatures().contains(Feature.SYNTHETIC)) continue;
                if (!server.getFeatures().contains(Feature.DOWNLOAD_USER_SKIN)) continue;
                try {
                    server.loadProfileData(sessionService, profile).forEach(textures::putIfAbsent);
                } catch (IOException e) {
                    logger.error("Failed to get texture data from {}.", server, e);
                }
            }

            long timestamp = System.currentTimeMillis();
            UUID profileId = profile.getId();
            String profileName = profile.getName();
            return new TexturesPayload(timestamp, profileId, profileName, true, textures);
        });
    }

    public CompletableFuture<MinecraftTexturesPayload> getPayload(GameProfile profile) {
        return CACHE.getUnchecked(profile);
    }
}