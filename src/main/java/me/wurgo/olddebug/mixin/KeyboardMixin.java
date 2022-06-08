package me.wurgo.olddebug.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void debugLog(String key, Object... args);

    @Shadow public abstract void setClipboard(String clipboard);

    @Shadow protected abstract void copyLookAt(boolean hasQueryPermission, boolean queryServer);

    @Shadow private long debugCrashStartTime;

    @Shadow protected abstract void debugLog(Text text);

    /**
     * @author Wurgo
     */
    @Overwrite
    private boolean processF3(int key) {
        if (this.debugCrashStartTime > 0L && this.debugCrashStartTime < Util.getMeasuringTimeMs() - 100L) {
            return true;
        } else {
            switch(key) {
                case 65:
                    this.client.worldRenderer.reload();
                    this.debugLog("debug.reload_chunks.message");
                    return true;
                case 66:
                    boolean bl = !this.client.getEntityRenderDispatcher().shouldRenderHitboxes();
                    this.client.getEntityRenderDispatcher().setRenderHitboxes(bl);
                    this.debugLog(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
                    return true;
                case 67:
                    if (this.client.player != null && this.client.player.hasReducedDebugInfo()) {
                        return false;
                    } else {
                        ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.player.networkHandler;
                        if (clientPlayNetworkHandler == null) {
                            return false;
                        }

                        this.debugLog("debug.copy_location.message");
                        this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.client.player.world.getRegistryKey().getValue(), this.client.player.getX(), this.client.player.getY(), this.client.player.getZ(), this.client.player.getYaw(), this.client.player.getPitch()));
                        return true;
                    }
                case 68:
                    if (this.client.inGameHud != null) {
                        this.client.inGameHud.getChatHud().clear(false);
                    }

                    return true;
                case 70:
                    this.client.options.getViewDistance().setValue(MathHelper.clamp((this.client.options.getViewDistance().getValue() + (Screen.hasShiftDown() ? -1 : 1)), 2, 32));
                    this.debugLog(Text.literal(String.format("Render Distance: %s", this.client.options.getViewDistance().getValue())));
                    return true;
                case 71:
                    boolean bl2 = this.client.debugRenderer.toggleShowChunkBorder();
                    this.debugLog(bl2 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
                    return true;
                case 72:
                    this.client.options.advancedItemTooltips = !this.client.options.advancedItemTooltips;
                    this.debugLog(this.client.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
                    this.client.options.write();
                    return true;
                case 73:
                    if (this.client.player != null && !this.client.player.hasReducedDebugInfo()) {
                        this.copyLookAt(this.client.player.hasPermissionLevel(2), !Screen.hasShiftDown());
                    }

                    return true;
                case 78:
                    if (this.client.player != null && !this.client.player.hasPermissionLevel(2)) {
                        this.debugLog("debug.creative_spectator.error");
                    } else if (!this.client.player.isSpectator()) {
                        this.client.player.sendChatMessage("/gamemode spectator");
                    } else {
                        if (this.client.interactionManager != null) {
                            GameMode previousGameMode = this.client.interactionManager.getPreviousGameMode();
                            if (previousGameMode != null) {
                                this.client.player.sendChatMessage("/gamemode " + this.client.interactionManager.getPreviousGameMode().getName());
                            }
                        }
                    }

                    return true;
                case 80:
                    this.client.options.pauseOnLostFocus = !this.client.options.pauseOnLostFocus;
                    this.client.options.write();
                    this.debugLog(this.client.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
                    return true;
                case 81:
                    this.debugLog("debug.help.message");
                    ChatHud chatHud = this.client.inGameHud.getChatHud();
                    chatHud.addMessage(Text.translatable("debug.reload_chunks.help"));
                    chatHud.addMessage(Text.translatable("debug.show_hitboxes.help"));
                    chatHud.addMessage(Text.translatable("debug.copy_location.help"));
                    chatHud.addMessage(Text.translatable("debug.clear_chat.help"));
                    chatHud.addMessage(Text.literal("F3 + F = Cycle render distance (Shift to invert)"));
                    chatHud.addMessage(Text.translatable("debug.chunk_boundaries.help"));
                    chatHud.addMessage(Text.translatable("debug.advanced_tooltips.help"));
                    chatHud.addMessage(Text.translatable("debug.inspect.help"));
                    chatHud.addMessage(Text.translatable("debug.creative_spectator.help"));
                    chatHud.addMessage(Text.translatable("debug.pause_focus.help"));
                    chatHud.addMessage(Text.translatable("debug.help.help"));
                    chatHud.addMessage(Text.translatable("debug.reload_resourcepacks.help"));
                    chatHud.addMessage(Text.translatable("debug.pause.help"));
                    chatHud.addMessage(Text.translatable("debug.gamemodes.help"));
                    return true;
                case 84:
                    this.debugLog("debug.reload_resourcepacks.message");
                    this.client.reloadResources();
                    return true;
                case 293:
                    if (this.client.player != null && !this.client.player.hasPermissionLevel(2)) {
                        this.debugLog("debug.gamemodes.error");
                    } else {
                        this.client.setScreen(new GameModeSelectionScreen());
                    }

                    return true;
                default:
                    return false;
            }
        }
    }
}
