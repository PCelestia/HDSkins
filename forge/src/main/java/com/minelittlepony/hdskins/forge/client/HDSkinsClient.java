package com.minelittlepony.hdskins.forge.client;

import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.minelittlepony.hdskins.common.EventHookedNetworkPlayerMap;
import com.minelittlepony.hdskins.common.EventHookedSkinCache;
import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.common.file.FileDrop;
import com.minelittlepony.hdskins.common.gui.screen.SkinUploadScreen;
import com.minelittlepony.hdskins.common.skins.Session;
import com.minelittlepony.hdskins.common.skins.SkinCache;
import com.minelittlepony.hdskins.common.upload.Uploader;
import com.minelittlepony.hdskins.forge.client.gui.screen.MCPScreenWrapper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HDSkinsClient {
    private static final Logger logger = LogManager.getLogger();
    private static final Path skinCacheDir = assetsDir().resolve("hdskins");

    private SkinCache skinCache;
    private final List<PendingSkin> pendingSkins = new LinkedList<>();

    private boolean skinCacheLoaderReplaced;

    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggedInEvent event) {
        ClientPlayerEntity player = event.getPlayer();
        if (player != null) {
            replaceNetworkPlayerMap(player.connection);
            logger.info("Replaced ClientPlayNetHandler.playerInfoMap");
        }

        pendingSkins.clear();
        skinCache = new SkinCache(IHDSkins.instance().getSkinServers(), Minecraft.getInstance()::getSessionService);

        if (!skinCacheLoaderReplaced) {
            replaceSkinCacheLoader();
            skinCacheLoaderReplaced = true;
        }
    }

    private void replaceSkinCacheLoader() {

        // replace the skin cache to make it return my skins instead
        SkinManager skins = Minecraft.getInstance().getSkinManager();
        final LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> vanillaCache = skins.skinCacheLoader;
        skins.skinCacheLoader = new EventHookedSkinCache(this::getSkinCache) {
            @Override
            protected LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> delegate() {
                return vanillaCache;
            }
        };
    }

    private SkinCache getSkinCache() {
        return skinCache;
    }

    @SubscribeEvent
    public void onPlayerAdd(AddPlayerEvent event) {
        NetworkPlayerInfo player = event.getPlayer();

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

    private void loadSkin(MinecraftProfileTexture profileTexture, Type textureType, @Nullable Callback callback) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        String textureName = Hashing.sha1().hashUnencodedChars(profileTexture.getHash()).toString();
        ResourceLocation resourcelocation = new ResourceLocation("hdskins", "skins/" + textureName);
        Texture texture = textureManager.getTexture(resourcelocation);
        if (texture != null) {
            if (callback != null) {
                callback.onSkinAvailable(resourcelocation);
            }
        } else {
            String prefix = textureName.length() > 2 ? textureName.substring(0, 2) : "xx";
            Path path = skinCacheDir.resolve(prefix).resolve(textureName);
            textureManager.loadTexture(resourcelocation, new HDDownloadingTexture(path.toFile(), profileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkinLegacy(), textureType == Type.SKIN, () -> {
                if (callback != null) {
                    callback.onSkinAvailable(resourcelocation);
                }
            }));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            pendingSkins.removeIf(this::setPlayerSkin);
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
                Minecraft mc = Minecraft.getInstance();
                mc.displayGuiScreen(new MCPScreenWrapper(new SkinUploadScreen(
                        new Uploader(IHDSkins.instance().getSkinServers().getSkinServers().get(0),
                                sessionFromVanilla(mc.getSession()), mc.getSessionService()),
                        (a) -> new FileDrop(mc, mc.getMainWindow()::getHandle, a)
                )));
            }));
        }
    }

    private static Session sessionFromVanilla(net.minecraft.util.Session session) {
        return new Session(session.getToken(), session.getProfile());
    }

    private static Path assetsDir() {
        return Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.ASSETSDIR.get()).orElseThrow(UnsupportedOperationException::new);
    }

    private static void replaceNetworkPlayerMap(ClientPlayNetHandler handler) {
        Map<UUID, NetworkPlayerInfo> info = ObfuscationReflectionHelper.getPrivateValue(ClientPlayNetHandler.class, handler, "field_147310_i");
        info = new EventHookedNetworkPlayerMap<NetworkPlayerInfo>(info) {
            @Override
            protected void firePutEvent(NetworkPlayerInfo value) {
                MinecraftForge.EVENT_BUS.post(new AddPlayerEvent(value));
            }
        };
        ObfuscationReflectionHelper.setPrivateValue(ClientPlayNetHandler.class, handler, info, "field_147310_i");
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

    private interface Callback {
        void onSkinAvailable(ResourceLocation location);
    }
}
