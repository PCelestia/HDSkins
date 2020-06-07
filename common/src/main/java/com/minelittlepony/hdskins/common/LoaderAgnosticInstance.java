package com.minelittlepony.hdskins.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
class LoaderAgnosticInstance {

    private static final String MOD_ID = IHDSkins.MOD_ID;
    private static final Class<?> MOD_CLASS = IHDSkins.class;

    private static Object instance;

    static Object getInstance() {
        if (instance == null) {
            instance = findInstance();
        }

        return instance;
    }

    private static Object findInstance() {

        Throwable forgeException;
        Throwable fabricException;

        // try first with forge. It's the simplest.
        try {
            return findForge();
        } catch (ClassNotFoundException t) {
            forgeException = t;
        }

        // I guess forge isn't installed. Maybe Fabric is being used.
        // Here we go!
        try {
            return findFabric();
        } catch (ClassNotFoundException t) {
            fabricException = t;
        }

        // Something went terribly wrong
        RuntimeException t = new RuntimeException("Could not find instance of '" + IHDSkins.MOD_ID + "'. This probably indicates a bug.");
        t.addSuppressed(forgeException);
        t.addSuppressed(fabricException);
        throw t;
    }


    private static Object findForge() throws ClassNotFoundException {
        Class<?> ModList = Class.forName("net.minecraftforge.fml.ModList");
        // try forge first.
        try {
            Method ModList_get = ModList.getMethod("get");
            Method ModList_getModObjectById = ModList.getMethod("getModObjectById", String.class);

            // ModList modList = ModList.get();
            Object modList = ModList_get.invoke(null);

            // Optional<T> mod = modList.getModObjectById(modId);
            Optional<Object> mod = (Optional<Object>) ModList_getModObjectById.invoke(modList, MOD_ID);

            return mod.orElseThrow(() -> new IllegalStateException("Mod is not loaded?"));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException t) {
            throw new RuntimeException("Failed to load forge mod. Did something internal change?", t);
        }
    }

    private static Object findFabric() throws ClassNotFoundException {
        Class<?> FabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
        try {
            Class<?> ModInitializer = Class.forName("net.fabricmc.api.ModInitializer");
            // requires fabric-loader 0.8+
            Class<?> EntrypointContainer = Class.forName("net.fabricmc.loader.api.entrypoint.EntrypointContainer");
            Class<?> ModContainer = Class.forName("net.fabricmc.loader.api.ModContainer");
            Class<?> ModMetadata = Class.forName("net.fabricmc.loader.api.metadata.ModMetadata");

            Method FabricLoader_getInstance = FabricLoader.getMethod("getInstance");
            Method FabricLoader_getEntrypointContainers = FabricLoader.getMethod("getEntrypointContainers", String.class, Class.class);
            Method EntrypointContainer_getEntrypoint = EntrypointContainer.getMethod("getEntrypoint");
            Method EntrypointContainer_getProvider = EntrypointContainer.getMethod("getProvider");
            Method ModContainer_getMetadata = ModContainer.getMethod("getMetadata");
            Method ModMetadata_getId = ModMetadata.getMethod("getId");

            // FabricLoader loader = FabricLoader.getInstance();
            Object loader = FabricLoader_getInstance.invoke(null);

            // List<EntrypointContainer> containers = loader.getEntrypointContainers("main", ModInitializer.class);
            List<Object> containers = (List<Object>) FabricLoader_getEntrypointContainers.invoke(loader, "main", ModInitializer);

            // for (EntrypointContainer container : containers)
            for (Object container : containers) {
                // ModContainer modContainer = container.getProvider();
                Object modContainer = EntrypointContainer_getProvider.invoke(container);
                // ModMetadata metadata = modContainer.getMetadata();
                Object modMetadata = ModContainer_getMetadata.invoke(modContainer);
                // String id = metadata.getId();
                String id = (String) ModMetadata_getId.invoke(modMetadata);

                if (id.equals(MOD_ID)) {
                    // Object mod = container.getEntrypoint();
                    Object mod = EntrypointContainer_getEntrypoint.invoke(container);
                    if (MOD_CLASS.isInstance(mod)) {
                        return mod;
                    }
                }
            }

            throw new IllegalStateException("Mod with id " + MOD_ID + " did not have a main entrypoint.");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException t) {
            throw new RuntimeException("Failed to load fabric mod. Did something internal change?", t);
        }

    }
}
