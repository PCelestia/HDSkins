package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.IScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;

public abstract class MCPScreenAdapter extends GuiHelperAdapter implements IScreen {
    public MCPScreenAdapter(Screen gui) {
        super(gui);
    }

    @Override
    protected Screen getGui() {
        return (Screen) super.getGui();
    }

    @Override
    public int getWidth() {
        return getGui().width;
    }

    @Override
    public int getHeight() {
        return getGui().height;
    }

    @Override
    public void close() {
        getGui().onClose();
    }

    @Override
    public String translate(String key, Object... args) {
        return I18n.format(key, args);
    }
}
