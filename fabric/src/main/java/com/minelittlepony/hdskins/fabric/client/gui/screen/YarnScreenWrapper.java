package com.minelittlepony.hdskins.fabric.client.gui.screen;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.ITextRenderer;
import com.minelittlepony.hdskins.common.gui.screen.CustomScreen;
import com.minelittlepony.hdskins.fabric.client.gui.AbstractYarnWidgets;
import com.minelittlepony.hdskins.fabric.client.gui.GuiHelperAdapter;
import com.minelittlepony.hdskins.fabric.client.gui.YarnScreenAdapter;
import com.minelittlepony.hdskins.fabric.client.gui.YarnTextAdapter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;
import java.util.List;

public class YarnScreenWrapper extends Screen {

    protected final Screen parent;
    protected final CustomScreen screen;
    private final IGuiHelper gui = new GuiHelperAdapter(this);

    public YarnScreenWrapper(@Nullable Screen parent, CustomScreen screen) {
        super(new TranslatableText(screen.getTitle()));
        this.parent = parent;
        this.screen = screen;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void init() {
        screen.init(new YarnScreen(), new YarnWidgets());
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();

        screen.render(mouseX, mouseY, delta, gui);
        super.render(mouseX, mouseY, delta);

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

    @Override
    public void onClose() {
        this.minecraft.openScreen(parent);
    }

    protected class YarnScreen extends YarnScreenAdapter {

        public YarnScreen() {
            super(YarnScreenWrapper.this);
        }

        @Override
        public ITextRenderer getTextRenderer() {
            return new YarnTextAdapter(font);
        }
    }

    protected class YarnWidgets extends AbstractYarnWidgets {

        @Override
        public TextRenderer getTextRenderer() {
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

    }
}
