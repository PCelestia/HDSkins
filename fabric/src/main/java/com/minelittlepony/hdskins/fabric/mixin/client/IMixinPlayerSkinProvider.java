package com.minelittlepony.hdskins.fabric.mixin.client;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PlayerSkinProvider.class)
public interface IMixinPlayerSkinProvider {

    @Accessor
    LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> getSkinCache();

    @Accessor
    void setSkinCache(LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> cache);
}
