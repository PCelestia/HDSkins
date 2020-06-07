package com.minelittlepony.hdskins.fabric.client.entity;

import com.google.common.base.MoreObjects;
import com.minelittlepony.hdskins.common.IHDSkins;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("EntityConstructor")
public class DummyPlayer extends AbstractClientPlayerEntity {

    private static final Identifier NO_SKIN = new Identifier(IHDSkins.MOD_ID, "textures/mob/noskin.png");
    private static final Identifier NO_SKIN_ALEX = new Identifier(IHDSkins.MOD_ID, "textures/mob/noskin.png");

    private final Map<MinecraftProfileTexture.Type, Identifier> textures = new HashMap<>();
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
    protected PlayerListEntry getPlayerListEntry() {
        return null;
    }

    @Override
    public boolean hasSkinTexture() {
        return true;
    }

    @Override
    public Identifier getSkinTexture() {
        return textures.getOrDefault(MinecraftProfileTexture.Type.SKIN, getDefaultSkin());
    }

    @Override
    @Nullable
    public String getModel() {
        return MoreObjects.firstNonNull(model, this.getDefaultModel());
    }

    @Nullable
    @Override
    public Identifier getCapeTexture() {
        return textures.get(MinecraftProfileTexture.Type.CAPE);
    }

    @Nullable
    @Override
    public Identifier getElytraTexture() {
        return textures.get(MinecraftProfileTexture.Type.ELYTRA);
    }

    public void setTexture(MinecraftProfileTexture.Type skinType, Identifier location) {
        this.textures.put(skinType, location);
    }

    private static ClientWorld makeDummyWorld() {
        MinecraftClient mc = MinecraftClient.getInstance();
        LevelInfo settings = new LevelInfo(0, GameMode.NOT_SET, false, false, LevelGeneratorType.DEFAULT);
        return new ClientWorld(null, settings, DimensionType.OVERWORLD, 0, null, mc.worldRenderer);
    }

    public void resetSkins() {
        this.textures.clear();
        this.model = null;
    }

    public void loadSkins(MinecraftProfileTexture.Type type, Identifier location, MinecraftProfileTexture texture) {
        this.textures.put(type, location);
        if (type == MinecraftProfileTexture.Type.SKIN) {
            model = texture.getMetadata("model");
        }
    }

    private Identifier getDefaultSkin() {
        return isSlimSkin(this.getUuid()) ? NO_SKIN_ALEX : NO_SKIN;
    }

    private String getDefaultModel() {
        return DefaultSkinHelper.getModel(this.getUuid());
    }

    private static boolean isSlimSkin(UUID playerUUID) {
        return (playerUUID.hashCode() & 1) == 1;
    }
}
