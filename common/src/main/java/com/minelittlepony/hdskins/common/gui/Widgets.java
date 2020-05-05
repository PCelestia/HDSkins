package com.minelittlepony.hdskins.common.gui;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface Widgets {

    ILabel addLabel(int x, int y, String text, int color, boolean shadow, boolean centered);

    default ILabel addLabel(int x, int y, String text, int color, boolean shadow) {
        return addLabel(x, y, text, color, shadow, false);
    }

    default ILabel addLabel(int x, int y, String text, int color) {
        return addLabel(x, y, text, color, false);
    }

    default ILabel addLabel(int x, int y, String text) {
        return addLabel(x, y, text, -1);
    }

    IButton addButton(int x, int y, int w, int h, String text, @Nullable String tooltip, Consumer<IButton> action);

    default IButton addButton(int x, int y, int w, int h, String text, Consumer<IButton> action) {
        return addButton(x, y, w, h, text, null, action);
    }

    IButton addButtonIcon(int x, int y, String item, @Nullable String tooltip, Consumer<IButton> action);

    default IButton addButtonIcon(int x, int y, String item, Consumer<IButton> action) {
        return addButtonIcon(x, y, item, null, action);
    }

    ITextField addTextField(int x, int y, int w, int h, String text);

    PathList addPathList(int widthIn, int heightIn, int topIn, int bottomIn);
}
