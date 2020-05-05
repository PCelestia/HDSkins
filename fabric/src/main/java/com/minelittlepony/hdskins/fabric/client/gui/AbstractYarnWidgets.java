package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.IButton;
import com.minelittlepony.hdskins.common.gui.ILabel;
import com.minelittlepony.hdskins.common.gui.ITextField;
import com.minelittlepony.hdskins.common.gui.Widgets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.arguments.ItemStackArgumentType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractYarnWidgets implements Drawable, Widgets {

    private final List<Drawable> draws = new ArrayList<>();

    protected abstract TextRenderer getTextRenderer();

    protected abstract <B extends AbstractButtonWidget> B addButton(B button);

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        draws.forEach(d -> d.render(mouseX, mouseY, delta));
    }

    protected <T extends Drawable> T addDraw(T draw) {
        draws.add(draw);
        return draw;
    }

    @Override
    public ILabel addLabel(int x, int y, String text, int color, boolean shadow, boolean centered) {
        return addDraw(new YarnLabel(getTextRenderer(), x, y, text, color, shadow, centered));
    }

    @Override
    public IButton addButton(int x, int y, int w, int h, String text, @Nullable String tooltip, Consumer<IButton> action) {
        return addButton(new YarnButton(x, y, w, h, text, tooltip, action));
    }

    @Override
    public IButton addButtonIcon(int x, int y, String item, String tooltip, Consumer<IButton> action) {
        try {
            ItemStack itemStack = ItemStackArgumentType.itemStack().parse(new StringReader(item)).createStack(1, false);
            return addButton(new YarnButtonIcon(x, y, itemStack, tooltip, action));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ITextField addTextField(int x, int y, int w, int h, String text) {
        return new YarnTextField(addButton(new TextFieldWidget(getTextRenderer(), x, y, w, h, text)));
    }
}
