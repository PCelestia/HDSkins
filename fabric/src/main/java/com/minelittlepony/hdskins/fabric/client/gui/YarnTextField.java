package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.ITextField;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class YarnTextField implements ITextField {
    private final TextFieldWidget textField;

    public YarnTextField(TextFieldWidget textField) {
        this.textField = textField;
    }

    @Override
    public String getText() {
        return textField.getText();
    }

    @Override
    public void setText(String text) {
        textField.setText(text);
    }

    @Override
    public void setMaxLength(int len) {
        textField.setMaxLength(len);
    }
}
