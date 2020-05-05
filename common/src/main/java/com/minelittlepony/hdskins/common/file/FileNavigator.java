package com.minelittlepony.hdskins.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class FileNavigator {

    private Path directory;

    public void setDirectory(Path directory) {
        directory = directory.toAbsolutePath();
        if (Files.isDirectory(directory)) {
            try {
                onDirectory(directory, Files.list(directory));
                this.directory = directory;
            } catch (IOException e) {
                onError(this.directory);
            }
        } else {
            setDirectory(directory.getParent());
        }
    }

    public void resolve(String child) {
        setDirectory(directory.resolve(child));
    }

    protected abstract void onDirectory(Path directory, Stream<Path> children);

    protected abstract void onError(Path oldDirectory);

    public boolean canNavigateUp() {
        return directory.getParent() != null;
    }
}
