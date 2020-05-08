package com.minelittlepony.hdskins.common.gui.screen;

import com.minelittlepony.hdskins.common.file.FileNavigator;
import com.minelittlepony.hdskins.common.gui.IButton;
import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.ITextField;
import com.minelittlepony.hdskins.common.gui.Widgets;
import com.minelittlepony.hdskins.common.gui.element.Label;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

public class FileSelectorScreen extends CustomScreen {

    private final FileNavigator navigator = new FileNavigator() {
        @Override
        protected void onDirectory(@Nullable Path directory, Stream<Path> children) {
            FileSelectorScreen.this.textInput.setContent(Objects.toString(directory, ""));
        }

        @Override
        protected void onSelect(Path path) {
            System.out.println(path);
        }

        @Override
        protected void onError(Path oldDirectory) {
            textInput.setContent(oldDirectory.toString());
        }
    };

    private Label lblTitle;
    private ITextField textInput;

    protected FileSelectorScreen(String title) {
        super(title);
    }

    @Override
    public void init(Widgets factory) {
        textInput = factory.addTextField(10, 30, screen.getWidth() - 50, 18, "");
        textInput.setMaxContentLength(Short.MAX_VALUE);

        factory.addButton(screen.getWidth() - 30, 29, 20, 20,
                "hdskins.directory.go", null, this::goDirectory);

        lblTitle = new Label(screen.getTextRenderer(), screen.getWidth() / 2, 5, screen.translate(getTitle()), -1, false, false);

        IButton parentBtn = factory.addButton(screen.getWidth() / 2 - 160, screen.getHeight() - 25, 100, 20,
                "hdskins.directory.up", null, this::upDirectory);
        parentBtn.setEnabled(navigator.canNavigateUp());

        factory.addButton(screen.getWidth()/2 + 60, screen.getHeight() - 25, 100, 20,
                "hdskins.options.close", null, b -> screen.close());

        // TODO get the last used directory
        navigator.setDirectory(Paths.get("."));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta, IGuiHelper gui) {
        lblTitle.render(mouseX, mouseY, delta, gui);
    }

    private void goDirectory(IButton button) {
        navigator.setDirectory(Paths.get(textInput.getContent()));
    }

    private void upDirectory(IButton button) {
        navigator.resolve("..");
    }

}
