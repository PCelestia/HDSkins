package com.minelittlepony.hdskins.fabric.client.gui.elements;

import com.minelittlepony.hdskins.common.gui.IButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class YarnButtonIcon extends YarnButton {
    private final ItemStack item;

    public YarnButtonIcon(int x, int y, ItemStack item, @Nullable String tooltip, Consumer<IButton> callback) {
        super(x, y, 20, 20, "", tooltip, callback);
        this.item = item;
    }

    @Override
    protected void renderBg(MinecraftClient client, int mouseX, int mouseY) {
        client.getItemRenderer().renderGuiItem(item, x + 2, y + 2);
    }
}
