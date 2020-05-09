package com.minelittlepony.hdskins.forge.client;

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
import com.minelittlepony.hdskins.forge.ForgeObfHelper;
import com.minelittlepony.hdskins.forge.client.gui.screen.MCPScreenWrapper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class HDSkinsClient {
    private static final Logger logger = LogManager.getLogger();

    private SkinCache skinCache;
    private final List<PendingSkin> pendingSkins = new LinkedList<>();

    private boolean skinCacheLoaderReplaced;

    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggedInEvent event) {
        ClientPlayerEntity player = event.getPlayer();
        if (player != null) {
            replaceNetworkPlayerMap(player.connection, this::onPlayerAdd);
        }

        pendingSkins.clear();
        skinCache = new SkinCache(IHDSkins.instance().getSkinServers(), Minecraft.getInstance()::getSessionService);
    }

    private Texture mapDownloadingTextureToHD(Texture textureObj) {
        if (textureObj instanceof DownloadingTexture && !(textureObj instanceof HDDownloadingTexture)) {
            textureObj = new HDDownloadingTexture((DownloadingTexture) textureObj);
        }
        return textureObj;
    }

    private SkinCache getSkinCache() {
        return skinCache;
    }

    private void onPlayerAdd(NetworkPlayerInfo player) {
        skinCache.getPayload(player.getGameProfile())
                .thenAcceptAsync(payload -> loadSkins(player, payload.getTextures()), Minecraft.getInstance())
                .exceptionally(t -> {
                    logger.catching(t);
                    return null;
                });
    }

    private void loadSkins(NetworkPlayerInfo player, Map<Type, MinecraftProfileTexture> textures) {
        RenderSystem.recordRenderCall(() -> {
            for (Type textureType : Type.values()) {
                if (textures.containsKey(textureType)) {
                    MinecraftProfileTexture texture = textures.get(textureType);
                    loadSkin(texture, textureType, location -> pendingSkins.add(new PendingSkin(player, textureType, location, texture)));
                }
            }
        });
    }

    private void loadSkin(MinecraftProfileTexture profileTexture, Type textureType, @Nullable Consumer<ResourceLocation> callback) {

        SkinManager skins = Minecraft.getInstance().getSkinManager();
        skins.loadSkin(profileTexture, textureType, (type, location, texture) -> {
            if (callback != null) {
                callback.accept(location);
            }
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            pendingSkins.removeIf(this::setPlayerSkin);

            if (!skinCacheLoaderReplaced) {
                skinCache = new SkinCache(IHDSkins.instance().getSkinServers(), Minecraft.getInstance()::getSessionService);

                SkinManager skins = Minecraft.getInstance().getSkinManager();
                replaceSkinCacheLoader(skins, this::getSkinCache);
                replaceSkinTextureManager(skins, this::mapDownloadingTextureToHD);
                skinCacheLoaderReplaced = true;
            }
        }
    }

    private boolean setPlayerSkin(PendingSkin skin) {
        if (skin.player.playerTexturesLoaded) {
            skin.player.playerTextures.put(skin.type, skin.location);
            if (skin.type == Type.SKIN) {
                skin.player.skinType = skin.texture.getMetadata("model");
                if (skin.player.skinType == null) {
                    skin.player.skinType = "default";
                }
            }
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onScreenInit(GuiScreenEvent.InitGuiEvent event) {
        if (event.getGui() instanceof MainMenuScreen) {
            Screen screen = event.getGui();
            event.addWidget(new Button(screen.width - 25, screen.height - 50, 20, 20, "S", b -> {
                Minecraft.getInstance().displayGuiScreen(createSkinsUpload(screen));
            }));
        }
    }

    public static Screen createSkinsUpload(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        return new MCPScreenWrapper(parent, new SkinUploadScreen(
                new Uploader(IHDSkins.instance().getSkinServers().getSkinServers().get(0),
                        sessionFromVanilla(mc.getSession()), mc.getSessionService()),
                (a) -> new FileDrop(mc, mc.getMainWindow()::getHandle, a)
        ));
    }

    private static Session sessionFromVanilla(net.minecraft.util.Session session) {
        return new Session(session.getToken(), session.getProfile());
    }

    private static final ObfHelper<ClientPlayNetHandler, Map<UUID, NetworkPlayerInfo>>
            ClientPlayNetHandler_playerInfoMap = new ForgeObfHelper<>(ClientPlayNetHandler.class, "field_147310_i", Map.class);
    private static final ObfHelper<SkinManager, LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>>>
            SkinManager_skinCacheLoader = new ForgeObfHelper<>(SkinManager.class, "field_152798_f", LoadingCache.class);
    private static final ObfHelper<SkinManager, TextureManager>
            SkinManager_textureManager = new ForgeObfHelper<>(SkinManager.class, "field_152795_c", TextureManager.class);

    private static void replaceNetworkPlayerMap(ClientPlayNetHandler handler, Consumer<NetworkPlayerInfo> callback) {
        Map<UUID, NetworkPlayerInfo> info = ClientPlayNetHandler_playerInfoMap.get(handler);
        info = new EventHookedNetworkPlayerMap<>(info, callback);
        ClientPlayNetHandler_playerInfoMap.set(handler, info);
        logger.info("Replaced {} to detect when a player joins the server.", ClientPlayNetHandler_playerInfoMap);
    }

    private static void replaceSkinCacheLoader(SkinManager skins, Supplier<SkinCache> skinCache) {
        // replace the skin cache to make it return my skins instead
        LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> vanillaCache = SkinManager_skinCacheLoader.get(skins);
        vanillaCache = new EventHookedSkinCache(vanillaCache, skinCache);
        SkinManager_skinCacheLoader.set(skins, vanillaCache);
        logger.info("Replaced {} to handle non-player skins.", SkinManager_skinCacheLoader);
    }

    private static void replaceSkinTextureManager(SkinManager skins, UnaryOperator<Texture> callback) {
        // Replacing the texture manager allows me to intercept loadTexture and
        // substitute the Texture object with one that supports HD Skins.
        TextureManager textures = SkinManager_textureManager.get(skins);
        textures = new ForwardingTextureManager(textures) {
            @Override
            public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
                super.loadTexture(textureLocation, callback.apply(textureObj));
            }
        };
        SkinManager_textureManager.set(skins, textures);
        logger.info("Replaced {} to handle HD skins.", SkinManager_textureManager);
    }

    private static class PendingSkin {
        private final NetworkPlayerInfo player;
        private final Type type;
        private final ResourceLocation location;
        private final MinecraftProfileTexture texture;

        private PendingSkin(NetworkPlayerInfo player, Type type, ResourceLocation location, MinecraftProfileTexture texture) {
            this.player = player;
            this.type = type;
            this.location = location;
            this.texture = texture;
        }
    }
}
