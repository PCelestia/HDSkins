package com.minelittlepony.hdskins.fabric.client.gui.elements;

import com.minelittlepony.hdskins.common.gui.IButton;
import com.minelittlepony.hdskins.fabric.client.gui.IRenderAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.resource.language.I18n;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class YarnButton extends AbstractPressableButtonWidget implements IButton, IRenderAdapter {

    @Nullable
    private final String tooltip;
    private final Consumer<IButton> callback;

    public YarnButton(int i, int j, int k, int l, String string, @Nullable String tooltip, Consumer<IButton> callback) {
        super(i, j, k, l, I18n.translate(string));
        if (tooltip != null) {
            this.tooltip = I18n.translate(tooltip);
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
            MinecraftClient.getInstance().currentScreen.renderTooltip(tooltip, mouseX, mouseY + 10);
        }
    }
}
