package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.ILabel;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.resource.language.I18n;

public class YarnLabel implements ILabel, Drawable {

    private final TextRenderer font;
    private final int x;
    private final int y;
    private String text;
    private final int color;
    private final boolean shadow;
    private final boolean centered;

    public YarnLabel(TextRenderer font, int x, int y, String text, int color, boolean shadow, boolean centered) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.text = I18n.translate(text);
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
            font.drawWithShadow(text, xPos, y, color);
        } else {
            font.draw(text, xPos, y, color);
        }
    }
}
