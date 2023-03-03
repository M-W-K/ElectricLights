package com.m_w_k.electriclights;

import com.m_w_k.electriclights.block.ElectricRelayBlock;
import com.m_w_k.electriclights.block.MasterSwitchboardBlock;
import com.m_w_k.electriclights.block.VoltageBlock;
import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import com.m_w_k.electriclights.item.RedstoneBulbItem;
import com.m_w_k.electriclights.item.RedstoneSilicateItem;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

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
    

    public static final RegistryObject<Block> ELECTRIC_LIGHT = BLOCKS.register("electric_light", () -> new ElectricRelayBlock(BlockBehaviour.Properties.of(Material.STONE), true));
    public static final RegistryObject<Block> ELECTRIC_RELAY = BLOCKS.register("electric_relay", () -> new ElectricRelayBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Block> SWITCHBOARD_BLOCK = BLOCKS.register("master_switchboard", () -> new MasterSwitchboardBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Block> VOLTAGE_COIL_L_BLOCK = BLOCKS.register("voltage_coil_l", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.STONE), 2));
    public static final RegistryObject<Block> VOLTAGE_COIL_M_BLOCK = BLOCKS.register("voltage_coil_m", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.STONE), 3));
    public static final RegistryObject<Block> VOLTAGE_COIL_H_BLOCK = BLOCKS.register("voltage_coil_h", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.STONE), 4));

    public static final RegistryObject<Item> ELECTRIC_LIGHT_BLOCK_ITEM = ITEMS.register("electric_light", () -> new BlockItem(ELECTRIC_LIGHT.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELECTRIC_RELAY_BLOCK_ITEM = ITEMS.register("electric_relay", () -> new BlockItem(ELECTRIC_RELAY.get(), new Item.Properties()));
    public static final RegistryObject<Item> SWITCHBOARD_BLOCK_ITEM = ITEMS.register("master_switchboard", () -> new BlockItem(SWITCHBOARD_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_L_BLOCK_ITEM = ITEMS.register("voltage_coil_l", () -> new BlockItem(VOLTAGE_COIL_L_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_M_BLOCK_ITEM = ITEMS.register("voltage_coil_m", () -> new BlockItem(VOLTAGE_COIL_M_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_H_BLOCK_ITEM = ITEMS.register("voltage_coil_h", () -> new BlockItem(VOLTAGE_COIL_H_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> REDSTONE_SILICATE = ITEMS.register("redstone_silicate", RedstoneSilicateItem::new);
    public static final RegistryObject<Item> REDSTONE_BULB = ITEMS.register("redstone_bulb", RedstoneBulbItem::new);
    
    public static final RegistryObject<BlockEntityType<MasterSwitchboardBlockEntity>> MASTER_SWITCHBOARD = BLOCK_ENTITIES.register("master_switchboard", () -> BlockEntityType.Builder.of(MasterSwitchboardBlockEntity::new, SWITCHBOARD_BLOCK.get()).build(null));

    static DimensionDataStorage overworldDataStorage;
    static ElectricLightsGraph electricLightsGraph = ElectricLightsGraph.create();

    public static final int NODE_CONNECT_DIST_SQR = 16 * 16;
    public static final int MINIMUM_SWITCHBOARD_UPDATE_INTERVAL = 10;

    public ElectricLightsMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
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
        }
    }

    @SubscribeEvent
    void getLevelStorage(ServerStartedEvent event) {
        overworldDataStorage = event.getServer().overworld().getDataStorage();
        electricLightsGraph = electricLightsGraph.recallFromStorage(overworldDataStorage);
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
