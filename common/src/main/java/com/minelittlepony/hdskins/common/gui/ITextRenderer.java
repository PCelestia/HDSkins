package com.minelittlepony.hdskins.common.gui;

public interface ITextRenderer {

    void draw(String text, int x, int y, int color);

    void drawWithShadow(String text, int x, int y, int color);

    int getStringWidth(String text);
}
