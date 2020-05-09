package com.minelittlepony.hdskins.forge.client;

import com.google.common.cache.LoadingCache;
import com.minelittlepony.hdskins.common.HDSkinsClientEvents;
import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.common.ObfHelper;
import com.minelittlepony.hdskins.common.file.FileDrop;
import com.minelittlepony.hdskins.common.gui.screen.SkinUploadScreen;
import com.minelittlepony.hdskins.common.skins.Session;
import com.minelittlepony.hdskins.common.upload.Uploader;
import com.minelittlepony.hdskins.forge.ForgeObfHelper;
import com.minelittlepony.hdskins.forge.client.gui.screen.MCPScreenWrapper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class HDSkinsClient extends HDSkinsClientEvents<
        NetworkPlayerInfo,
        ResourceLocation,
        ClientPlayNetHandler,
        SkinManager,
        TextureManager,
        Texture> {
    private static final Logger logger = LogManager.getLogger();

    @Override
    protected SkinManager getSkinProvider() {
        return Minecraft.getInstance().getSkinManager();
    }

    @Override
    protected MinecraftSessionService getSessionService() {
        return Minecraft.getInstance().getSessionService();
    }

    @SubscribeEvent
    public void onClientLogin(ClientPlayerNetworkEvent.LoggedInEvent event) {
        ClientPlayerEntity player = event.getPlayer();
        if (player != null) {
            replaceNetworkPlayerMap(player.connection);
        }

        onClientLogin();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            onTick();
        }
    }

    @SubscribeEvent
    public void onScreenInit(GuiScreenEvent.InitGuiEvent event) {
        if (event.getGui() instanceof MainMenuScreen) {
            Screen screen = event.getGui();
            event.addWidget(new Button(screen.width - 25, screen.height - 50, 20, 20, "S", b -> {
                Screen toOpen = createSkinsUpload(screen);
                Minecraft.getInstance().displayGuiScreen(toOpen);
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

    protected Texture mapPlayerSkinTextureToHD(Texture textureObj) {
        if (textureObj instanceof DownloadingTexture && !(textureObj instanceof HDDownloadingTexture)) {
            textureObj = new HDDownloadingTexture((DownloadingTexture) textureObj);
        }
        return textureObj;
    }

    protected void onPlayerAdd(NetworkPlayerInfo player) {
        getSkinCache().getPayload(player.getGameProfile())
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
                    loadSkin(texture, textureType, location -> addPendingSkin(new PendingSkin(player, textureType, location, texture)));
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

    @Override
    protected boolean setPlayerSkin(PendingSkin skin) {
        if (skin.player.playerTexturesLoaded) {
            skin.player.playerTextures.put(skin.type, skin.identifier);
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

    private static final ObfHelper<ClientPlayNetHandler, Map<UUID, NetworkPlayerInfo>>
            ClientPlayNetHandler_playerInfoMap = new ForgeObfHelper<>(ClientPlayNetHandler.class, "field_147310_i", Map.class);
    private static final ObfHelper<SkinManager, LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>>>
            SkinManager_skinCacheLoader = new ForgeObfHelper<>(SkinManager.class, "field_152798_f", LoadingCache.class);
    private static final ObfHelper<SkinManager, TextureManager>
            SkinManager_textureManager = new ForgeObfHelper<>(SkinManager.class, "field_152795_c", TextureManager.class);

    @Override
    protected ObfHelper<ClientPlayNetHandler, Map<UUID, NetworkPlayerInfo>> getPlayerInfoMap() {
        return ClientPlayNetHandler_playerInfoMap;
    }

    @Override
    protected ObfHelper<SkinManager, LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>>> getPlayerSkinCache() {
        return SkinManager_skinCacheLoader;
    }

    @Override
    protected ObfHelper<SkinManager, TextureManager> getTextureManager() {
        return SkinManager_textureManager;
    }

    @Override
    protected TextureManager forwardTextures(TextureManager textures, UnaryOperator<Texture> callback) {
        return new ForwardingTextureManager(textures) {
            @Override
            public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
                super.loadTexture(textureLocation, callback.apply(textureObj));
            }
        };
    }
}
