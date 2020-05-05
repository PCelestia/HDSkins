package com.minelittlepony.hdskins.common.gui.screen;

import com.minelittlepony.hdskins.common.gui.IScreen;

public abstract class CustomScreen {

    private final String title;
    protected IScreen screen;

    protected CustomScreen(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void render(int mouseX, int mouseY, float delta) { }

    protected void init() {}

    public void tick() {}

    public void removed() {}

    public final void init(IScreen screen) {
        this.screen = screen;
        init();
    }

}
