package com.minelittlepony.hdskins.forge.client.gui.widgets;

import com.minelittlepony.hdskins.common.gui.IButton;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MCPButtonIcon extends MCPButton {
    private final ItemStack item;

    public MCPButtonIcon(int x, int y, ItemStack item, @Nullable String tooltip, Consumer<IButton> callback) {
        super(x, y, 20, 20, "", tooltip, callback);
        this.item = item;
    }

    @Override
    protected void renderBg(Minecraft client, int mouseX, int mouseY) {
        client.getItemRenderer().renderItemIntoGUI(item, x + 2, y + 2);
    }
}
