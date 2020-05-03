package com.minelittlepony.hdskins.skins.yggdrasil;

import com.minelittlepony.hdskins.skins.SkinServerType;

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
