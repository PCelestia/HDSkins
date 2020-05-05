package com.minelittlepony.hdskins.common.file;

import com.google.common.collect.Lists;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Wrapper around GLFW to handle file drop events.
 */
public class FileDrop implements AutoCloseable {

    private boolean cancelled;

    @Nullable
    private GLFWDropCallback hook;

    private final Executor minecraft;
    private final LongSupplier windowHandle;
    private final Consumer<List<Path>> callback;

    public FileDrop(Executor minecraft, LongSupplier windowHandle, Consumer<List<Path>> callback) {
        this.minecraft = minecraft;
        this.windowHandle = windowHandle;
        this.callback = callback;
    }

    private void invoke(long window, int count, long names) {
        PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);

        List<Path> paths = Lists.newArrayList();

        for (int i = 0; i < count; i++) {
            paths.add(Paths.get(MemoryUtil.memUTF8(charPointers.get(i))));
        }

        callback.accept(paths);
    }

    public void subscribe() {
        if (!cancelled && hook == null) {
            minecraft.execute(() -> {
                if (!cancelled) {
                    hook = GLFW.glfwSetDropCallback(windowHandle.getAsLong(), this::invoke);
                }
            });
        }
    }

    public void close() {
        cancelled = true;

        if (hook != null) {
            minecraft.execute(() -> {
                hook = GLFW.glfwSetDropCallback(windowHandle.getAsLong(), null);
            });
        }
    }

}
