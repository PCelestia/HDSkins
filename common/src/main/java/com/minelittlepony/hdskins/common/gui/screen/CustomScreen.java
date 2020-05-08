package com.minelittlepony.hdskins.common.gui.screen;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.IRender;
import com.minelittlepony.hdskins.common.gui.IScreen;
import com.minelittlepony.hdskins.common.gui.Widgets;

public abstract class CustomScreen implements IRender {

    private final String title;
    protected IScreen screen;

    protected CustomScreen(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void render(int mouseX, int mouseY, float delta, IGuiHelper gui) { }

    protected void init(Widgets factory) {}

    public void tick() {}

    public void removed() {}

    @Deprecated
    public final void init(IScreen screen, Widgets factory) {
        this.screen = screen;
        init(factory);
    }

}
