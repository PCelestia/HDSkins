package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.PathList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

import java.nio.file.Path;
import java.util.function.Consumer;

public class YarnPathList extends AlwaysSelectedEntryListWidget<YarnPathList.PathEntry> implements PathList {

    public YarnPathList(MinecraftClient mcIn, int widthIn, int heightIn, int topIn, int bottomIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, 12);
        this.centerListVertically = false;
    }

    @Override
    public void clear() {
        this.clearEntries();
        this.setScrollAmount(0);
    }

    @Override
    public void addPath(Path path, String display, Consumer<Path> callback) {
        this.addEntry(new PathEntry(path, display, callback));
    }

    @Override
    public void setLeft(int left) {
        setLeftPos(left);
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    protected int getScrollbarPosition() {
        return this.right - 6;
    }

    public class PathEntry extends AlwaysSelectedEntryListWidget.Entry<PathEntry> {

        private final Path path;
        private final String display;
        private final Consumer<Path> callback;

        public PathEntry(Path path, String display, Consumer<Path> callback) {
            this.path = path;
            this.display = display;
            this.callback = callback;
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            callback.accept(path);
            return true;
        }

        @Override
        public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            String text = display;
            int l = minecraft.textRenderer.getStringWidth(text);
            if (l > 157 && !hovered) {
                text = minecraft.textRenderer.trimToWidth(text, 196 - minecraft.textRenderer.getStringWidth("...")) + "...";
            }

            minecraft.textRenderer.draw(text, left, top + 1, hovered ? 0x999999 : -1);}
    }
}
