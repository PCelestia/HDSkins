package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.IRender;
import net.minecraft.client.gui.IRenderable;

public interface IRenderAdapter extends IRender, IRenderable {

    @Override
    default void render(int mouseX, int mouseY, float partial, IGuiHelper gui) {
        render(mouseX, mouseY, partial);
    }
}
