package com.m_w_k.electriclights.data;

import com.m_w_k.electriclights.ElectricLightsMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = ElectricLightsMod.MODID, bus = Bus.MOD)
public class ELDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        boolean server = event.includeServer();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        gen.addProvider(server, new ELLootProv(gen));
        gen.addProvider(server, new ELBlockStateProv(gen.getPackOutput(), fileHelper));
        gen.addProvider(server, new ELItemModelProv(gen.getPackOutput(), fileHelper));
    }
}
