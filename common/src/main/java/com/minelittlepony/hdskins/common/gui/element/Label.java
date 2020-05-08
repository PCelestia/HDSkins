package com.minelittlepony.hdskins.common.gui.element;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.IRender;
import com.minelittlepony.hdskins.common.gui.ITextRenderer;

public class Label extends CustomElement implements IRender {

    private final ITextRenderer font;
    private final int x;
    private final int y;
    private String text;
    private final int color;
    private final boolean shadow;
    private final boolean centered;

    public Label(ITextRenderer font, int x, int y, String text, int color, boolean shadow, boolean centered) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.shadow = shadow;
        this.centered = centered;
    }

    public Label(ITextRenderer font, int x, int y, String text) {
        this(font, x, y, text, -1, false, false);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void render(int mouseX, int mouseY, float partial, IGuiHelper gui) {
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
