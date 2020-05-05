package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.PathList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

import java.nio.file.Path;

public class YarnPathList extends AlwaysSelectedEntryListWidget<YarnPathList.PathEntry> implements PathList {

    public YarnPathList(MinecraftClient mcIn, int widthIn, int heightIn, int topIn, int bottomIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, mcIn.textRenderer.fontHeight + 3);
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

    public class PathEntry extends AlwaysSelectedEntryListWidget.Entry<PathEntry> {

        private final Path path;

        public PathEntry(Path path) {
            this.path = path;
        }

        @Override
        public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            String text = this.path.toString();
            float center = (left + width) / 2f - minecraft.textRenderer.getStringWidth(text) / 2f;
            MinecraftClient.getInstance().textRenderer.draw(this.path.toString(), center, top, hovered ? 0 : -1);
        }
    }
}
