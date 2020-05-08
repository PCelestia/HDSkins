package com.minelittlepony.hdskins.common.gui.element;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.IGuiListener;
import com.minelittlepony.hdskins.common.gui.IRender;

public abstract class CustomElement implements IGuiListener, IRender {

    public void render(int mouseX, int mouseY, float partial, IGuiHelper gui) {}
}
