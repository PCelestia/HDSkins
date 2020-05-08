package com.minelittlepony.hdskins.common.gui.element;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.AbstractPlayerEntityRenderer;
import com.minelittlepony.hdskins.common.gui.IRender;
import org.lwjgl.glfw.GLFW;

public class PlayerModelElement extends CustomElement implements IRender {
    private final int xPos;
    private final int yPos;
    private final int width;
    private final int height;
    private final AbstractPlayerEntityRenderer player;

    private int prevMouseX;
    private int prevMouseY;

    private boolean clicked;

    public PlayerModelElement(int xPos, int yPos, int width, int height, AbstractPlayerEntityRenderer player) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.player = player;
    }

    @Override
    public void render(int mouseX, int mouseY, float ticks, IGuiHelper gui) {
        gui.fill(xPos, yPos, xPos + width, yPos + height, 0xff000000);

        if (clicked) {
            player.rotation += prevMouseX - mouseX;

            player.pitch += prevMouseY - mouseY;
            player.pitch = Math.max(player.pitch, -60);
            player.pitch = Math.min(player.pitch, 60);
        } else {
            player.rotation += ticks * 2;
//            player.pitch = 0;
        }

        if (player.rotation < 0) {
            player.rotation += 360;
        }

        if (player.rotation > 360) {
            player.rotation -= 360;
        }

        int renderX = xPos + width / 2;
        int renderY = yPos + height / 2 + 50;

        player.renderStatic(renderX, renderY, 50, -(mouseX - renderX), -(mouseY - renderY));

        prevMouseX = mouseX;
        prevMouseY = mouseY;

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.clicked = true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.clicked && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.clicked = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return clicked
                || this.xPos < mouseX && this.xPos + width > mouseX
                && this.yPos < mouseY && this.yPos + height > mouseY;
    }
}
