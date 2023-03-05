package com.m_w_k.electriclights;

import com.m_w_k.electriclights.block.AlternatorBlock;
import com.m_w_k.electriclights.block.ElectricRelayBlock;
import com.m_w_k.electriclights.block.MasterSwitchboardBlock;
import com.m_w_k.electriclights.block.VoltageBlock;
import com.m_w_k.electriclights.blockentity.AlternatorBlockEntity;
import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import com.m_w_k.electriclights.item.RedstoneBulbItem;
import com.m_w_k.electriclights.item.RedstoneSilicateItem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Mod(ElectricLightsMod.MODID)
public class ElectricLightsMod
{
    public static final String MODID = "electriclights";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<Block> ELECTRIC_LIGHT = BLOCKS.register("electric_light", () -> new ElectricRelayBlock(BlockBehaviour.Properties.of(Material.DECORATION), true));
    public static final RegistryObject<Block> ELECTRIC_RELAY = BLOCKS.register("electric_relay", () -> new ElectricRelayBlock(BlockBehaviour.Properties.of(Material.DECORATION)));
    public static final RegistryObject<Block> SWITCHBOARD_BLOCK = BLOCKS.register("master_switchboard", () -> new MasterSwitchboardBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
    public static final RegistryObject<Block> ALTERNATOR_BLOCK = BLOCKS.register("alternator_generator", () -> new AlternatorBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
    public static final RegistryObject<Block> VOLTAGE_COIL_L_BLOCK = BLOCKS.register("voltage_coil_l", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL), 2));
    public static final RegistryObject<Block> VOLTAGE_COIL_M_BLOCK = BLOCKS.register("voltage_coil_m", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL), 3));
    public static final RegistryObject<Block> VOLTAGE_COIL_H_BLOCK = BLOCKS.register("voltage_coil_h", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL), 4));

    public static final RegistryObject<Item> ELECTRIC_LIGHT_BLOCK_ITEM = ITEMS.register("electric_light", () -> new BlockItem(ELECTRIC_LIGHT.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELECTRIC_RELAY_BLOCK_ITEM = ITEMS.register("electric_relay", () -> new BlockItem(ELECTRIC_RELAY.get(), new Item.Properties()));
    public static final RegistryObject<Item> SWITCHBOARD_BLOCK_ITEM = ITEMS.register("master_switchboard", () -> new BlockItem(SWITCHBOARD_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> ALTERNATOR_BLOCK_ITEM = ITEMS.register("alternator_generator", () -> new BlockItem(ALTERNATOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_L_BLOCK_ITEM = ITEMS.register("voltage_coil_l", () -> new BlockItem(VOLTAGE_COIL_L_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_M_BLOCK_ITEM = ITEMS.register("voltage_coil_m", () -> new BlockItem(VOLTAGE_COIL_M_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_H_BLOCK_ITEM = ITEMS.register("voltage_coil_h", () -> new BlockItem(VOLTAGE_COIL_H_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> REDSTONE_SILICATE = ITEMS.register("redstone_silicate", RedstoneSilicateItem::new);
    public static final RegistryObject<Item> REDSTONE_BULB = ITEMS.register("redstone_bulb", RedstoneBulbItem::new);
    
    public static final RegistryObject<BlockEntityType<MasterSwitchboardBlockEntity>> MASTER_SWITCHBOARD = BLOCK_ENTITIES.register("master_switchboard", () -> BlockEntityType.Builder.of(MasterSwitchboardBlockEntity::new, SWITCHBOARD_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<AlternatorBlockEntity>> ALTERNATOR_GENERATOR = BLOCK_ENTITIES.register("alternator_generator", () -> BlockEntityType.Builder.of(AlternatorBlockEntity::new, ALTERNATOR_BLOCK.get()).build(null));

    public static final RegistryObject<MenuType<AlternatorMenu>> ALTERNATOR_MENU = MENU_TYPES.register("alternator_generator", () -> new MenuType<>(AlternatorMenu::new));

    static DimensionDataStorage overworldDataStorage;
    static ElectricLightsGraph electricLightsGraph = ElectricLightsGraph.create();

    public static final String SWITCHBOARD_STRING = "SWITCHBOARD";
    public static final String GENERATOR_STRING = "GENERATOR";

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

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
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

    private void addCreative(CreativeModeTabEvent.BuildContents event)
    {
        if (event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ELECTRIC_LIGHT_BLOCK_ITEM);
            event.accept(ELECTRIC_RELAY_BLOCK_ITEM);
            event.accept(REDSTONE_SILICATE);
            event.accept(REDSTONE_BULB);
            event.accept(SWITCHBOARD_BLOCK_ITEM);
            event.accept(VOLTAGE_COIL_L_BLOCK_ITEM);
            event.accept(VOLTAGE_COIL_M_BLOCK_ITEM);
            event.accept(VOLTAGE_COIL_H_BLOCK_ITEM);
            event.accept(ALTERNATOR_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    void getLevelStorage(ServerStartedEvent event) {
        overworldDataStorage = event.getServer().overworld().getDataStorage();
        electricLightsGraph = electricLightsGraph.recallFromStorage(overworldDataStorage);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ALTERNATOR_MENU.get(), AlternatorScreen::new));
    }

    public static void addGraphNodeAndAutoConnect(GraphNode node, Level level) {
        GraphNode[] graphNodes = electricLightsGraph.getNodes();
        electricLightsGraph.addNode(node);
        for (GraphNode graphNode : graphNodes) {
            if (!node.toString().equals(graphNode.toString())) {
                BlockPos pos1 = node.getPos();
                BlockPos pos2 = graphNode.getPos();
                if (pos1.distSqr(pos2) <= NODE_CONNECT_DIST_SQR) {
                    electricLightsGraph.addConnection(node, graphNode);
                }
            }
        }
        electricLightsGraph.refreshSwitchboards(level);
        ElectricLightsMod.markGraphForSaving();
    }
    public static void removeGraphNode(GraphNode node, Level level) {
        electricLightsGraph.removeNode(node);
        electricLightsGraph.refreshSwitchboards(level);
        ElectricLightsMod.markGraphForSaving();
    }
    public static List<GraphNode> getSwitchboards() {
        return electricLightsGraph.getSwitchboards();
    }
    public static List<GraphNode> getGenerators() {
        return electricLightsGraph.getGenerators();
    }
    public static Set<GraphNode> getConnectedNodes(GraphNode node) {
        return electricLightsGraph.getConnectedNodes(node);
    }
    public static boolean nodeExists(GraphNode node) {
        return electricLightsGraph.nodeExists(node);
    }
    public static void markGraphForSaving() {
        electricLightsGraph.setDirty();
    }
    public static void logToConsole(String string) {
        LOGGER.info(string);
    }
}
