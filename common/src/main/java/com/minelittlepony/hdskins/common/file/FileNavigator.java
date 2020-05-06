package com.minelittlepony.hdskins.common.file;

import java.awt.geom.IllegalPathStateException;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public abstract class FileNavigator {

    private Path directory;

    public void setDirectory(Path directory) {
        try {
            directory = directory.toAbsolutePath().normalize();
            if (Files.isDirectory(directory)) {
                onDirectory(directory, Files.list(directory));
                this.directory = directory;
            } else {
                onSelect(directory);
            }
        } catch (IOError | IOException e) {
            onError(this.directory);
        }
    }

    public void setDirectory(String path) {
        try {
            setDirectory(Paths.get(path));
        } catch (InvalidPathException e) {
            onError(this.directory);
        }
    }

    public void resolve(String child) {
        try {
            setDirectory(directory.resolve(child));
        } catch (InvalidPathException e) {
            onError(this.directory);
        }
    }

    protected abstract void onSelect(Path path);

    protected abstract void onDirectory(Path directory, Stream<Path> children);

    protected abstract void onError(Path oldDirectory);

    public boolean canNavigateUp() {
        return directory.getParent() != null;
    }
}
