package com.minelittlepony.hdskins.common;

import com.google.common.cache.LoadingCache;
import com.minelittlepony.hdskins.common.skins.SkinCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public abstract class HDSkinsClientEvents<PlayerInfo, Location, ClientConnection, SkinProvider, TextureManager, Texture> {

    private static final Logger logger = LogManager.getLogger();

    private SkinCache skinCache;
    private final List<PendingSkin> pendingSkins = new LinkedList<>();

    private boolean skinCacheLoaderReplaced;

    protected SkinCache getSkinCache() {
        return skinCache;
    }

    protected void onClientLogin() {
        pendingSkins.clear();
        skinCache = new SkinCache(IHDSkins.instance().getSkinServers(), this::getSessionService);
    }

    protected void onTick() {
        pendingSkins.removeIf(this::setPlayerSkin);

        if (!skinCacheLoaderReplaced) {
            skinCache = new SkinCache(IHDSkins.instance().getSkinServers(), this::getSessionService);

            SkinProvider skins = this.getSkinProvider();
            replaceSkinCacheLoader(skins);
            replaceSkinTextureManager(skins, this::mapPlayerSkinTextureToHD);
            skinCacheLoaderReplaced = true;
        }
    }

    protected void addPendingSkin(PendingSkin pending) {
        this.pendingSkins.add(pending);
    }

    protected abstract Texture mapPlayerSkinTextureToHD(Texture texture);

    protected abstract SkinProvider getSkinProvider();

    protected abstract MinecraftSessionService getSessionService();

    protected abstract boolean setPlayerSkin(PendingSkin pendingSkin);

    protected abstract void onPlayerAdd(PlayerInfo player);

    protected abstract ObfHelper<ClientConnection, Map<UUID, PlayerInfo>> getPlayerInfoMap();

    protected abstract ObfHelper<SkinProvider, LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>>> getPlayerSkinCache();

    protected abstract ObfHelper<SkinProvider, TextureManager> getTextureManager();

    protected abstract TextureManager forwardTextures(TextureManager textures, UnaryOperator<Texture> callback);

    protected void replaceNetworkPlayerMap(ClientConnection handler) {
        // ClientPlayNetworkHandler;playerListEntries:Map<UUID, PlayerListEntry>
        ObfHelper<ClientConnection, Map<UUID, PlayerInfo>> playerInfoMap = getPlayerInfoMap();
        Map<UUID, PlayerInfo> entries = playerInfoMap.get(handler);
        entries = new EventHookedNetworkPlayerMap<>(entries, this::onPlayerAdd);
        playerInfoMap.set(handler, entries);
        logger.info("Replaced {} to detect when a player joins the server.", playerInfoMap);
    }

    protected void replaceSkinCacheLoader(SkinProvider skins) {
        // replace the skin cache to make it return my skins instead
        ObfHelper<SkinProvider, LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>>> skinCache = getPlayerSkinCache();
        LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> vanillaCache = skinCache.get(skins);
        vanillaCache = new EventHookedSkinCache(vanillaCache, this::getSkinCache);
        skinCache.set(skins, vanillaCache);
        logger.info("Replaced {} to handle non-player skins.", skinCache);
    }

    protected void replaceSkinTextureManager(SkinProvider skins, UnaryOperator<Texture> callback) {
        // Replacing the texture manager allows me to intercept loadTexture and
        // substitute the Texture object with one that supports HD Skins.
        ObfHelper<SkinProvider, TextureManager> textureManager = getTextureManager();
        TextureManager textures = textureManager.get(skins);
        textures = forwardTextures(textures, callback);
        textureManager.set(skins, textures);
        logger.info("Replaced {} to handle HD skins.", textureManager);
    }

    protected class PendingSkin {
        public final PlayerInfo player;
        public final Type type;
        public final Location identifier;
        public final MinecraftProfileTexture texture;

        public PendingSkin(PlayerInfo player, Type type, Location identifier, MinecraftProfileTexture texture) {
            this.player = player;
            this.type = type;
            this.identifier = identifier;
            this.texture = texture;
        }
    }
}
