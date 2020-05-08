package com.minelittlepony.hdskins.forge.client.gui.screen;

import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.IScreen;
import com.minelittlepony.hdskins.common.gui.ITextRenderer;
import com.minelittlepony.hdskins.common.gui.screen.CustomScreen;
import com.minelittlepony.hdskins.forge.client.gui.AbstractMCPWidgets;
import com.minelittlepony.hdskins.forge.client.gui.GuiHelperAdapter;
import com.minelittlepony.hdskins.forge.client.gui.MCPScreenAdapter;
import com.minelittlepony.hdskins.forge.client.gui.MCPTextAdapter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class MCPScreenWrapper extends Screen {

    protected final CustomScreen screen;
    private final IGuiHelper gui = new GuiHelperAdapter(this);

    public MCPScreenWrapper(CustomScreen screen) {
        super(new TranslationTextComponent(screen.getTitle()));
        this.screen = screen;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void init() {
        screen.init( new SrgScreen(), new SrgWidgets());
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();

        screen.render(mouseX, mouseY, delta, gui);
        super.render(mouseX, mouseY, delta);

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

    protected class SrgScreen extends MCPScreenAdapter {

        public SrgScreen() {
            super(MCPScreenWrapper.this);
        }

        @Override
        public ITextRenderer getTextRenderer() {
            return new MCPTextAdapter(font);
        }
    }

    protected class SrgWidgets extends AbstractMCPWidgets {

        @Override
        public FontRenderer getTextRenderer() {
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

    }
}
