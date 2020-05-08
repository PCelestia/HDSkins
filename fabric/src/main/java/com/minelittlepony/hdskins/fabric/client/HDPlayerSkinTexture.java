package com.minelittlepony.hdskins.fabric.client;

import com.minelittlepony.hdskins.common.texture.HDSkinProcessor;
import com.minelittlepony.hdskins.common.texture.Image;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

public class HDPlayerSkinTexture extends PlayerSkinTexture implements IPlayerSkinTexture {
    private static final Logger LOGGER = LogManager.getLogger();

    private final IPlayerSkinTextureAccessors that = (IPlayerSkinTextureAccessors) this;

    public HDPlayerSkinTexture(IPlayerSkinTextureAccessors texture) {
        super(texture.getCacheFile(), texture.getUrl(), texture.getLocation(), texture.isConvertLegacy(), texture.getLoadedCallback());
    }

    @Nullable
    @Override
    public NativeImage loadTextureOverride(InputStream inputStreamIn) {
        NativeImage nativeimage = null;

        try {
            nativeimage = NativeImage.read(inputStreamIn);
            if (that.isConvertLegacy()) {
                nativeimage = processLegacySkin0(nativeimage);
            }
        } catch (IOException ioexception) {
            LOGGER.warn("Error while loading the skin texture", ioexception);
        }

        return nativeimage;
    }

    private NativeImage processLegacySkin0(NativeImage image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        if (imageHeight != imageWidth) {

            // it needs to be square
            //noinspection SuspiciousNameCombination
            NativeImage image2 = new NativeImage(imageWidth, imageWidth, true);
            image2.copyFrom(image);
            image.close();
            image = image2;

            HDSkinProcessor.processHDSkins(new Image() {
                @Override
                public int getWidth() {
                    return image2.getWidth();
                }

                @Override
                public int getHeight() {
                    return image2.getHeight();
                }

                @Override
                public void copyRaw(int xFrom, int yFrom, int xOffset, int yOffset, int width, int height, boolean mirrorX, boolean mirrorY) {
                    image2.copyRect(xFrom, yFrom, xOffset, yOffset, width, height, mirrorX, mirrorY);
                }
            });

        }

        return image;
    }

}
