package com.minelittlepony.hdskins.fabric.client.gui.screen;

import com.minelittlepony.hdskins.common.gui.IScreen;
import com.minelittlepony.hdskins.common.gui.screen.CustomScreen;
import com.minelittlepony.hdskins.fabric.client.gui.AbstractYarnWidgets;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.TranslatableText;

import java.util.List;

public class YarnScreenWrapper extends Screen {

    protected YarnScreen screenHelper;
    protected final CustomScreen screen;

    public YarnScreenWrapper(CustomScreen screen) {
        super(new TranslatableText(screen.getTitle()));
        this.screen = screen;
    }

    @Override
    protected void init() {
        screenHelper = new YarnScreen();
        screen.init(screenHelper);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();

        screen.render(mouseX, mouseY, delta);
        super.render(mouseX, mouseY, delta);
        screenHelper.render(mouseX, mouseY, delta);

        this.buttons.stream()
                .filter(AbstractButtonWidget::isHovered)
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

    protected class YarnScreen extends AbstractYarnWidgets implements IScreen {

        @Override
        protected TextRenderer getTextRenderer() {
            return font;
        }

        @Override
        protected <B extends AbstractButtonWidget> B addButton(B button) {
            return YarnScreenWrapper.this.addButton(button);
        }

        @Override
        protected List<Element> children() {
            return YarnScreenWrapper.this.children;
        }

        @Override
        public int getWidth() {
            return YarnScreenWrapper.this.width;
        }

        @Override
        public int getHeight() {
            return YarnScreenWrapper.this.height;
        }

        @Override
        public void close() {
            YarnScreenWrapper.this.onClose();
        }
    }
}
