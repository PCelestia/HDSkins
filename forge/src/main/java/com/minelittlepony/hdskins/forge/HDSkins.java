package com.minelittlepony.hdskins.forge;

import com.minelittlepony.hdskins.forge.client.HDSkinsClientEvents;
import com.minelittlepony.hdskins.common.skins.SkinServerList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

@Mod(HDSkins.MOD_ID)
public class HDSkins {

    public static final String MOD_ID = "hdskins";

    private static HDSkins instance;

    private SkinServerList skinServers;

    public HDSkins() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public static HDSkins instance() {
        return instance;
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        skinServers = new SkinServerList(FMLPaths.CONFIGDIR.get().resolve(MOD_ID));
        skinServers.tryLoad();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new HDSkinsClientEvents());
    }

    public SkinServerList getSkinServers() {
        return skinServers;
    }
}
