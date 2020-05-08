package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.ITextRenderer;
import net.minecraft.client.font.TextRenderer;

public class YarnTextAdapter implements ITextRenderer {

    private TextRenderer renderer;

    public YarnTextAdapter(TextRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void draw(String text, int x, int y, int color) {
        renderer.draw(text, x, y, color);
    }

    @Override
    public void drawWithShadow(String text, int x, int y, int color) {
        renderer.drawWithShadow(text, x, y, color);
    }

    @Override
    public int getStringWidth(String text) {
        return renderer.getStringWidth(text);
    }
}
