package com.minelittlepony.hdskins.fabric.client;

import com.google.common.cache.LoadingCache;
import com.minelittlepony.hdskins.common.EventHookedNetworkPlayerMap;
import com.minelittlepony.hdskins.common.EventHookedSkinCache;
import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.common.file.FileDrop;
import com.minelittlepony.hdskins.common.gui.screen.SkinUploadScreen;
import com.minelittlepony.hdskins.common.skins.Session;
import com.minelittlepony.hdskins.common.skins.SkinCache;
import com.minelittlepony.hdskins.common.upload.Uploader;
import com.minelittlepony.hdskins.fabric.client.callbacks.AddPlayerCallback;
import com.minelittlepony.hdskins.fabric.client.callbacks.ClientLogInCallback;
import com.minelittlepony.hdskins.fabric.client.callbacks.InitScreenCallback;
import com.minelittlepony.hdskins.fabric.client.gui.screen.YarnScreenWrapper;
import com.minelittlepony.hdskins.fabric.mixin.client.IMixinPlayerListEntry;
import com.minelittlepony.hdskins.fabric.mixin.client.IMixinPlayerSkinProvider;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class HDSkinsClient implements ClientModInitializer {
    private static final Logger logger = LogManager.getLogger();

    private SkinCache skinCache;
    private final List<PendingSkin> pendingSkins = new LinkedList<>();

    private boolean skinCacheLoaderReplaced;

    @Override
    public void onInitializeClient() {
        AddPlayerCallback.EVENT.register(this::onPlayerAdd);
        ClientLogInCallback.EVENT.register(this::onClientLogin);
        ClientTickCallback.EVENT.register(this::onTick);
        InitScreenCallback.EVENT.register(this::onScreenInit);
    }

    public void onClientLogin(ClientPlayerEntity player) {
        if (player != null) {
            replaceNetworkPlayerMap(player.networkHandler);
            logger.info("Replaced ClientPlayNetworkHandler.playerListEntries");
        }

        pendingSkins.clear();
        skinCache = new SkinCache(IHDSkins.instance().getSkinServers(), MinecraftClient.getInstance()::getSessionService);

        if (!skinCacheLoaderReplaced) {
            IMixinPlayerSkinProvider skins = (IMixinPlayerSkinProvider) MinecraftClient.getInstance().getSkinProvider();
            replaceSkinCacheLoader(skins);
            replaceSkinTextureManager(skins);
            skinCacheLoaderReplaced = true;
        }
    }

    private void replaceSkinCacheLoader(IMixinPlayerSkinProvider skins) {

        // replace the skin cache to make it return my skins instead
        final LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> vanillaCache = skins.getSkinCache();
        skins.setSkinCache(new EventHookedSkinCache(this::getSkinCache) {
            @Override
            protected LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> delegate() {
                return vanillaCache;
            }
        });
    }

    private void replaceSkinTextureManager(IMixinPlayerSkinProvider skins) {
        // Replacing the texture manager allows me to intercept loadTexture and
        // substitute the Texture object with one that supports HD Skins.
        skins.setTextureManager(new ForwardingTextureManager(skins.getTextureManager()) {
            @Override
            public void registerTexture(Identifier identifier, AbstractTexture abstractTexture) {
                if (abstractTexture instanceof PlayerSkinTexture && !(abstractTexture instanceof HDPlayerSkinTexture)) {
                    abstractTexture = new HDPlayerSkinTexture(((IPlayerSkinTextureAccessors) abstractTexture));
                }
                super.registerTexture(identifier, abstractTexture);
            }
        });
    }

    private SkinCache getSkinCache() {
        return skinCache;
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
        logger.debug("Loaded skins for {}: {}", player.getProfile().getName(), textures);
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
        PlayerSkinProvider skins = MinecraftClient.getInstance().getSkinProvider();
        skins.loadSkin(profileTexture, textureType, (type, identifier, texture) -> {
            if (callback != null) {
                callback.onSkinAvailable(identifier);
            }
        });
    }

    private void onTick(MinecraftClient mc) {
        pendingSkins.removeIf(this::setPlayerSkin);
    }

    private boolean setPlayerSkin(PendingSkin skin) {
        IMixinPlayerListEntry playerEntry = (IMixinPlayerListEntry) skin.player;
        if (playerEntry.isTexturesLoaded()) {
            logger.debug("Setting {} for {} to {}.", skin.type, skin.player.getProfile().getName(), skin.identifier);
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

    private void onScreenInit(Screen screen, Consumer<AbstractButtonWidget> addButton) {
        if (screen instanceof TitleScreen) {
            addButton.accept(new ButtonWidget(screen.width - 25, screen.height - 50, 20, 20, "S", b -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.openScreen(new YarnScreenWrapper(new SkinUploadScreen(
                        new Uploader(IHDSkins.instance().getSkinServers().getSkinServers().get(0),
                                sessionFromVanilla(mc.getSession()), mc.getSessionService()),
                        (a) -> new FileDrop(mc, mc.getWindow()::getHandle, a)
                )));
            }));
        }
    }

    private static Session sessionFromVanilla(net.minecraft.client.util.Session session) {
        return new Session(session.getAccessToken(), session.getProfile());
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
            info = new EventHookedNetworkPlayerMap<PlayerListEntry>(info) {
                @Override
                protected void firePutEvent(PlayerListEntry value) {
                    AddPlayerCallback.EVENT.invoker().onAddPlayer(value);
                }
            };
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
