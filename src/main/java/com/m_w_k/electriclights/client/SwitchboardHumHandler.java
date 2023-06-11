package com.m_w_k.electriclights.client;

import com.m_w_k.electriclights.registry.ELSoundsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

public class SwitchboardHumHandler {
    private static LocalPlayer player;
    private static SoundManager soundManager;
    private static Map<BlockPos, SwitchboardHum> soundInstances = new HashMap<>();
    private static boolean setup = false;

    public static boolean isSetup() {
        return setup;
    }

    public static void setupHandler() {
        SwitchboardHumHandler.player = Minecraft.getInstance().player;
        SwitchboardHumHandler.soundManager = Minecraft.getInstance().getSoundManager();
        setup = true;
    }

    public static void startHum(BlockPos pos) {
        SwitchboardHum hum = new SwitchboardHum(player, pos);
        if (!soundInstances.containsKey(pos)) {
            soundManager.play(hum);
            soundInstances.put(pos, hum);
        }
    }

    public static void stopHum(BlockPos pos) {
        SwitchboardHum hum = soundInstances.get(pos);
        if (hum != null) {
            soundManager.stop(hum);
            soundInstances.remove(pos);
        }
    }

    private static class SwitchboardHum extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        protected SwitchboardHum(LocalPlayer player, BlockPos pos) {
            super(ELSoundsRegistry.SWITCHBOARD_ACTIVE, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0F;
            this.pitch = 2.0F;

            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }

        @Override
        public void tick() {
            if (this.player.isRemoved() || this.player.position().distanceToSqr(x,y,z) >= 256) {
                SwitchboardHumHandler.stopHum(new BlockPos(x,y,z));
            }
        }
    }
}
