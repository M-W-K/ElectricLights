package com.m_w_k.electriclights;

import com.m_w_k.electriclights.gui.screen.AlternatorScreen;
import com.m_w_k.electriclights.gui.screen.GeothermalScreen;
import com.m_w_k.electriclights.gui.screen.SolarScreen;
import com.m_w_k.electriclights.registry.ELItemsRegistry;
import com.m_w_k.electriclights.registry.ELRegistry;
import com.m_w_k.electriclights.util.ELGraphHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.m_w_k.electriclights.registry.ELGUIRegistry.*;


@Mod(ElectricLightsMod.MODID)
public class ElectricLightsMod
{
    public static final String MODID = "electriclights";
    private static final Logger LOGGER = LogUtils.getLogger();

    static DimensionDataStorage overworldDataStorage;

    static final ForgeChunkManager FORGE_CHUNK_MANAGER = null; // I think just having this somewhere is necessary for chunk loading, IDK if that's true, and I can't be bothered to find out
    static final List<ChunkPos> loadedChunks = new ArrayList<>();

    public ElectricLightsMod()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ELConfig.SERVER_SPEC);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ELRegistry.registerThings(modEventBus);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);

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
    void commonSetup(final FMLCommonSetupEvent event) {
        BrewingRecipeRegistry.addRecipe(Ingredient.of(new ItemStack(ELItemsRegistry.REDSTONE_BULB.get())), Ingredient.of(Items.DRAGON_BREATH), new ItemStack(ELItemsRegistry.DRAGON_BULB.get()));
    }


    @SubscribeEvent
    void loadGraphs(ServerStartedEvent event) {
        ELGraphHandler.loadGraphs(event.getServer());
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ALTERNATOR_MENU.get(), AlternatorScreen::new));
        event.enqueueWork(() -> MenuScreens.register(SOLAR_MENU.get(), SolarScreen::new));
        event.enqueueWork(() -> MenuScreens.register(GEOTHERMAL_MENU.get(), GeothermalScreen::new));
    }
    public static void logToConsole(String string) {
        LOGGER.info(string);
    }
    public static void logToConsole(String string, Object... arguments) {
        LOGGER.info(string, arguments);
    }
}
