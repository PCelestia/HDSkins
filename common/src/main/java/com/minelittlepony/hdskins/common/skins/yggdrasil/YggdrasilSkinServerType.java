package com.minelittlepony.hdskins.common.skins.yggdrasil;

import com.minelittlepony.hdskins.common.skins.SkinServerType;

import java.lang.reflect.Type;

public class YggdrasilSkinServerType implements SkinServerType {
    @Override
    public String getName() {
        return "mojang";
    }

    @Override
    public Type getType() {
        return YggdrasilSkinServer.class;
    }

}
