package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.IButton;
import com.minelittlepony.hdskins.common.gui.ILabel;
import com.minelittlepony.hdskins.common.gui.ITextField;
import com.minelittlepony.hdskins.common.gui.PathList;
import com.minelittlepony.hdskins.common.gui.Widgets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractMCPWidgets implements IRenderable, Widgets {

    private final List<IRenderable> draws = new ArrayList<>();

    protected abstract FontRenderer getTextRenderer();

    protected abstract <B extends Widget> B addButton(B button);

    protected abstract List<IGuiEventListener> children();

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        draws.forEach(d -> d.render(mouseX, mouseY, delta));
    }

    protected <T extends IRenderable> T addDraw(T draw) {
        draws.add(draw);
        return draw;
    }

    @Override
    public ILabel addLabel(int x, int y, String text, int color, boolean shadow, boolean centered) {
        return addDraw(new MCPLabel(getTextRenderer(), x, y, text, color, shadow, centered));
    }

    @Override
    public IButton addButton(int x, int y, int w, int h, String text, @Nullable String tooltip, Consumer<IButton> action) {
        return addButton(new MCPButton(x, y, w, h, text, tooltip, action));
    }

    @Override
    public IButton addButtonIcon(int x, int y, String item, String tooltip, Consumer<IButton> action) {
        try {
            ItemStack itemStack = ItemArgument.item().parse(new StringReader(item)).createStack(1, false);
            return addButton(new MCPButtonIcon(x, y, itemStack, tooltip, action));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ITextField addTextField(int x, int y, int w, int h, String text) {
        return new MCPTextField(addButton(new TextFieldWidget(getTextRenderer(), x, y, w, h, text)));
    }

    @Override
    public PathList addPathList(int widthIn, int heightIn, int topIn, int bottomIn) {
        MCPPathList list = new MCPPathList(Minecraft.getInstance(), widthIn, heightIn, topIn, bottomIn);
        children().add(list);
        return list;
    }
}
