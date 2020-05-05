package com.minelittlepony.hdskins.fabric.mixin.client;

import com.minelittlepony.hdskins.fabric.client.callbacks.InitScreenCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen {

    @Shadow
    protected abstract <T extends AbstractButtonWidget> T addButton(T button);

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("RETURN"))
    private void onOpenScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
        InitScreenCallback.EVENT.invoker().onScreenInit((Screen) (Object) this, this::addButton);
    }
}
