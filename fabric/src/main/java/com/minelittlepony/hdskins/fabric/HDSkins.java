package com.minelittlepony.hdskins.fabric;

import com.minelittlepony.hdskins.skins.SkinServerList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class HDSkins implements ModInitializer {
    public static final String MOD_ID = "hdskins";

    public static final Logger logger = LogManager.getLogger();

    private static HDSkins instance;

    public static HDSkins instance() {
        return instance;
    }

    private SkinServerList skinServers;

    public HDSkins() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        Path configDir = FabricLoader.getInstance().getConfigDirectory().toPath();
        skinServers = new SkinServerList(configDir.resolve("skinservers.json"));
        try {
            try {
                skinServers.loadJson();
            } catch (NoSuchFileException e) {
                skinServers.saveJson();
            }
        } catch (IOException e) {
            logger.warn("Unable to load skin servers.", e);
        }
    }

    public SkinServerList getSkinServers() {
        return skinServers;
    }

}
