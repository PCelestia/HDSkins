package com.minelittlepony.hdskins.common.gui;

public interface IScreen extends IGuiHelper {

    ITextRenderer getTextRenderer();

    int getWidth();

    int getHeight();

    void close();

    String translate(String key, Object... args);
}
