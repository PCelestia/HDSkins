package com.minelittlepony.hdskins.fabric.client;

import net.minecraft.client.texture.NativeImage;

import javax.annotation.Nullable;
import java.io.InputStream;

public interface IPlayerSkinTexture {
    @Nullable
    NativeImage loadTextureOverride(InputStream inputStreamIn);
}
