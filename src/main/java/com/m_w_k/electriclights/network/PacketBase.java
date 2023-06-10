package com.m_w_k.electriclights.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface PacketBase {
    static void handler(PacketBase packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> packet::handleSelf));
        context.get().setPacketHandled(true);

    }

    // static void encoder(...) takes in a FriendlyByteBuf and some other things, then writes those other things to the FriendlyByteBuf

    // static PacketBase decoder(FriendlyByteBuf buff) pulls the things out of the FriendlyByteBuf and returns a new packet based on the data

    void handleSelf(); // takes the decoded packet and updates things

}
