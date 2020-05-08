package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.IScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

public abstract class YarnScreenAdapter extends GuiHelperAdapter implements IScreen {
    public YarnScreenAdapter(Screen gui) {
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
        return I18n.translate(key, args);
    }
}
