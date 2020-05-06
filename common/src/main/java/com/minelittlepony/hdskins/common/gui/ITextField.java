package com.minelittlepony.hdskins.common.gui;

import java.util.function.Consumer;

public interface ITextField {
    String getContent();

    void setContent(String text);

    void setMaxContentLength(int len);

    void setCallback(Consumer<String> callback);

    void setScroll(int scr);
}
