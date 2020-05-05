package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.IButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;

import net.minecraft.client.resources.I18n;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MCPButton extends AbstractButton implements IButton {

    @Nullable
    private final String tooltip;
    private final Consumer<IButton> callback;

    public MCPButton(int i, int j, int k, int l, String string, @Nullable String tooltip, Consumer<IButton> callback) {
        super(i, j, k, l, I18n.format(string));
        if (tooltip != null) {
            this.tooltip = I18n.format(tooltip);
        } else {
            this.tooltip = null;
        }
        this.callback = callback;
    }

    @Override
    public void setEnabled(boolean en) {
        active = en;
    }

    @Override
    public void onPress() {
        callback.accept(this);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        if (tooltip != null) {
            Minecraft.getInstance().currentScreen.renderTooltip(tooltip, mouseX, mouseY + 10);
        }
    }
}
