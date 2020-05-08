package com.minelittlepony.hdskins.common.gui.screen;

import com.google.common.collect.Streams;
import com.minelittlepony.hdskins.common.file.FileDrop;
import com.minelittlepony.hdskins.common.file.FileNavigator;
import com.minelittlepony.hdskins.common.gui.IButton;
import com.minelittlepony.hdskins.common.gui.IGuiHelper;
import com.minelittlepony.hdskins.common.gui.ITextField;
import com.minelittlepony.hdskins.common.gui.PathList;
import com.minelittlepony.hdskins.common.gui.Widgets;
import com.minelittlepony.hdskins.common.gui.element.Label;
import com.minelittlepony.hdskins.common.gui.element.PlayerModelElement;
import com.minelittlepony.hdskins.common.skins.Feature;
import com.minelittlepony.hdskins.common.upload.Uploader;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SkinUploadScreen extends CustomScreen {

    private final FileNavigator navigator = new FileNavigator() {
        @Override
        protected void onDirectory(@Nullable Path directory, Stream<Path> children) {
            SkinUploadScreen.this.onDirectory(directory, children);
        }

        @Override
        protected void onSelect(Path path) {
            SkinUploadScreen.this.onSelect(path);
        }

        @Override
        protected void onError(Path oldDirectory) {
            textInput.setContent(oldDirectory.toString());
        }
    };

    private Path currentDirectory;

    private final FileDrop dropper;
    private Uploader uploader;

    private IButton btnBrowse;
    private IButton btnUpload;
    private IButton btnDownload;
    private IButton btnClear;
    private IButton btnModeSteve;
    private IButton btnModeAlex;
    private IButton btnModeSkin;
    private IButton btnModeElytra;

    private ITextField textInput;
    private PathList pathList;
    private PlayerModelElement entity;
    private Label lblTitle;
    private Label lblLocal;
    private Label lblServer;

    public SkinUploadScreen(Uploader uploader, Function<Consumer<List<Path>>, FileDrop> dropper) {
        super("hdskins.gui.title");
        this.uploader = uploader;
        this.dropper = dropper.apply(this::onDrop);
        currentDirectory = Paths.get(".");
    }

    private Predicate<Path> filterHidden() {
        return path -> {
            try {
                return path.endsWith(Paths.get(".."))
                        || Files.exists(path)
                        && !Files.isHidden(path)
                        && !path.getFileName().toString().startsWith(".")
                        && (!Files.isRegularFile(path) || path.getFileName().toString().toLowerCase().endsWith(".png"));
            } catch (IOException e) {
                LogManager.getLogger().catching(e);
                return true;
            }
        };
    }

    private void onDirectory(@Nullable Path directory, Stream<Path> children) {
        pathList.clear();
        currentDirectory = directory;
        textInput.setScroll(0);
        textInput.setContent(Objects.toString(directory, ""));
        if (directory != null) {
            children = Streams.concat(Stream.of(directory.resolve("..")), children)
                    .filter(filterHidden());
        }
        children.forEach(d -> pathList.addPath(d, getDisplayStringForPath(d), navigator::setDirectory));
    }

    private static String getDisplayStringForPath(Path path) {
        String text;
        if (path.getFileName() != null) {
            text = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                text += File.separator;
            }
        } else {
            text = path.toString();
        }
        return text;
    }

    private void onSelect(Path path) {
        System.out.println(path);
    }

    @Override
    protected void init(Widgets factory) {
        this.dropper.subscribe();
        int entityWPos = screen.getWidth() / 2 - 60;
        int entityXPos = screen.getWidth() - entityWPos;
        entity = factory.addEntity(entityXPos - 30, 39, entityWPos, screen.getHeight() - 100);

        lblTitle = new Label(screen.getTextRenderer(),
                screen.getWidth() / 2, 5,
                screen.translate("hdskins.manager"),
                -1, false, true);
        lblLocal = new Label(screen.getTextRenderer(),
                34, 29, screen.translate("hdskins.local"));
        lblServer = new Label(screen.getTextRenderer(),
                screen.getWidth() / 2 + 34, 29,
                screen.translate("hdskins.server"));

        btnBrowse = factory.addButton(screen.getWidth() / 2 - 150, screen.getHeight() - 27, 90, 20,
                "hdskins.options.browse", null, this::browse);
        btnUpload = factory.addButton(screen.getWidth() / 2 - 24, screen.getHeight() / 2 - 40, 48, 20,
                "hdskins.options.upload", "hdskins.options.upload.title", this::upload);
        btnDownload = factory.addButton(screen.getWidth() / 2 - 24, screen.getHeight() / 2 + 20, 48, 20,
                "hdskins.options.download", "hdskins.options.download.title", this::download);
        btnDownload.setEnabled(false);
        btnClear = factory.addButton(screen.getWidth() / 2 + 60, screen.getHeight() - 27, 90, 20,
                "hdskins.options.clear", this::clear);

        factory.addButton(screen.getWidth() / 2 - 50, screen.getHeight() - 25, 100, 20,
                "hdskins.options.close", null, b -> screen.close());
        btnModeSteve = factory.addButtonIcon(screen.getWidth() - 25, 32,
                "player_head{SkullOwner:{Id:00000000-0000-0000-0000-000000000002}}",
                "hdskins.mode.steve", this::setSteveMode);
        btnModeAlex = factory.addButtonIcon(screen.getWidth() - 25, 51,
                "player_head{SkullOwner:{Id:00000000-0000-0000-0000-000000000001}}",
                "hdskins.mode.alex", this::setAlexMode);

        btnModeSkin = factory.addButtonIcon(screen.getWidth() - 25, 75, "leather_chestplate",
                "hdskins.mode.skin", this::setSkinMode);
        btnModeElytra = factory.addButtonIcon(screen.getWidth() - 25, 94, "elytra",
                "hdskins.mode.elytra", this::setElytraMode);

        pathList = factory.addPathList(screen.getWidth() / 2 - 60, screen.getHeight(), 64, screen.getHeight() - 64);
        pathList.setLeft(30);

        textInput = factory.addTextField(30, 40, screen.getWidth() / 2 - 60, 20, "");
        textInput.setMaxContentLength(255);
        textInput.setCallback(navigator::setDirectory);

        navigator.setDirectory(currentDirectory);

        updateButtons();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta, IGuiHelper gui) {
        pathList.render(mouseX, mouseY, delta, gui);
        entity.render(mouseX, mouseY, delta, gui);

        lblTitle.render(mouseX, mouseY, delta, gui);
        lblLocal.render(mouseX, mouseY, delta, gui);
        lblServer.render(mouseX, mouseY, delta, gui);
    }

    private void setSkinMode(IButton iButton) {
        setTextureType(Type.SKIN);
    }

    private void setElytraMode(IButton iButton) {
        setTextureType(Type.ELYTRA);
    }

    private void setAlexMode(IButton iButton) {
        setSkinModel("slim");
    }

    private void setSteveMode(IButton iButton) {
        setSkinModel("default");
    }

    private void setTextureType(Type type) {
        uploader.setSkinType(type);
        updateButtons();
    }

    private void setSkinModel(String model) {
        uploader.setMetadata("model", model);
        updateButtons();
    }

    private void clear(IButton iButton) {
        punchServer("hdskins.request");
    }

    private void browse(IButton button) {
//        chooser.openBrowsePNG(I18n.translate("hdskins.open.title"));
    }

    private void upload(IButton button) {
        if (uploader.getLocalSkin() != null) {
            punchServer("hdskins.upload");
        }
    }

    private void download(IButton button) {
//        chooser.openSavePNG(I18n.translate("hdskins.save.title"), minecraft.getSession().getUsername());
    }

    @Override
    public void removed() {
        this.dropper.close();
    }

    private void punchServer(String message) {

    }

    private void onDrop(List<Path> paths) {
        paths.stream().findFirst().ifPresent(path -> {
            this.uploader.setLocalSkin(path.toFile().toURI());
            updateButtons();
            LogManager.getLogger().info("Dropped file {}", path);
        });
    }

    private void updateButtons() {

        Set<Feature> features = uploader.getGateway().getFeatures();

        boolean hasRemote = false;
        boolean hasLocal = uploader.getLocalSkin() != null;

        btnClear.setEnabled(hasRemote);
        btnUpload.setEnabled(hasLocal && features.contains(Feature.UPLOAD_USER_SKIN));
        btnDownload.setEnabled(hasRemote);

        boolean types = !features.contains(Feature.MODEL_TYPES);
        boolean variants = !features.contains(Feature.MODEL_VARIANTS);

        boolean isSkin = uploader.getSkinType() == Type.SKIN;
        boolean isSlim = "slim".equals(uploader.getMetadata("model"));

        btnModeSkin.setEnabled(!isSkin);
        btnModeElytra.setEnabled(isSkin);

        btnModeSteve.setEnabled(isSlim && isSkin);
        btnModeAlex.setEnabled(!isSlim && isSkin);

//        btnClear.setLocked(!features.contains(Feature.DELETE_USER_SKIN));
//        btnUpload.setLocked(!features.contains(Feature.UPLOAD_USER_SKIN));
//        btnDownload.setLocked(!features.contains(Feature.DOWNLOAD_USER_SKIN));
    }

}
