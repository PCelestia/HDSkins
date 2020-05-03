package com.minelittlepony.hdskins.common.skins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SkinServerList {

    private static final String CONFIG_NAME = "skinservers.json";
    private static final int CONFIG_VERSION = 1;
    private static final Logger logger = LogManager.getLogger();
    private transient final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(SkinServerList.class, (InstanceCreator<SkinServerList>) (type) -> this)
            .create();

    private transient final Path configPath;

    private final int configVersion = CONFIG_VERSION;
    private final List<SkinServer> servers = Arrays.asList(
            makeServer("valhalla", ImmutableMap.of("address", "https://skins.minelittlepony-mod.com")),
            makeServer("mojang", ImmutableMap.of())
    );

    private static SkinServer makeServer(String type, @Nullable Map<String, Object> args) {
        Type t = SkinServerTypes.instance.getType(type);
        Gson gson = new Gson();
        JsonObject obj = gson.toJsonTree(args).getAsJsonObject();
        return gson.fromJson(obj, t);
    }

    public SkinServerList(Path configPath) {
        this.configPath = configPath.resolve(CONFIG_NAME);
    }

    private void logVersionMismatch() {
        logger.warn("HDSkins {} file has a version mismatch! The default config will be used until it is manually deleted or updated.", CONFIG_NAME);
        logger.warn("Using the following config:");
        for (String s : gson.toJson(this).split("\n")) {
            logger.warn(s);
        }
    }

    public void loadJson() throws IOException {
        try (BufferedReader r = Files.newBufferedReader(configPath)) {
            JsonObject o = gson.fromJson(r, JsonObject.class);
            int version = o.get("configVersion").getAsInt();
            if (version != this.configVersion) {
                logVersionMismatch();
                return;
            }

            gson.fromJson(o, SkinServerList.class);
        }
    }

    public void saveJson() throws IOException {
        Files.createDirectories(configPath.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(configPath)) {
            gson.toJson(this, w);
        }
    }

    public List<SkinServer> getSkinServers() {
        return ImmutableList.copyOf(servers);
    }

    public Iterator<SkinServer> getCycler() {
        return Iterators.cycle(getSkinServers());
    }
}
