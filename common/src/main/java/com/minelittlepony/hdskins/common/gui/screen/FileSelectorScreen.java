package com.minelittlepony.hdskins.common.gui.screen;

import com.minelittlepony.hdskins.common.file.FileNavigator;
import com.minelittlepony.hdskins.common.gui.IButton;
import com.minelittlepony.hdskins.common.gui.ITextField;

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

    private ITextField textInput;

    protected FileSelectorScreen(String title) {
        super(title);
    }

    @Override
    public void init() {
        textInput = screen.addTextField(10, 30, screen.getWidth() - 50, 18, "");
        textInput.setMaxContentLength(Short.MAX_VALUE);

        screen.addButton(screen.getWidth() - 30, 29, 20, 20,
                "hdskins.directory.go", null, this::goDirectory);

        screen.addLabel(screen.getWidth() / 2, 5, getTitle(), -1, false, false);

        IButton parentBtn = screen.addButton(screen.getWidth() / 2 - 160, screen.getHeight() - 25, 100, 20,
                "hdskins.directory.up", null, this::upDirectory);
        parentBtn.setEnabled(navigator.canNavigateUp());

        screen.addButton(screen.getWidth()/2 + 60, screen.getHeight() - 25, 100, 20,
                "hdskins.options.close", null, b -> screen.close());

        // TODO get the last used directory
        navigator.setDirectory(Paths.get("."));
    }

    private void goDirectory(IButton button) {
        navigator.setDirectory(Paths.get(textInput.getContent()));
    }

    private void upDirectory(IButton button) {
        navigator.resolve("..");
    }

}
