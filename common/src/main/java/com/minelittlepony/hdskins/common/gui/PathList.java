package com.minelittlepony.hdskins.common.gui;

import java.nio.file.Path;

public interface PathList {
    void clear();

    void addPath(Path path);

    void setLeft(int left);

    void render(int mouseX, int mouseX1, float delta);
}
