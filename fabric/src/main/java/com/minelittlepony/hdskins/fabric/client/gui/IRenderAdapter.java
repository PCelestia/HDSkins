package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.IRender;
import net.minecraft.client.gui.Drawable;

public interface IRenderAdapter extends IRender, Drawable {
    @Override
    default void render(int mouseX, int mouseY, float partial, IGuiHelper gui) {
        render(mouseX, mouseY, partial);
    }
}
