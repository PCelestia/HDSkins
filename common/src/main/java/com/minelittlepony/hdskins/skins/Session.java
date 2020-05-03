package com.minelittlepony.hdskins.skins;

import com.mojang.authlib.GameProfile;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Session {
    private final String accessToken;
    private final GameProfile gameProfile;

    public Session(String accessToken, GameProfile gameProfile) {
        this.accessToken = accessToken;
        this.gameProfile = gameProfile;
    }

    public GameProfile getProfile() {
        return gameProfile;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
