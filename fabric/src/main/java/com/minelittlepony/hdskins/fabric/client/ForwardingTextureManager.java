package com.minelittlepony.hdskins.fabric.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class ForwardingTextureManager extends TextureManager {
    private final TextureManager textures;

    public ForwardingTextureManager(TextureManager textures) {
        super(MinecraftClient.getInstance().getResourceManager());
        this.textures = textures;
    }

    @Override
    public void bindTexture(Identifier id) {
        textures.bindTexture(id);
    }

    @Override
    public void registerTexture(Identifier identifier, AbstractTexture abstractTexture) {
        textures.registerTexture(identifier, abstractTexture);
    }

    @Override
    @Nullable
    public AbstractTexture getTexture(Identifier id) {
        return textures.getTexture(id);
    }

    @Override
    public Identifier registerDynamicTexture(String prefix, NativeImageBackedTexture texture) {
        return textures.registerDynamicTexture(prefix, texture);
    }

    @Override
    public CompletableFuture<Void> loadTextureAsync(Identifier id, Executor executor) {
        return textures.loadTextureAsync(id, executor);
    }

    @Override
    public void tick() {
        textures.tick();
    }

    @Override
    public void destroyTexture(Identifier id) {
        textures.destroyTexture(id);
    }

    @Override
    public void close() {
        textures.close();
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return textures.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
    }

    @Override
    public String getName() {
        return textures.getName();
    }

}
