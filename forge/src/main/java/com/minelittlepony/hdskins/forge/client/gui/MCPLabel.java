package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.ILabel;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.resources.I18n;

public class MCPLabel implements ILabel, IRenderable {

    private final FontRenderer font;
    private final int x;
    private final int y;
    private String text;
    private final int color;
    private final boolean shadow;
    private final boolean centered;

    public MCPLabel(FontRenderer font, int x, int y, String text, int color, boolean shadow, boolean centered) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.text = I18n.format(text);
        this.color = color;
        this.shadow = shadow;
        this.centered = centered;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        int xPos = x;
        if (this.centered) {
            xPos -= font.getStringWidth(text) / 2;
        }
        if (shadow) {
            font.drawStringWithShadow(text, xPos, y, color);
        } else {
            font.drawString(text, xPos, y, color);
        }
    }
}
