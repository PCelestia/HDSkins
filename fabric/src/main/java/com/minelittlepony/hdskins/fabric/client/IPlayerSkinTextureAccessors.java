package com.minelittlepony.hdskins.fabric.client;

import net.minecraft.util.Identifier;

import java.io.File;

public interface IPlayerSkinTextureAccessors {
    File getCacheFile();

    String getUrl();

    Identifier getLocation();

    boolean isConvertLegacy();

    Runnable getLoadedCallback();
}
