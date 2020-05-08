package com.minelittlepony.hdskins.forge.client.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.world.ClientWorld;

import javax.annotation.Nullable;

@SuppressWarnings("EntityConstructor")
public class DummyPlayer extends AbstractClientPlayerEntity {
    public DummyPlayer(ClientWorld p_i50991_1_, GameProfile p_i50991_2_) {
        super(p_i50991_1_, p_i50991_2_);
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
}
