package com.minelittlepony.hdskins.forge.client.entity;

import com.google.common.base.MoreObjects;
import com.minelittlepony.hdskins.common.IHDSkins;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("EntityConstructor")
public class DummyPlayer extends AbstractClientPlayerEntity {

    private static final ResourceLocation NO_SKIN = new ResourceLocation(IHDSkins.MOD_ID, "textures/mob/noskin.png");
    private static final ResourceLocation NO_SKIN_ALEX = new ResourceLocation(IHDSkins.MOD_ID, "textures/mob/noskin.png");

    private final Map<MinecraftProfileTexture.Type, ResourceLocation> textures = new HashMap<>();
    @Nullable
    private String model;

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

    @Override
    public boolean hasSkin() {
        return true;
    }

    @Override
    public synchronized ResourceLocation getLocationSkin() {
        return textures.getOrDefault(MinecraftProfileTexture.Type.SKIN, getDefaultSkin());
    }

    @Override
    public String getSkinType() {
        return MoreObjects.firstNonNull(this.model, this.getDefaultModel());
    }

    @Nullable
    @Override
    public ResourceLocation getLocationCape() {
        return textures.get(MinecraftProfileTexture.Type.CAPE);
    }

    @Nullable
    @Override
    public ResourceLocation getLocationElytra() {
        return textures.get(MinecraftProfileTexture.Type.ELYTRA);
    }

    public void setTexture(MinecraftProfileTexture.Type skinType, ResourceLocation location) {
        this.textures.put(skinType, location);
    }

    private static ClientWorld makeDummyWorld() {
        Minecraft mc = Minecraft.getInstance();
        WorldSettings settings = new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.DEFAULT);
        return new ClientWorld(null, settings, DimensionType.OVERWORLD, 0, null, mc.worldRenderer);
    }

    public void resetSkins() {
        this.textures.clear();
        this.model = null;
    }

    public void loadSkins(MinecraftProfileTexture.Type type, ResourceLocation location, MinecraftProfileTexture texture) {
        this.textures.put(type, location);
        if (type == MinecraftProfileTexture.Type.SKIN) {
            model = texture.getMetadata("model");
        }
    }

    private ResourceLocation getDefaultSkin() {
        return isSlimSkin(this.getUniqueID()) ? NO_SKIN_ALEX : NO_SKIN;
    }

    private String getDefaultModel() {
        return DefaultPlayerSkin.getSkinType(this.getUniqueID());
    }

    private static boolean isSlimSkin(UUID playerUUID) {
        return (playerUUID.hashCode() & 1) == 1;
    }
}
