package com.minelittlepony.hdskins.forge.client.gui.screen;

import com.minelittlepony.hdskins.common.gui.IScreen;
import com.minelittlepony.hdskins.common.gui.screen.CustomScreen;
import com.minelittlepony.hdskins.forge.client.gui.AbstractMCPWidgets;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class MCPScreenWrapper extends Screen {

    protected SrgScreen screenHelper;
    protected final CustomScreen screen;

    public MCPScreenWrapper(CustomScreen screen) {
        super(new TranslationTextComponent(screen.getTitle()));
        this.screen = screen;
    }

    @Override
    protected void init() {
        screenHelper = new SrgScreen();
        screen.init(screenHelper);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();

        screen.render(mouseX, mouseY, delta);
        super.render(mouseX, mouseY, delta);
        screenHelper.render(mouseX, mouseY, delta);

        this.buttons.stream()
                .filter(Widget::isHovered)
                .forEach(b -> b.renderToolTip(mouseX, mouseY));
    }

    @Override
    public void tick() {
        screen.tick();
    }

    @Override
    public void removed() {
        screen.removed();
    }

    protected class SrgScreen extends AbstractMCPWidgets implements IScreen {

        @Override
        protected FontRenderer getTextRenderer() {
            return font;
        }

        @Override
        protected <B extends Widget> B addButton(B button) {
            return MCPScreenWrapper.this.addButton(button);
        }

        @Override
        protected List<IGuiEventListener> children() {
            return MCPScreenWrapper.this.children;
        }

        @Override
        public int getWidth() {
            return MCPScreenWrapper.this.width;
        }

        @Override
        public int getHeight() {
            return MCPScreenWrapper.this.height;
        }

        @Override
        public void close() {
            MCPScreenWrapper.this.onClose();
        }
    }
}
