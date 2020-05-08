package com.minelittlepony.hdskins.fabric.client.gui.elements;

import com.minelittlepony.hdskins.common.gui.ITextField;
import com.minelittlepony.hdskins.fabric.client.gui.IRenderAdapter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class YarnTextField extends TextFieldWidget implements ITextField, IRenderAdapter {

    private Consumer<String> callback;

    public YarnTextField(TextRenderer textRenderer, int x, int y, int width, int height, String message) {
        super(textRenderer, x, y, width, height, message);
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
        this.setMaxLength(len);
    }

    @Override
    public void setCallback(Consumer<String> callback) {
        this.callback = callback;
    }

    @Override
    public void setScroll(int scr) {
        this.setCursor(scr);
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
