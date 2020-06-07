package com.minelittlepony.hdskins.common.gui;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Map;

public abstract class AbstractPlayerEntityRenderer implements Closeable {

    public float rotation;
    public float pitch;

    public abstract void renderStatic(int posX, int posY, int scale, float mouseX, float mouseY);

    public abstract void loadSkins(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures);

    public abstract void loadTexture(MinecraftProfileTexture.Type skinType, Path path);

    @Override
    public abstract void close();
}
