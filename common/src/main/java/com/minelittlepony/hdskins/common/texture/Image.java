package com.minelittlepony.hdskins.common.texture;

public interface Image {

    int getWidth();

    int getHeight();

    /**
     * Copies image data without scaling.
     */
    void copyRaw(int xFrom, int yFrom, int xOffset, int yOffset, int width, int height, boolean mirrorX, boolean mirrorY);

    /**
     * Copies image data with scaling. Scaling is determined by dividing the image by 64.
     */
    default void copy(int xFrom, int yFrom, int xOffset, int yOffset, int width, int height, boolean mirrorX, boolean mirrorY) {
        int scale = getWidth() / 64;
        copyRaw(xFrom * scale, yFrom * scale, xOffset * scale, yOffset * scale, width * scale, height * scale, mirrorX, mirrorY);
    }
}
