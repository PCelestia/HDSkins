package com.minelittlepony.hdskins.common;

import com.minelittlepony.hdskins.common.skins.SkinServerList;

public interface IHDSkins {
    String MOD_ID = "hdskins";

    static IHDSkins instance() {
        return (IHDSkins) LoaderAgnosticInstance.getInstance();
    }

    SkinServerList getSkinServers();
}
