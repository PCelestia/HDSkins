package com.minelittlepony.hdskins.fabric.mixin.client;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PlayerListEntry.class)
public interface IMixinPlayerListEntry {
    @Accessor
    Map<Type, Identifier> getTextures();

    @Accessor
    boolean isTexturesLoaded();

    @Accessor
    String getModel();

    @Accessor
    void setModel(String model);

}
