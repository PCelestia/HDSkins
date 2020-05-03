package com.minelittlepony.hdskins.common.skins.valhalla;

import com.minelittlepony.hdskins.common.skins.SkinServerType;

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
