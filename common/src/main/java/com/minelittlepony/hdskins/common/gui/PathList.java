package com.minelittlepony.hdskins.common.gui;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface PathList {
    void clear();

    void addPath(Path path, Consumer<Path> callback);

    void setLeft(int left);

    void render(int mouseX, int mouseY, float delta);
}
