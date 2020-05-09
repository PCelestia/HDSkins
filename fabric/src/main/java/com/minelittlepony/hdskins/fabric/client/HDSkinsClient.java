package com.minelittlepony.hdskins.fabric.client;

import com.google.common.cache.LoadingCache;
import com.minelittlepony.hdskins.common.EventHookedNetworkPlayerMap;
import com.minelittlepony.hdskins.common.EventHookedSkinCache;
import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.common.ObfHelper;
import com.minelittlepony.hdskins.common.file.FileDrop;
import com.minelittlepony.hdskins.common.gui.screen.SkinUploadScreen;
import com.minelittlepony.hdskins.common.skins.Session;
import com.minelittlepony.hdskins.common.skins.SkinCache;
import com.minelittlepony.hdskins.common.upload.Uploader;
import com.minelittlepony.hdskins.fabric.FabricObfHelper;
import com.minelittlepony.hdskins.fabric.client.callbacks.ClientLogInCallback;
import com.minelittlepony.hdskins.fabric.client.callbacks.InitScreenCallback;
import com.minelittlepony.hdskins.fabric.client.gui.screen.YarnScreenWrapper;
import com.minelittlepony.hdskins.fabric.mixin.client.IMixinPlayerListEntry;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
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
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class HDSkinsClient implements ClientModInitializer {
    private static final Logger logger = LogManager.getLogger();

    private SkinCache skinCache;
    private final List<PendingSkin> pendingSkins = new LinkedList<>();

    private boolean skinCacheLoaderReplaced;

    @Override
    public void onInitializeClient() {
        ClientLogInCallback.EVENT.register(this::onClientLogin);
        ClientTickCallback.EVENT.register(this::onTick);
        InitScreenCallback.EVENT.register(this::onScreenInit);
    }

    public void onClientLogin(ClientPlayerEntity player) {
        if (player != null) {
            replaceNetworkPlayerMap(player.networkHandler, this::onPlayerAdd);
        }

        pendingSkins.clear();
        skinCache = new SkinCache(IHDSkins.instance().getSkinServers(), MinecraftClient.getInstance()::getSessionService);

    }

    private AbstractTexture mapPlayerSkinTextureToHD(AbstractTexture abstractTexture) {
        if (abstractTexture instanceof PlayerSkinTexture && !(abstractTexture instanceof HDPlayerSkinTexture)) {
            abstractTexture = new HDPlayerSkinTexture(((IPlayerSkinTextureAccessors) abstractTexture));
        }
        return abstractTexture;
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

    private void loadSkin(MinecraftProfileTexture profileTexture, Type textureType, @Nullable Consumer<Identifier> callback) {
        PlayerSkinProvider skins = MinecraftClient.getInstance().getSkinProvider();
        skins.loadSkin(profileTexture, textureType, (type, identifier, texture) -> {
            if (callback != null) {
                callback.accept(identifier);
            }
        });
    }

    private void onTick(MinecraftClient mc) {
        pendingSkins.removeIf(this::setPlayerSkin);

        if (!skinCacheLoaderReplaced) {
            skinCache = new SkinCache(IHDSkins.instance().getSkinServers(), MinecraftClient.getInstance()::getSessionService);

            PlayerSkinProvider skins = MinecraftClient.getInstance().getSkinProvider();
            replaceSkinCacheLoader(skins);
            replaceSkinTextureManager(skins, this::mapPlayerSkinTextureToHD);
            skinCacheLoaderReplaced = true;
        }
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
                MinecraftClient.getInstance().openScreen(createSkinUpload(screen));
            }));
        }
    }

    public static Screen createSkinUpload(Screen parent) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return new YarnScreenWrapper(parent, new SkinUploadScreen(
                new Uploader(IHDSkins.instance().getSkinServers().getSkinServers().get(0),
                        sessionFromVanilla(mc.getSession()), mc.getSessionService()),
                (a) -> new FileDrop(mc, mc.getWindow()::getHandle, a)
        ));
    }

    private static Session sessionFromVanilla(net.minecraft.client.util.Session session) {
        return new Session(session.getAccessToken(), session.getProfile());
    }
    private static final ObfHelper<ClientPlayNetworkHandler, Map<UUID, PlayerListEntry>>
            ClientPlayNetworkhandler_playerListEntries = new FabricObfHelper<>(ClientPlayNetworkHandler.class, "field_3693", Map.class);
    private static final ObfHelper<PlayerSkinProvider, LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>>>
            PlayerSkinProvider_skinCache = new FabricObfHelper<>(PlayerSkinProvider.class, "field_5306", LoadingCache.class);

    private static final ObfHelper<PlayerSkinProvider, TextureManager>
            PlayerSkinProvider_textureManager = new FabricObfHelper<>(PlayerSkinProvider.class, "field_5304", TextureManager.class);

    private static void replaceNetworkPlayerMap(ClientPlayNetworkHandler handler, Consumer<PlayerListEntry> callback) {
        // ClientPlayNetworkHandler;playerListEntries:Map<UUID, PlayerListEntry>
        Map<UUID, PlayerListEntry> entries = ClientPlayNetworkhandler_playerListEntries.get(handler);
        entries = new EventHookedNetworkPlayerMap<>(entries, callback);
        ClientPlayNetworkhandler_playerListEntries.set(handler, entries);
        logger.info("Replaced {} to detect when a player joins the server.", ClientPlayNetworkhandler_playerListEntries);
    }

    private void replaceSkinCacheLoader(PlayerSkinProvider skins) {
        // replace the skin cache to make it return my skins instead
        LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> vanillaCache = PlayerSkinProvider_skinCache.get(skins);
        vanillaCache = new EventHookedSkinCache(vanillaCache, this::getSkinCache);
        PlayerSkinProvider_skinCache.set(skins, vanillaCache);
        logger.info("Replaced {} to handle non-player skins.", PlayerSkinProvider_skinCache);
    }

    private static void replaceSkinTextureManager(PlayerSkinProvider skins, UnaryOperator<AbstractTexture> callback) {
        // Replacing the texture manager allows me to intercept loadTexture and
        // substitute the Texture object with one that supports HD Skins.
        TextureManager textures = PlayerSkinProvider_textureManager.get(skins);
        textures = new ForwardingTextureManager(textures) {
            @Override
            public void registerTexture(Identifier identifier, AbstractTexture abstractTexture) {
                super.registerTexture(identifier, callback.apply(abstractTexture));
            }
        };
        PlayerSkinProvider_textureManager.set(skins, textures);
        logger.info("Replaced {} to handle HD skins.", PlayerSkinProvider_textureManager);
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
}
