package com.minelittlepony.hdskins.forge.client.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;

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
    protected NetworkPlayerInfo getPlayerInfo() {
        return null;
    }

    private static ClientWorld makeDummyWorld() {
        Minecraft mc = Minecraft.getInstance();
        WorldSettings settings = new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.DEFAULT);
        return new ClientWorld(null, settings, DimensionType.OVERWORLD, 0, null, mc.worldRenderer);
    }
}
