package com.minelittlepony.hdskins;

import com.minelittlepony.hdskins.upload.FileSystem;
import com.minelittlepony.hdskins.upload.IFileDialog;
import com.minelittlepony.hdskins.util.net.MoreHttpResponses;

import net.minecraft.client.texture.NativeImage;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

public class SkinChooser {

    public static final int MAX_SKIN_DIMENSION = 1024;

    public static final String[] EXTENSIONS = new String[]{"png", "PNG"};

    public static final String ERR_UNREADABLE = "hdskins.error.unreadable";
    public static final String ERR_EXT = "hdskins.error.ext";
    public static final String ERR_OPEN = "hdskins.error.open";
    public static final String ERR_INVALID_TOO_LARGE = "hdskins.error.invalid.too_large";
    public static final String ERR_INVALID_SHAPE = "hdskins.error.invalid.shape";
    public static final String ERR_INVALID_POWER_OF_TWO = "hdskins.error.invalid.power_of_two";
    public static final String ERR_INVALID = "hdskins.error.invalid";

    public static final String MSG_CHOOSE = "hdskins.choose";

    private static boolean isPowerOfTwo(int number) {
        return number != 0 && (number & number - 1) == 0;
    }

    @Nullable
    private IFileDialog openFileThread;

    private final SkinUploader uploader;

    private volatile String status = MSG_CHOOSE;

    public SkinChooser(SkinUploader uploader) {
        this.uploader = uploader;
    }

    public boolean pickingInProgress() {
        return openFileThread != null;
    }

    public String getStatus() {
        return status;
    }

    public void openBrowsePNG(String title) {
        openFileThread = FileSystem.openRead(title)
                .filter(".png", "PNG Files (*.png)")
                .andThen((file, success) -> {
            openFileThread = null;

            if (success) {
                selectFile(file);
            }
        }).launch();
    }

    public void openSavePNG(String title, String filename) {
        openFileThread = FileSystem.openWrite(title, filename)
                .filter(".png", "PNG Files (*.png)")
                .andThen((file, success) -> {
            openFileThread = null;

            if (success) {
                try (MoreHttpResponses response = uploader.downloadSkin().get()) {
                    if (response.ok()) {
                        Files.copy(response.getInputStream(), file);
                    }
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).launch();
    }

    public void selectFile(Path skinFile) {
        status = evaluateAndSelect(skinFile);
    }

    private String evaluateAndSelect(Path skinFile) {
        if (!Files.exists(skinFile)) {
            return ERR_UNREADABLE;
        }

        if (!FilenameUtils.isExtension(skinFile.getFileName().toString(), EXTENSIONS)) {
            return ERR_EXT;
        }

        try (InputStream in = Files.newInputStream(skinFile)) {
            NativeImage chosenImage = NativeImage.read(in);

            String err = acceptsSkinDimensions(chosenImage.getWidth(), chosenImage.getHeight());
            if (err != null) {
                return err;
            }

            uploader.setLocalSkin(skinFile);

            return MSG_CHOOSE;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ERR_OPEN;
    }

    @Nullable
    protected String acceptsSkinDimensions(int w, int h) {
        if (!isPowerOfTwo(w)) {
            return ERR_INVALID_POWER_OF_TWO;
        }
        if (w > MAX_SKIN_DIMENSION) {
            return ERR_INVALID_TOO_LARGE;
        }
        if (!(w == h || w == h * 2)) {
            return ERR_INVALID_SHAPE;
        }
        return null;
    }
}