package com.m_w_k.electriclights.registry;

import com.m_w_k.electriclights.ElectricLightsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ELSoundsRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ElectricLightsMod.MODID);

    public static final SoundEvent BREAKER_ON = create("block.breaker.on");
    public static final SoundEvent BREAKER_OFF = create("block.breaker.off");
    public static final SoundEvent SWITCHBOARD_ON = create("block.switchboard.on");
    public static final SoundEvent SWITCHBOARD_OFF = create("block.switchboard.off");
    public static final SoundEvent SWITCHBOARD_ACTIVE = create("block.switchboard.active");

    private static SoundEvent create(String name)
    {
        ResourceLocation location = new ResourceLocation(ElectricLightsMod.MODID, name);
        SoundEvent sound = SoundEvent.createVariableRangeEvent(location);
        ForgeRegistries.SOUND_EVENTS.register(location, sound);
        return sound;
    }

}
