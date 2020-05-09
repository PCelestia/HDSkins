package com.minelittlepony.hdskins.fabric;

import com.minelittlepony.hdskins.common.ObfHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Type;

public class FabricObfHelper<Owner, Descriptor> extends ObfHelper<Owner, Descriptor> {
    private static final MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();

    public FabricObfHelper(Class<Owner> owner, String name, Class<?> type) {
        super(owner, name, type);
    }

    @Override
    protected String mapFieldName() {
        Class<?> desc = this.desc;
        String intermediateOwner = unmapClass(owner);
        String intermediateType = unmapClass(desc);
        String intermediateDesc = Type.getObjectType(intermediateType).getDescriptor().replace('.', '/');

        return mappings.mapFieldName("intermediary", intermediateOwner, name, intermediateDesc);
    }

    private static String unmapClass(Class<?> cls) {
        return mappings.unmapClassName("intermediary", cls.getName());
    }
}
