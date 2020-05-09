package com.minelittlepony.hdskins.fabric.client.modmenu;

import com.minelittlepony.hdskins.common.IHDSkins;
import com.minelittlepony.hdskins.fabric.client.HDSkinsClient;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class HDSkinsMenuFactory implements ModMenuApi {

    @Override
    public String getModId() {
        return IHDSkins.MOD_ID;
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return HDSkinsClient::createSkinUpload;
    }
}
