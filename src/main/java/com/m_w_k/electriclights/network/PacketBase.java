package com.m_w_k.electriclights.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface PacketBase {
    static void handler(PacketBase packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> packet::handleSelf));
        context.get().setPacketHandled(true);

    }

    // static void encoder(packet, buffer) takes in a FriendlyByteBuf and a packet, then writes the packet to the FriendlyByteBuf

    // static PacketBase decoder(buffer) generates a packet out of the FriendlyByteBuf and returns it

    void handleSelf(); // static void handler() does things based on the packet's data

}
