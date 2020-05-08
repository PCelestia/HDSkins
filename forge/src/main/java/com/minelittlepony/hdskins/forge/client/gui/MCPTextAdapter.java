package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.ITextRenderer;
import net.minecraft.client.gui.FontRenderer;

public class MCPTextAdapter implements ITextRenderer {

    private FontRenderer renderer;

    public MCPTextAdapter(FontRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void draw(String text, int x, int y, int color) {
        renderer.drawString(text, x, y, color);
    }

    @Override
    public void drawWithShadow(String text, int x, int y, int color) {
        renderer.drawStringWithShadow(text, x, y, color);
    }

    @Override
    public int getStringWidth(String text) {
        return renderer.getStringWidth(text);
    }
}
