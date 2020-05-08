package com.minelittlepony.hdskins.common.gui;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface PathList extends IRender {
    void clear();

    void addPath(Path path, String display, Consumer<Path> callback);

    void setLeft(int left);
}
