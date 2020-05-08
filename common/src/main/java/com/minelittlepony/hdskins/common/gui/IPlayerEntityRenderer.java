package com.minelittlepony.hdskins.common.gui;

public abstract class IPlayerEntityRenderer {

    public float rotation;
    public float pitch;

    public abstract void renderStatic(int posX, int posY, int scale, float mouseX, float mouseY);
}
