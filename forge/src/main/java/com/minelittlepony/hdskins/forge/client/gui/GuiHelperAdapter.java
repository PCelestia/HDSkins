package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import net.minecraft.client.gui.AbstractGui;

public class GuiHelperAdapter implements IGuiHelper {

    private final AbstractGui gui;

    public GuiHelperAdapter(AbstractGui gui) {
        this.gui = gui;
    }

    protected AbstractGui getGui() {
        return gui;
    }

    @Override
    public void fill(int x1, int y1, int x2, int y2, int color) {
        AbstractGui.fill(x1, y1, x2, y2, color);
    }
}
