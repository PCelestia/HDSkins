package com.minelittlepony.hdskins.fabric.client;

import com.google.common.hash.Hashing;
import com.minelittlepony.hdskins.fabric.HDSkins;
import com.minelittlepony.hdskins.fabric.mixin.client.IMixinPlayerListEntry;
import com.minelittlepony.hdskins.skins.SkinCache;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HDSkinsClient implements ClientModInitializer {
    private static final Logger logger = LogManager.getLogger();

    private SkinCache skinCache;
    private final List<PendingSkin> pendingSkins = new LinkedList<>();

    @Override
    public void onInitializeClient() {
        AddPlayerCallback.EVENT.register(this::onPlayerAdd);
        ClientLogInCallback.EVENT.register(this::onClientLogin);
        ClientTickCallback.EVENT.register(this::onTick);
    }

    public void onClientLogin(ClientPlayerEntity player) {
        if (player != null) {
            replaceNetworkPlayerMap(player.networkHandler);
            logger.info("Replaced ClientPlayNetHandler.playerInfoMap");
        }

        pendingSkins.clear();
        skinCache = new SkinCache(HDSkins.instance().getSkinServers(), MinecraftClient.getInstance()::getSessionService);
    }


    private void onPlayerAdd(PlayerListEntry player) {

        skinCache.getPayload(player.getProfile())
                .thenAcceptAsync(payload -> loadSkins(player, payload.getTextures()), MinecraftClient.getInstance())
                .exceptionally(t -> {
                    logger.catching(t);
                    return null;
                });
    }

    private void loadSkins(PlayerListEntry player, Map<Type, MinecraftProfileTexture> textures) {
        RenderSystem.recordRenderCall(() -> {
            for (Type textureType : Type.values()) {
                if (textures.containsKey(textureType)) {
                    MinecraftProfileTexture texture = textures.get(textureType);
                    loadSkin(texture, textureType, location -> pendingSkins.add(new PendingSkin(player, textureType, location, texture)));
                }
            }
        });
    }

    private void loadSkin(MinecraftProfileTexture profileTexture, Type textureType, @Nullable Callback callback) {
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
        String textureName = Hashing.sha1().hashUnencodedChars(profileTexture.getHash()).toString();
        Identifier resourcelocation = new Identifier("hdskins", "skins/" + textureName);
        AbstractTexture texture = textureManager.getTexture(resourcelocation);
        if (texture != null) {
            if (callback != null) {
                callback.onSkinAvailable(resourcelocation);
            }
        } else {
            String prefix = textureName.length() > 2 ? textureName.substring(0, 2) : "xx";
            Path path = assetsDir().resolve("hdskins").resolve(prefix).resolve(textureName);
            textureManager.registerTexture(resourcelocation, new HDPlayerSkinTexture(path.toFile(), profileTexture.getUrl(), DefaultSkinHelper.getTexture(), textureType == Type.SKIN, () -> {
                if (callback != null) {
                    callback.onSkinAvailable(resourcelocation);
                }
            }));
        }
    }

    private void onTick(MinecraftClient mc) {
        pendingSkins.removeIf(this::setPlayerSkin);
    }

    private boolean setPlayerSkin(PendingSkin skin) {
        IMixinPlayerListEntry playerEntry = (IMixinPlayerListEntry) skin.player;
        if (playerEntry.isTexturesLoaded()) {
            playerEntry.getTextures().put(skin.type, skin.identifier);
            if (skin.type == Type.SKIN) {
                playerEntry.setModel(skin.texture.getMetadata("model"));
                if (playerEntry.getModel() == null) {
                    playerEntry.setModel("default");
                }
            }
            return true;
        }
        return false;
    }

    private static Path assetsDir() {
        // PlayerSkinProvider.skinCacheDir: File
        String skinCacheDir_fieldName = FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary",
                "net.minecraft.class_1071",
                "field_5305",
                "Ljava/io/File;");
        PlayerSkinProvider skins = MinecraftClient.getInstance().getSkinProvider();
        try {
            Field skinCacheDir_field = PlayerSkinProvider.class.getDeclaredField(skinCacheDir_fieldName);
            skinCacheDir_field.setAccessible(true);
            File skinCacheDir = (File) skinCacheDir_field.get(skins);
            return skinCacheDir.getParentFile().toPath();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void replaceNetworkPlayerMap(ClientPlayNetworkHandler handler) {
        // ClientPlayNetworkHandler;playerListEntries:Map<UUID, PlayerListEntry>
        String playerListEntries_fieldName = FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary",
                "net.minecraft.class_634",
                "field_3693",
                "Ljava/util/Map;");
        try {
            Field playerListEntries_field = ClientPlayNetworkHandler.class.getDeclaredField(playerListEntries_fieldName);
            playerListEntries_field.setAccessible(true);
            Map<UUID, PlayerListEntry> info = (Map<UUID, PlayerListEntry>) playerListEntries_field.get(handler);
            info = new EventHookedPlayerInfoMap(info);
            playerListEntries_field.set(handler, info);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class PendingSkin {
        private final PlayerListEntry player;
        private final Type type;
        private final Identifier identifier;

        private final MinecraftProfileTexture texture;

        private PendingSkin(PlayerListEntry player, Type type, Identifier identifier, MinecraftProfileTexture texture) {
            this.player = player;
            this.type = type;
            this.identifier = identifier;
            this.texture = texture;
        }

    }

    private interface Callback {
        void onSkinAvailable(Identifier identifier);
    }
}
