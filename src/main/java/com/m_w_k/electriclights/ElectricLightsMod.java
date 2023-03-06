package com.m_w_k.electriclights;

import com.m_w_k.electriclights.gui.AlternatorScreen;
import com.m_w_k.electriclights.registry.ELRegistry;
import com.m_w_k.electriclights.util.ELGraphHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.m_w_k.electriclights.registry.ELGUIRegistry.ALTERNATOR_MENU;


@Mod(ElectricLightsMod.MODID)
public class ElectricLightsMod
{
    public static final String MODID = "electriclights";
    private static final Logger LOGGER = LogUtils.getLogger();

    static DimensionDataStorage overworldDataStorage;

    public static final int NODE_CONNECT_DIST_SQR = 16 * 16;
    public static final int MINIMUM_SWITCHBOARD_UPDATE_INTERVAL = 10;
    public static final int ALTERNATOR_ENERGY_FACTOR = 156;
    public static final int SOLAR_ENERGY_FACTOR = 72;
    public static final int GEOTHERMAL_ENERGY_FACTOR = 48;

    static final ForgeChunkManager FORGE_CHUNK_MANAGER = null; // I think just having this somewhere is necessary for chunk loading, IDK if that's true, and I can't be bothered to find out
    static final List<ChunkPos> loadedChunks = new ArrayList<>();

    public ElectricLightsMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ELRegistry.registerThings(modEventBus);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

    }

    public static void manageLoadedChunks(ServerLevel level, BlockPos blockPos, boolean add) {
        if (!level.isClientSide) {
            ChunkPos chunkPos = level.getChunk(blockPos).getPos();
            if (add && !loadedChunks.contains(chunkPos)) {
                if (ForgeChunkManager.forceChunk(level, MODID, blockPos, chunkPos.x, chunkPos.z, true, false))
                    loadedChunks.add(chunkPos);
            } else if (!add && loadedChunks.contains(chunkPos))
                    if (ForgeChunkManager.forceChunk(level, MODID, blockPos, chunkPos.x, chunkPos.z, false, false))
                        loadedChunks.remove(chunkPos);
        }
    }



    @SubscribeEvent
    void loadGraphs(ServerStartedEvent event) {
        ELGraphHandler.loadGraphs(event.getServer());
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ALTERNATOR_MENU.get(), AlternatorScreen::new));
    }
    public static void logToConsole(String string) {
        LOGGER.info(string);
    }
}
