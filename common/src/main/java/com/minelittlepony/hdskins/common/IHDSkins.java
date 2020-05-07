package com.minelittlepony.hdskins.common;

import com.minelittlepony.hdskins.common.skins.SkinServerList;

public interface IHDSkins {
    String MOD_ID = "hdskins";

    static IHDSkins instance() {
        return LoaderAgnosticInstance.getInstance(MOD_ID, IHDSkins.class);
    }

    SkinServerList getSkinServers();
}
