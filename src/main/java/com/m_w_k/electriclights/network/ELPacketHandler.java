package com.m_w_k.electriclights.network;

import com.m_w_k.electriclights.ElectricLightsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ELPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ElectricLightsMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        register(SwitchboardHumPacket.class, SwitchboardHumPacket::encoder, SwitchboardHumPacket::decoder, PacketBase::handler);
    }

    private static <MSG> void register(Class<MSG> packet, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        INSTANCE.registerMessage(id, packet, encoder, decoder, messageConsumer);
        id++;
    }


    /**
     * Server -> Client
     */
    public static void sendToClient(Object packet, ServerPlayer serverPlayer) {
        if (!(serverPlayer instanceof FakePlayer))
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
    }

    /**
     * Server -> Clients within a radius of a point
     */
    public static void sendToNearClients(Object packet, BlockPos pos, int r, Level level) {
        INSTANCE.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), r, level.dimension())), packet);
    }

    /**
     * Client -> Server
     */
    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

}
