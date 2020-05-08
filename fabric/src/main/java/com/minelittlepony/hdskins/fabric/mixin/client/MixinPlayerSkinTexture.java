package com.minelittlepony.hdskins.fabric.mixin.client;

import com.minelittlepony.hdskins.fabric.client.IPlayerSkinTexture;
import com.minelittlepony.hdskins.fabric.client.IPlayerSkinTextureAccessors;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;

@Mixin(PlayerSkinTexture.class)
public abstract class MixinPlayerSkinTexture extends ResourceTexture implements IPlayerSkinTexture, IPlayerSkinTextureAccessors {

    public MixinPlayerSkinTexture(Identifier location) {
        super(location);
    }

    //@formatter:off
    @Accessor @Override public abstract File getCacheFile();
    @Accessor @Override public abstract String getUrl();
    @Accessor @Override public abstract boolean isConvertLegacy();
    @Accessor @Override public abstract Runnable getLoadedCallback();
    //@formatter:on

    @Override
    public Identifier getLocation() {
        return this.location;
    }

    @Shadow
    @Nullable
    protected abstract NativeImage loadTexture(InputStream stream);

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/PlayerSkinTexture;loadTexture(Ljava/io/InputStream;)Lnet/minecraft/client/texture/NativeImage;"))
    private NativeImage foo(PlayerSkinTexture playerSkinTexture, InputStream stream) {
        return this.loadTextureOverride(stream);
    }

    @Override
    public NativeImage loadTextureOverride(InputStream stream) {
        return this.loadTexture(stream);
    }
}
