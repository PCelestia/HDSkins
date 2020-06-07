package com.minelittlepony.hdskins.forge.client.gui;

import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.common.gui.AbstractPlayerEntityRenderer;
import com.minelittlepony.hdskins.forge.client.HDDownloadingTexture;
import com.minelittlepony.hdskins.forge.client.HDSkinsClient;
import com.minelittlepony.hdskins.forge.client.entity.DummyPlayer;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class GuiPlayerEntityRenderer extends AbstractPlayerEntityRenderer {

    private final Map<MinecraftProfileTexture.Type, DynamicTexture> textures = new HashMap<>();
    private final DummyPlayer entity;

    public GuiPlayerEntityRenderer(DummyPlayer entity) {
        this.entity = entity;
    }

    public void renderStatic(int posX, int posY, int scale, float mouseX, float mouseY) {
        EntityRendererManager erm = Minecraft.getInstance().getRenderManager();
        if (erm.info == null) {
            erm.info = new ActiveRenderInfo();
        }

        float f1 = (float) Math.atan(mouseY / 40.0F);

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) posX, (float) posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float) scale, (float) scale, (float) scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(pitch);
        quaternion.multiply(quaternion1);
        matrixstack.rotate(quaternion);
        entity.renderYawOffset = rotation;
        entity.rotationYaw = rotation;
        entity.rotationPitch = -f1 * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        entityrenderermanager.renderEntityStatic(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        RenderSystem.popMatrix();
    }

    @Override
    public void loadSkins(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {
        RenderSystem.recordRenderCall(() -> {
            entity.resetSkins();
            for (MinecraftProfileTexture.Type textureType : MinecraftProfileTexture.Type.values()) {
                if (textures.containsKey(textureType)) {
                    MinecraftProfileTexture texture = textures.get(textureType);
                    HDSkinsClient.loadSkin(texture, textureType, entity::loadSkins);
                }
            }
        });
    }

    @Override
    public void loadTexture(MinecraftProfileTexture.Type skinType, Path path) {
        ResourceLocation location = new ResourceLocation(IHDSkins.MOD_ID, "skin_preview/" + skinType.name().toLowerCase(Locale.ROOT));

        RenderSystem.recordRenderCall(() -> {
            try (InputStream input = Files.newInputStream(path)) {
                HDDownloadingTexture texture = new HDDownloadingTexture(new DownloadingTexture(null, "", location, skinType == MinecraftProfileTexture.Type.SKIN, null));
                NativeImage image = Objects.requireNonNull(texture.loadTexture(input));
                DynamicTexture tex = textures.compute(skinType, (k, v) -> {
                    if (v == null) {
                        return new DynamicTexture(image);
                    }
                    try {
                        v.setTextureData(image);
                        v.updateDynamicTexture();
                    } catch (Exception e) {
                        // will probably never happen. The method declared throws, but never throws.
                        LogManager.getLogger().error(e);
                    }
                    return v;
                });
                Minecraft.getInstance().getTextureManager().loadTexture(location, tex);
                entity.setTexture(skinType, location);
            } catch (IOException e) {
                LogManager.getLogger().error(e);
            }
        });
    }

    @Override
    public void close() {
        textures.values().forEach(DynamicTexture::close);
    }
}