package me.falu.olddebug.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow protected abstract void debugLog(Text text);

    @Inject(method = "processF3", at = @At("TAIL"), cancellable = true)
    private void renderDistance(int key, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && key == GLFW.GLFW_KEY_F) {
            this.client.options.getViewDistance().setValue(MathHelper.clamp((this.client.options.getViewDistance().getValue() + (Screen.hasShiftDown() ? -1 : 1)), 2, 32));
            this.debugLog(Text.literal(String.format("Render Distance: %s", this.client.options.getViewDistance().getValue())));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V", ordinal = 3, shift = At.Shift.AFTER))
    private void addHelp(int key, CallbackInfoReturnable<Boolean> cir) {
        this.client.inGameHud.getChatHud().addMessage(Text.literal("F3 + F = Cycle render distance (Shift to invert)"));
    }
}
