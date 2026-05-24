package com.example.dropall.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S packet: tells the server to drop all items
 * from the player's currently open screen handler.
 *
 * dropOnlyContainer = true  → drop only the container slots (chest, etc.)
 * dropOnlyContainer = false → drop all slots including player inventory
 */
public record DropAllPayload(boolean dropOnlyContainer) implements CustomPayload {

    public static final CustomPayload.Id<DropAllPayload> ID =
            new CustomPayload.Id<>(Identifier.of("dropall", "drop_all"));

    public static final PacketCodec<PacketByteBuf, DropAllPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeBoolean(value.dropOnlyContainer()),
                    buf -> new DropAllPayload(buf.readBoolean())
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
