package com.minelittlepony.hdskins.common.upload;

import com.minelittlepony.hdskins.common.BackendServerException;
import com.minelittlepony.hdskins.common.skins.Session;
import com.minelittlepony.hdskins.common.skins.SkinRequest;
import com.minelittlepony.hdskins.common.skins.SkinServer;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Uploader {

    private static final String ERR_NO_SKIN = "hdskins.error.noskin";
    private static final String ERR_OFFLINE = "hdskins.error.offline";
    private static final String ERR_MOJANG = "hdskins.error.mojang";

    private SkinServer gateway;

    private Type skinType = Type.SKIN;
    private final Map<String, String> skinMetadata = new HashMap<>();
    @Nullable
    private URI localSkin;

    private final MinecraftSessionService sessionService;
    private final Session session;

    public Uploader(SkinServer gateway, Session session, MinecraftSessionService sessionService) {
        this.gateway = gateway;
        this.session = session;
        this.sessionService = sessionService;

        skinMetadata.put("model", "default");
    }

    public SkinServer getGateway() {
        return gateway;
    }

    public void setGateway(@Nullable SkinServer gateway) {
        this.gateway = gateway;
    }

    public void setSkinType(Type type) {
        skinType = type;
        skinMetadata.clear();
        if (type == Type.SKIN) {
            setMetadata("model", "default");
        }
    }

    public Type getSkinType() {
        return skinType;
    }

    public void setMetadata(String field, String value) {
        skinMetadata.put(field, value);
    }

    @Nullable
    public String getMetadata(String field) {
        return skinMetadata.get(field);
    }

    public void setLocalSkin(URI localSkin) {
        this.localSkin = localSkin;
    }

    @Nullable
    public URI getLocalSkin() {
        return localSkin;
    }

    public void uploadSkin() throws UploaderException {
        if (localSkin == null) {
            throw new UploaderException(ERR_NO_SKIN);
        }
        pushToServer(new SkinRequest.Upload(session, skinType, localSkin, skinMetadata));
    }

    public void clearSkin() throws UploaderException {
        pushToServer(new SkinRequest.Delete(session, skinType));
    }

    private void pushToServer(SkinRequest request) throws UploaderException {
        try {
            gateway.performSkinUpload(sessionService, request);
        } catch (IOException | AuthenticationException e) {
            handleException(e);
        }
    }

    private static void handleException(Throwable throwable) throws UploaderException {
        if (throwable instanceof AuthenticationUnavailableException) {
            throw new UploaderException(ERR_OFFLINE, throwable);
        }
        if (throwable instanceof AuthenticationException) {
            throw new UploaderException(ERR_MOJANG, throwable);
        }
        if (throwable instanceof BackendServerException) {
            throw new UploaderException("A fatal error has occurred.", throwable);
        }
        if (throwable instanceof IOException) {
            throw new UploaderException(throwable);
        }
        throw new UploaderException("Unhandled exception", throwable);
    }
}
