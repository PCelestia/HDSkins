package com.minelittlepony.hdskins.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
class LoaderAgnosticInstance {

    private static Object instance;

    static <T> T getInstance(String modId, Class<T> type) {
        if (instance == null) {
            instance = findInstance(modId, type);
        }

        return (T) instance;
    }

    private static Object findInstance(String modId, Class<?> type) {

        Throwable forgeException;
        Throwable fabricException;

        // try forge first.
        try {
            Class<?> ModList = Class.forName("net.minecraftforge.fml.ModList");
            Method ModList_get = ModList.getMethod("get");
            Method ModList_getModObjectById = ModList.getMethod("getModObjectById", String.class);

            // ModList modList = ModList.get();
            Object modList = ModList_get.invoke(null);

            // Optional<T> mod = modList.getModObjectById(modId);
            Optional<Object> mod = (Optional<Object>) ModList_getModObjectById.invoke(modList, modId);

            return mod.orElseThrow(() -> new IllegalStateException("Mod is not loaded?"));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException t) {
            forgeException = t;
        }

        // I guess forge isn't installed. Maybe Fabric is being used.
        // Here we go!
        try {
            Class<?> FabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Class<?> ModInitializer = Class.forName("net.fabricmc.api.ModInitializer");
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

                if (id.equals(modId)) {
                    // Object mod = container.getEntrypoint();
                    Object mod = EntrypointContainer_getEntrypoint.invoke(container);
                    if (type.isInstance(mod)) {
                        return mod;
                    }
                }
            }

            throw new IllegalStateException("Mod with id " + modId + " did not have a main entrypoint.");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException t) {
            fabricException = t;
        }

        // Something went terribly wrong

        RuntimeException t = new RuntimeException("Could not find instance of '" + modId + "'. This probably indicates a bug.");
        t.addSuppressed(forgeException);
        t.addSuppressed(fabricException);
        throw t;
    }
}