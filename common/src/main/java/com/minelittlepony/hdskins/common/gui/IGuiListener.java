package com.minelittlepony.hdskins.common.gui;

public interface IGuiListener {
    default void mouseMoved(double xPos, double mouseY) {
    }

    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        return false;
    }

    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(char codepoint, int modifiers) {
        return false;
    }

    default boolean changeFocus(boolean focused) {
        return false;
    }

    default boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }
}
