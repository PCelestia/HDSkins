package com.minelittlepony.hdskins.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class ForwardingTextureManager extends TextureManager {
    private final TextureManager textures;

    public ForwardingTextureManager(TextureManager textures) {
        super(Minecraft.getInstance().getResourceManager());
        this.textures = textures;
    }

    @Override
    public void bindTexture(ResourceLocation resource) {
        textures.bindTexture(resource);
    }

    @Override
    public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
        textures.loadTexture(textureLocation, textureObj);
    }

    @Override
    @Nullable
    public Texture getTexture(ResourceLocation textureLocation) {
        return textures.getTexture(textureLocation);
    }

    @Override
    public ResourceLocation getDynamicTextureLocation(String name, DynamicTexture texture) {
        return textures.getDynamicTextureLocation(name, texture);
    }

    @Override
    public CompletableFuture<Void> loadAsync(ResourceLocation textureLocation, Executor executor) {
        return textures.loadAsync(textureLocation, executor);
    }

    @Override
    public void tick() {
        textures.tick();
    }

    @Override
    public void deleteTexture(ResourceLocation textureLocation) {
        textures.deleteTexture(textureLocation);
    }

    @Override
    public void close() {
        textures.close();
    }

    @Override
    public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return textures.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
    }

    @Override
    public String func_225594_i_() {
        return textures.func_225594_i_();
    }


}
