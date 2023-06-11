package com.m_w_k.electriclights.network;

import com.m_w_k.electriclights.client.SwitchboardHumHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class SwitchboardHumPacket implements PacketBase {
    private final BlockPos pos;
    private final Boolean playSound;

    public SwitchboardHumPacket(BlockPos pos, Boolean playSound) {
        this.pos = pos;
        this.playSound = playSound;
    }

    static void encoder(SwitchboardHumPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.playSound);
    }

    static SwitchboardHumPacket decoder(FriendlyByteBuf buffer) {
        return new SwitchboardHumPacket(buffer.readBlockPos(), buffer.readBoolean());
    }

    @Override
    public void handleSelf() {
        if (!SwitchboardHumHandler.isSetup()) SwitchboardHumHandler.setupHandler();
        if (playSound) SwitchboardHumHandler.startHum(pos);
        else SwitchboardHumHandler.stopHum(pos);
    }
}
