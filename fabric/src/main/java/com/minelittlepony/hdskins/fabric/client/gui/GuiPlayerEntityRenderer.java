package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.common.gui.AbstractPlayerEntityRenderer;
import com.minelittlepony.hdskins.fabric.client.HDPlayerSkinTexture;
import com.minelittlepony.hdskins.fabric.client.HDSkinsClient;
import com.minelittlepony.hdskins.fabric.client.IPlayerSkinTextureAccessors;
import com.minelittlepony.hdskins.fabric.client.entity.DummyPlayer;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
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

    private final Map<MinecraftProfileTexture.Type, NativeImageBackedTexture> textures = new HashMap<>();
    private final DummyPlayer entity;

    public GuiPlayerEntityRenderer(DummyPlayer entity) {
        this.entity = entity;
    }

    public void renderStatic(int posX, int posY, int scale, float mouseX, float mouseY) {
        EntityRenderDispatcher erm = MinecraftClient.getInstance().getEntityRenderManager();
        if (erm.camera == null) {
            erm.camera = new Camera();
        }

        float f1 = (float) Math.atan(mouseY / 40.0F);

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) posX, (float) posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float) scale, (float) scale, (float) scale);
        Quaternion quaternion = Vector3f.POSITIVE_Z.getDegreesQuaternion(180);
        Quaternion quaternion1 = Vector3f.POSITIVE_X.getDegreesQuaternion(pitch);
        quaternion.hamiltonProduct(quaternion1);
        matrixstack.multiply(quaternion);
        entity.bodyYaw = rotation;
        entity.yaw = rotation;
        entity.pitch = -f1 * 20.0F;
        entity.headYaw = entity.yaw;
        entity.prevHeadYaw = entity.yaw;
        EntityRenderDispatcher entityrenderermanager = MinecraftClient.getInstance().getEntityRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setRotation(quaternion1);
        entityrenderermanager.setRenderShadows(false);
        VertexConsumerProvider.Immediate irendertypebuffer$impl = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        entityrenderermanager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        irendertypebuffer$impl.draw();
        entityrenderermanager.setRenderShadows(true);
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
        Identifier location = new Identifier(IHDSkins.MOD_ID, "skin_preview/" + skinType.name().toLowerCase(Locale.ROOT));

        RenderSystem.recordRenderCall(() -> {
            try (InputStream input = Files.newInputStream(path)) {

                HDPlayerSkinTexture texture = new HDPlayerSkinTexture((IPlayerSkinTextureAccessors) new PlayerSkinTexture(null, "", location, skinType == MinecraftProfileTexture.Type.SKIN, null));
                NativeImage image = Objects.requireNonNull(texture.loadTextureOverride(input));
                NativeImageBackedTexture tex = textures.compute(skinType, (k, v) -> {
                    if (v == null) {
                        return new NativeImageBackedTexture(image);
                    }
                    try {
                        v.setImage(image);
                        v.upload();
                    } catch (Exception e) {
                        // will probably never happen. The method declared throws, but never throws.
                        LogManager.getLogger().error(e);
                    }
                    return v;
                });
                MinecraftClient.getInstance().getTextureManager().registerTexture(location, tex);
                entity.setTexture(skinType, location);
            } catch (IOException e) {
                LogManager.getLogger().error(e);
            }
        });
    }

    @Override
    public void close() {
        textures.values().forEach(NativeImageBackedTexture::close);
        textures.clear();
    }
}
