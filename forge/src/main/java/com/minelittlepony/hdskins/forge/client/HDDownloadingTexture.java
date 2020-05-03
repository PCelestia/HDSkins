package com.minelittlepony.hdskins.forge.client;

import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class HDDownloadingTexture extends DownloadingTexture {
    private static final Logger LOGGER = LogManager.getLogger();

    private final boolean isSkin;

    public HDDownloadingTexture(@Nullable File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, boolean legacySkinIn, @Nullable Runnable processTaskIn) {
        super(cacheFileIn, imageUrlIn, textureResourceLocation, legacySkinIn, processTaskIn);
        this.isSkin = legacySkinIn;
    }

    @Nullable
    @Override
    public NativeImage loadTexture(InputStream inputStreamIn) {
        NativeImage nativeimage = null;

        try {
            nativeimage = NativeImage.read(inputStreamIn);
            if (this.isSkin) {
                nativeimage = processLegacySkin0(nativeimage);
            }
        } catch (IOException ioexception) {
            LOGGER.warn("Error while loading the skin texture", (Throwable) ioexception);
        }

        return nativeimage;
    }

    private NativeImage processLegacySkin0(NativeImage image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        if (imageHeight != imageWidth) {

            NativeImage image2 = new NativeImage(imageWidth, imageWidth, true);
            image2.copyImageData(image);
            image.close();
            image = image2;

            // copy layers
            // leg
            copy(image, 4, 16, 16, 32, 4, 4, true, false); // top
            copy(image, 8, 16, 16, 32, 4, 4, true, false); // bottom
            copy(image, 0, 20, 24, 32, 4, 12, true, false); // inside
            copy(image, 4, 20, 16, 32, 4, 12, true, false); // front
            copy(image, 8, 20, 8, 32, 4, 12, true, false); // outside
            copy(image, 12, 20, 16, 32, 4, 12, true, false); // back
            // arm
            copy(image, 44, 16, -8, 32, 4, 4, true, false); // top
            copy(image, 48, 16, -8, 32, 4, 4, true, false); // bottom
            copy(image, 40, 20, 0, 32, 4, 12, true, false);// inside
            copy(image, 44, 20, -8, 32, 4, 12, true, false);// front
            copy(image, 48, 20, -16, 32, 4, 12, true, false);// outside
            copy(image, 52, 20, -8, 32, 4, 12, true, false); // back

        }

        return image;
    }

    private static void copy(NativeImage image, int xFrom, int yFrom, int xOffset, int yOffset, int width, int height, boolean mirrorX, boolean mirrorY) {
        int scale = image.getWidth() / 64;
        image.copyAreaRGBA(xFrom * scale, yFrom * scale, xOffset * scale, yOffset * scale, width * scale, height * scale, mirrorX, mirrorY);
    }
}
