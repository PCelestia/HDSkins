package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.ITextField;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class MCPTextField extends TextFieldWidget implements ITextField {

    private Consumer<String> callback;

    public MCPTextField(FontRenderer fontIn, int xIn, int yIn, int widthIn, int heightIn, String msg) {
        super(fontIn, xIn, yIn, widthIn, heightIn, msg);
    }

    @Override
    public String getContent() {
        return this.getText();
    }

    @Override
    public void setContent(String text) {
        this.setText(text);
    }

    @Override
    public void setMaxContentLength(int len) {
        this.setMaxStringLength(len);
    }

    @Override
    public void setCallback(Consumer<String> callback) {
        this.callback = callback;
    }

    @Override
    public void setScroll(int scr) {
        this.setCursorPosition(scr);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (super.keyPressed(key, scanCode, modifiers)) {
            return true;
        }
        switch(key) {
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_KP_ENTER:
                if (callback != null) {
                    callback.accept(this.getText());
                    return true;
                }
        }
        return false;
    }
}
