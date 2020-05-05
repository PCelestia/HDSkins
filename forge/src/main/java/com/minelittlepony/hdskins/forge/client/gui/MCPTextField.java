package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.ITextField;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class MCPTextField implements ITextField {
    private final TextFieldWidget textField;

    public MCPTextField(TextFieldWidget textField) {
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
        textField.setMaxStringLength(len);
    }
}
