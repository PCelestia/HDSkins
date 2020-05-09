package com.minelittlepony.hdskins.forge;

import com.minelittlepony.hdskins.common.ObfHelper;
import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ForgeObfHelper<Owner, Desc> extends ObfHelper<Owner, Desc> {
    public ForgeObfHelper(Class<Owner> owner, String name, Class<?> desc) {
        super(owner, name, desc);
    }

    @Override
    protected String mapFieldName() {
        return ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, name);
    }
}
