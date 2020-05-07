package com.minelittlepony.hdskins.fabric;

import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.common.skins.SkinServerList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class HDSkins implements IHDSkins, ModInitializer {

    private SkinServerList skinServers;

    @Override
    public void onInitialize() {
        Path configDir = FabricLoader.getInstance().getConfigDirectory().toPath();
        skinServers = new SkinServerList(configDir.resolve(MOD_ID));
        skinServers.tryLoad();
    }

    @Override
    public SkinServerList getSkinServers() {
        return skinServers;
    }

}
