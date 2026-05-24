package com.example.dropall;

import com.example.dropall.network.DropAllPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropAllMod implements ModInitializer {

    public static final String MOD_ID = "dropall";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register C2S payload type
        PayloadTypeRegistry.playC2S().register(DropAllPayload.ID, DropAllPayload.CODEC);

        // Handle packet on server
        ServerPlayNetworking.registerGlobalReceiver(DropAllPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                boolean containerOnly = payload.dropOnlyContainer();
                dropAll(player, containerOnly);
            });
        });

        LOGGER.info("[DropAll] Mod initialized.");
    }

    /**
     * Drops items from the player's current screen handler.
     *
     * @param player         the server player
     * @param containerOnly  if true, only drop the container portion
     *                       (slots before the player inventory); if false, drop everything
     */
    private void dropAll(ServerPlayerEntity player, boolean containerOnly) {
        var handler = player.currentScreenHandler;
        int totalSlots = handler.slots.size();

        // For non-player-inventory screens the last 36 slots are always
        // the player's own inventory (27 main + 9 hotbar).
        // For PlayerScreenHandler the layout is different (crafting + armor + inv).
        boolean isPlayerInv = (handler instanceof PlayerScreenHandler);

        int startSlot;
        int endSlot;

        if (isPlayerInv) {
            // Player inventory screen: drop slots 9–44 (main inv + hotbar),
            // skip crafting (0-4) and armour (5-8) and offhand (45).
            startSlot = 9;
            endSlot = 44;
        } else if (containerOnly) {
            // Container screen: drop only the container part
            startSlot = 0;
            endSlot = totalSlots - 37; // exclude 36 player slots + 1-based
        } else {
            // Drop absolutely everything in the handler
            startSlot = 0;
            endSlot = totalSlots - 1;
        }

        // Clamp to valid range
        startSlot = Math.max(0, startSlot);
        endSlot   = Math.min(totalSlots - 1, endSlot);

        for (int i = startSlot; i <= endSlot; i++) {
            Slot slot = handler.slots.get(i);
            var stack = slot.getStack();
            if (!stack.isEmpty() && slot.canTakeItems(player)) {
                slot.setStack(net.minecraft.item.ItemStack.EMPTY);
                // true = pickup delay, false = no velocity randomisation
                player.dropItem(stack, false, true);
            }
        }

        // Sync the handler so the client UI refreshes
        handler.sendContentUpdates();
    }
}
