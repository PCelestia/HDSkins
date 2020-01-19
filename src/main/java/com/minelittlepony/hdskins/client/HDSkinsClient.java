package com.minelittlepony.hdskins.client;

import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.event.ScreenInitCallback;
import com.minelittlepony.common.util.GamePaths;
import com.minelittlepony.hdskins.HDSkins;
import com.minelittlepony.hdskins.client.dummy.DummyPlayer;
import com.minelittlepony.hdskins.client.dummy.DummyPlayerRenderer;
import com.minelittlepony.hdskins.client.dummy.EquipmentList;
import com.minelittlepony.hdskins.client.gui.GuiSkins;
import com.minelittlepony.hdskins.client.resources.SkinResourceManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceType;

public final class HDSkinsClient implements ClientModInitializer {

    private static HDSkinsClient instance;

    public static HDSkinsClient getInstance() {
        return instance;
    }

    private final HDConfig config = new HDConfig(GamePaths.getConfigDirectory().resolve("hdskins.json"));

    private final EquipmentList equipmentList = new EquipmentList();
    private final SkinResourceManager resources = new SkinResourceManager();
    private final ProfileRepository repository = new ProfileRepository();

    public HDSkinsClient() {
        instance = this;
    }

    public HDConfig getConfig() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        config.load();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(resources);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(equipmentList);
        EntityRendererRegistry.INSTANCE.register(DummyPlayer.TYPE, DummyPlayerRenderer::new);
        ScreenInitCallback.EVENT.register(this::onScreenInit);

        FabricLoader.getInstance().getEntrypoints("hdskins", ClientModInitializer.class).forEach(ClientModInitializer::onInitializeClient);
    }

    private void onScreenInit(Screen screen, ScreenInitCallback.ButtonList buttons) {
        if (screen instanceof TitleScreen) {
            Button button = buttons.add(new Button(screen.width - 50, screen.height - 50, 20, 20))
                .onClick(sender -> MinecraftClient.getInstance().openScreen(GuiSkins.create(screen, HDSkins.getInstance().getSkinServerList())));
            button.getStyle()
                .setIcon(new ItemStack(Items.LEATHER_LEGGINGS), 0x3c5dcb);
            button.y = screen.height - 50; // ModMenu;
        }
    }

    public SkinResourceManager getResourceManager() {
        return resources;
    }

    public ProfileRepository getProfileRepository() {
        return repository;
    }

    public EquipmentList getDummyPlayerEquipmentList() {
        return equipmentList;
    }
}