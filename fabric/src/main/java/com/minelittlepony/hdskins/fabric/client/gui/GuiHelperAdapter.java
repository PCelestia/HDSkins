package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import net.minecraft.client.gui.DrawableHelper;

public class GuiHelperAdapter implements IGuiHelper {

    private final DrawableHelper gui;

    public GuiHelperAdapter(DrawableHelper gui) {
        this.gui = gui;
    }

    protected DrawableHelper getGui() {
        return gui;
    }

    @Override
    public void fill(int x1, int y1, int x2, int y2, int color) {
        DrawableHelper.fill(x1, y1, x2, y2, color);
    }
}
