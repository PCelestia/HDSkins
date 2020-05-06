package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.PathList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class MCPPathList extends ExtendedList<MCPPathList.PathEntry> implements PathList {

    public MCPPathList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, 12);
        this.centerListVertically = false;
    }

    @Override
    public void clear() {
        this.clearEntries();
        this.setScrollAmount(0);
    }

    @Override
    public void addPath(Path path, Consumer<Path> callback) {
        this.addEntry(new PathEntry(path, callback));
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
        return this.x1 - 6;
    }

    public class PathEntry extends ExtendedList.AbstractListEntry<PathEntry> {

        private final Path path;
        private final String display;
        private final Consumer<Path> callback;

        public PathEntry(Path path, Consumer<Path> callback) {
            this.path = path;
            this.callback = callback;
            String text = this.path.getFileName().toString();
            if (Files.isDirectory(path)) {
                text += File.separator;
            }
            this.display = text;
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            callback.accept(path);
            return true;
        }

        @Override
        public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            String text = display;
            int l = minecraft.fontRenderer.getStringWidth(text);
            if (l > 157 && !hovered) {
                text = minecraft.fontRenderer.trimStringToWidth(text, 196 - minecraft.fontRenderer.getStringWidth("...")) + "...";
            }

            minecraft.fontRenderer.drawString(text, left, top + 1, hovered ? 0x999999 : -1);
        }
    }
}
