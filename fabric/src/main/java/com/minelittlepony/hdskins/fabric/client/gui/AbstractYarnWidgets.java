package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.IButton;
import com.minelittlepony.hdskins.common.gui.ITextField;
import com.minelittlepony.hdskins.common.gui.PathList;
import com.minelittlepony.hdskins.common.gui.Widgets;
import com.minelittlepony.hdskins.common.gui.element.PlayerModelElement;
import com.minelittlepony.hdskins.fabric.client.entity.DummyPlayer;
import com.minelittlepony.hdskins.fabric.client.gui.elements.YarnButton;
import com.minelittlepony.hdskins.fabric.client.gui.elements.YarnButtonIcon;
import com.minelittlepony.hdskins.fabric.client.gui.elements.YarnPathList;
import com.minelittlepony.hdskins.fabric.client.gui.elements.YarnTextField;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.arguments.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractYarnWidgets implements Widgets {

    protected abstract TextRenderer getTextRenderer();

    protected abstract <B extends AbstractButtonWidget> B addButton(B button);

    protected abstract List<Element> children();

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
        return addButton(new YarnTextField(getTextRenderer(), x, y, w, h, text));
    }

    @Override
    public PathList addPathList(int widthIn, int heightIn, int topIn, int bottomIn) {
        YarnPathList list = new YarnPathList(MinecraftClient.getInstance(), widthIn, heightIn, topIn, bottomIn);
        children().add(list);
        return list;
    }
    @Override
    public PlayerModelElement addEntity(int x, int y, int w, int h) {
        MinecraftClient mc = MinecraftClient.getInstance();
        LevelInfo settings = new LevelInfo(0, GameMode.NOT_SET, false, false, LevelGeneratorType.DEFAULT);
        ClientWorld world = new ClientWorld(null, settings, DimensionType.OVERWORLD, 0, null, mc.worldRenderer);
        GuiPlayerEntityRenderer widget = new GuiPlayerEntityRenderer(new DummyPlayer(world, mc.getSession().getProfile()));
        PlayerModelElement player = new PlayerModelElement(x, y, w, h, widget);
        children().add(new GuiListenerAdapter(player));
        return player;
    }
}
