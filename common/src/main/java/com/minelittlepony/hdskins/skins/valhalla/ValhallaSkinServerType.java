package com.minelittlepony.hdskins.skins.valhalla;

import com.minelittlepony.hdskins.skins.SkinServerType;

import java.lang.reflect.Type;

public class ValhallaSkinServerType implements SkinServerType {
    @Override
    public String getName() {
        return "valhalla";
    }

    @Override
    public Type getType() {
        return ValhallaSkinServer.class;
    }

}
