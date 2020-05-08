package com.minelittlepony.hdskins.fabric.client.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;

import javax.annotation.Nullable;

@SuppressWarnings("EntityConstructor")
public class DummyPlayer extends AbstractClientPlayerEntity {

    public DummyPlayer(GameProfile p_i50991_2_) {
        super(makeDummyWorld(), p_i50991_2_);
    }

    public boolean isSpectator() {
        return false;
    }

    public boolean isCreative() {
        return false;
    }

    @Nullable
    @Override
    protected PlayerListEntry getPlayerListEntry() {
        return null;
    }

    private static ClientWorld makeDummyWorld() {
        MinecraftClient mc = MinecraftClient.getInstance();
        LevelInfo settings = new LevelInfo(0, GameMode.NOT_SET, false, false, LevelGeneratorType.DEFAULT);
        return new ClientWorld(null, settings, DimensionType.OVERWORLD, 0, null, mc.worldRenderer);
    }
}
