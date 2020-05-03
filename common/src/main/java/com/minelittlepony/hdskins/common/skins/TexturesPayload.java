package com.minelittlepony.hdskins.common.skins;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;

import java.util.Map;
import java.util.UUID;

public class TexturesPayload extends MinecraftTexturesPayload {
    private final long timestamp;
    private final UUID profileId;
    private final String profileName;
    private final boolean isPublic;
    private final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures;

    public TexturesPayload(long timestamp, UUID profileId, String profileName, boolean isPublic, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {
        this.timestamp = timestamp;
        this.profileId = profileId;
        this.profileName = profileName;
        this.isPublic = isPublic;
        this.textures = textures;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public UUID getProfileId() {
        return profileId;
    }

    @Override
    public String getProfileName() {
        return profileName;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures() {
        return textures;
    }
}
