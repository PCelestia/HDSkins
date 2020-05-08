package com.minelittlepony.hdskins.fabric.client.gui;

import com.minelittlepony.hdskins.common.gui.IPlayerEntityRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Quaternion;

public class GuiPlayerEntityRenderer extends IPlayerEntityRenderer {

    private final LivingEntity entity;

    public GuiPlayerEntityRenderer(LivingEntity entity) {
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
}
