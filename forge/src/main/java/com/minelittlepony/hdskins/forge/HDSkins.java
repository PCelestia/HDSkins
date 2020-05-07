package com.minelittlepony.hdskins.forge;

import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.common.skins.SkinServerList;
import com.minelittlepony.hdskins.forge.client.HDSkinsClientEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(IHDSkins.MOD_ID)
public class HDSkins implements IHDSkins {

    private SkinServerList skinServers;

    public HDSkins() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
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
