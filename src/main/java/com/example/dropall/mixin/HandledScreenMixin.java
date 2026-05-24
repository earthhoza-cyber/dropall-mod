package com.example.dropall.mixin;

import com.example.dropall.network.DropAllPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected abstract ScreenHandler getScreenHandler();

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void dropall$addButton(CallbackInfo ci) {
        boolean isPlayerInv = (getScreenHandler() instanceof PlayerScreenHandler);

        // Button label depends on context
        Text label = isPlayerInv
                ? Text.literal("Drop All")
                : Text.literal("Drop All");

        int btnWidth  = 70;
        int btnHeight = 18;

        // Place button just below the right edge of the GUI background
        int btnX = this.x + this.backgroundWidth - btnWidth - 2;
        int btnY = this.y + this.backgroundHeight + 2;

        // For player inventory the background height may put it out of view; nudge up
        if (isPlayerInv) {
            btnY = this.y + this.backgroundHeight + 2;
        }

        ButtonWidget button = ButtonWidget.builder(label, btn -> dropall$onDropAll(isPlayerInv))
                .dimensions(btnX, btnY, btnWidth, btnHeight)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                        isPlayerInv
                                ? Text.literal("ทิ้งไอเทมทั้งหมดในกระเป๋า")
                                : Text.literal("ทิ้งไอเทมทั้งหมดในกล่อง/ตู้")))
                .build();

        this.addDrawableChild(button);
    }

    private void dropall$onDropAll(boolean isPlayerInv) {
        // containerOnly = true  → chest/furnace/etc.: drop only the container portion
        // containerOnly = false → player inventory: drop inv slots
        boolean containerOnly = !isPlayerInv;
        ClientPlayNetworking.send(new DropAllPayload(containerOnly));
    }
}
