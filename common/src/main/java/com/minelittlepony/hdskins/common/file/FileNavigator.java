package com.minelittlepony.hdskins.common.file;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public abstract class FileNavigator {

    @Nullable
    private Path directory;

    public void setDirectory(Path directory) {
        try {
            if (directory.isAbsolute() && directory.getFileName() != null && directory.getFileName().toString().equals("..") && directory.getParent().getParent() == null) {
                onDirectory(null, listDrivesOnWindows());
                this.directory = null;
            } else {
                directory = directory.toAbsolutePath().normalize();
                if (Files.isDirectory(directory)) {
                    onDirectory(directory, Files.list(directory));
                    this.directory = directory;
                } else {
                    onSelect(directory);
                }
            }
        } catch (IOError | IOException e) {
            onError(this.directory);
        }
    }

    private Stream<Path> listDrivesOnWindows() {
        return Arrays.stream(File.listRoots()).map(File::toPath);
    }

    public void setDirectory(String path) {
        try {
            setDirectory(Paths.get(path));
        } catch (InvalidPathException e) {
            onError(this.directory);
        }
    }

    public void resolve(String child) {
        if (directory != null) {
            try {
                setDirectory(directory.resolve(child));
            } catch (InvalidPathException e) {
                onError(this.directory);
            }
        }
    }

    protected abstract void onSelect(Path path);

    protected abstract void onDirectory(@Nullable Path directory, Stream<Path> children);

    protected abstract void onError(Path oldDirectory);

    public boolean canNavigateUp() {
        return directory.getParent() != null;
    }
}
