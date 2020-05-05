package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.gui.PathList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.nio.file.Path;

public class MCPPathList extends ExtendedList<MCPPathList.PathEntry> implements PathList {

    public MCPPathList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, mcIn.fontRenderer.FONT_HEIGHT + 3);
    }

    @Override
    public void clear() {
        this.clearEntries();
    }

    @Override
    public void addPath(Path path) {
        this.addEntry(new PathEntry(path));
    }

    @Override
    public void setLeft(int left) {
        setLeftPos(left);
    }

    public class PathEntry extends ExtendedList.AbstractListEntry<PathEntry> {

        private final Path path;

        public PathEntry(Path path) {
            this.path = path;
        }

        @Override
        public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            String text = this.path.toString();
            float center = (left + width) / 2f - minecraft.fontRenderer.getStringWidth(text) / 2f;
            minecraft.fontRenderer.drawString(this.path.toString(), center, top, hovered ? 0 : -1);
        }
    }
}
