package com.voxelmodpack.hdskins.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.voxelmodpack.hdskins.HDSkinManager;
import com.voxelmodpack.hdskins.INetworkPlayerInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(NetworkPlayerInfo.class)
public abstract class MixinPlayerInfo implements INetworkPlayerInfo {

    private Map<Type, ResourceLocation> customTextures = new HashMap<>();
    private Map<Type, MinecraftProfileTexture> customProfiles = new HashMap<>();

    @Shadow @Final private GameProfile gameProfile;
    @Shadow Map<Type, ResourceLocation> playerTextures;
    @Shadow private boolean playerTexturesLoaded;
    @Shadow private String skinType;

    @SuppressWarnings("InvalidMemberReference") // mc-dev bug?
    @Redirect(
            method = {
                    "getLocationSkin",
                    "getLocationCape",
                    "getLocationElytra"
            }, at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    // synthetic
    private Object getSkin(Map<Type, ResourceLocation> playerTextures, Object key) {
        return getSkin(playerTextures, (Type) key);
    }

    // with generics
    private ResourceLocation getSkin(Map<Type, ResourceLocation> playerTextures, Type type) {
        return getResourceLocation(type).orElseGet(() -> playerTextures.get(type));
    }

    @Redirect(method = "getSkinType()Ljava/lang/String;",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/NetworkPlayerInfo;skinType:Ljava/lang/String;"))

    private String getTextureModel(NetworkPlayerInfo self) {
        return getProfileTexture(Type.SKIN).map(profile -> {
            String model = profile.getMetadata("model");
            return model != null ? model : "default";
        }).orElse(this.skinType);
    }

    @Inject(method = "loadPlayerTextures",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/SkinManager;loadProfileTextures("
                            + "Lcom/mojang/authlib/GameProfile;"
                            + "Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;"
                            + "Z)V",
                    shift = At.Shift.BEFORE))
    private void onLoadTexture(CallbackInfo ci) {
        HDSkinManager.INSTANCE.loadProfileTextures(this.gameProfile)
                .thenAcceptAsync(m -> m.forEach((type, profile) -> {
                    HDSkinManager.INSTANCE.loadTexture(type, profile, (typeIn, location, profileTexture) -> {
                        customTextures.put(type, location);
                        customProfiles.put(type, profileTexture);
                    });
                }), Minecraft.getMinecraft()::addScheduledTask);
    }

    @Override
    public Optional<ResourceLocation> getResourceLocation(Type type) {
        return Optional.ofNullable(this.customTextures.get(type));
    }

    @Override
    public Optional<MinecraftProfileTexture> getProfileTexture(Type type) {
        return Optional.ofNullable(this.customProfiles.get(type));
    }

    @Override
    public void deleteTextures() {
        TextureManager tm = Minecraft.getMinecraft().getTextureManager();
        Stream.concat(this.customTextures.values().stream(), this.playerTextures.values().stream())
                .forEach(tm::deleteTexture);
        this.customTextures.clear();
        this.customProfiles.clear();
        this.playerTextures.clear();

        this.skinType = null;

        this.playerTexturesLoaded = false;
    }
}
