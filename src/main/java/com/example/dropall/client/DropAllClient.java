package com.example.dropall.client;

import com.example.dropall.network.DropAllPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class DropAllClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register the payload type on the client side as well
        PayloadTypeRegistry.playC2S().register(DropAllPayload.ID, DropAllPayload.CODEC);
    }
}
