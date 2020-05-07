package com.minelittlepony.hdskins.common.texture;

public class HDSkinProcessor {

    public static void processHDSkins(Image image) {
        // copy layers
        // leg
        image.copy(4, 16, 16, 32, 4, 4, true, false); // top
        image.copy(8, 16, 16, 32, 4, 4, true, false); // bottom
        image.copy(0, 20, 24, 32, 4, 12, true, false); // inside
        image.copy(4, 20, 16, 32, 4, 12, true, false); // front
        image.copy(8, 20, 8, 32, 4, 12, true, false); // outside
        image.copy(12, 20, 16, 32, 4, 12, true, false); // back
        // arm
        image.copy(44, 16, -8, 32, 4, 4, true, false); // top
        image.copy(48, 16, -8, 32, 4, 4, true, false); // bottom
        image.copy(40, 20, 0, 32, 4, 12, true, false);// inside
        image.copy(44, 20, -8, 32, 4, 12, true, false);// front
        image.copy(48, 20, -16, 32, 4, 12, true, false);// outside
        image.copy(52, 20, -8, 32, 4, 12, true, false); // back
    }
}
