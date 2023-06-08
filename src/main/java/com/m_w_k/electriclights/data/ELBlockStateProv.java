package com.m_w_k.electriclights.data;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.block.AbstractRelayBlock;
import com.m_w_k.electriclights.block.BurnOutAbleLightBlock;
import com.m_w_k.electriclights.block.ElectricRelayBlock;
import com.m_w_k.electriclights.registry.ELBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ELBlockStateProv extends BlockStateProvider {
    public ELBlockStateProv(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ElectricLightsMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        this.light(ELBlockRegistry.ELECTRIC_LIGHT, "light");
        this.relay(ELBlockRegistry.ELECTRIC_RELAY, "relay");
        this.relay(ELBlockRegistry.DRAGON_LIGHT, "light");
        this.coil(ELBlockRegistry.VOLTAGE_COIL_L_BLOCK, "low");
    }
    public void light(RegistryObject<Block> registryObject, String variant) {
        Block block = registryObject.get();
        String path = registryObject.getId().getPath();
        String texturePath = "electriclights:block/" + variant + "_";
        List<ModelFile> floor_models = new ArrayList<>();
        List<ModelFile> wall_models = new ArrayList<>();
        List<ModelFile> ceiling_models = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            floor_models.add(this.models().withExistingParent(path.concat("_floor_") + i, texturePath + "floor_blueprint")
                    .texture("1", texturePath.concat("inner_bulb"))
                    .texture("2", texturePath + i)
                    .texture("particle", texturePath + i));
            wall_models.add(this.models().withExistingParent(path.concat("_wall_") + i, texturePath + "wall_blueprint")
                    .texture("1", texturePath.concat("inner_bulb"))
                    .texture("2", texturePath + i)
                    .texture("particle", texturePath + i));
            ceiling_models.add(this.models().withExistingParent(path.concat("_ceiling_") + i, texturePath + "ceiling_blueprint")
                    .texture("1", texturePath.concat("inner_bulb"))
                    .texture("2", texturePath + i)
                    .texture("particle", texturePath + i));
        }

        this.getVariantBuilder(block).forAllStatesExcept(state -> {
            int lightstate = state.getValue(AbstractRelayBlock.LIGHTSTATE);
            boolean burntOut = false;
            if (state.getBlock() instanceof BurnOutAbleLightBlock) burntOut = state.getValue(BurnOutAbleLightBlock.AGE) == 7;
            Direction facing = state.getValue(AbstractRelayBlock.FACING);
            AttachFace face = state.getValue(AbstractRelayBlock.FACE);
            return ConfiguredModel.builder()
                    .modelFile(switch (face) {
                        case FLOOR -> floor_models.get(burntOut ? 0 :lightstate);
                        case CEILING -> ceiling_models.get(burntOut ? 0 :lightstate);
                        case WALL -> wall_models.get(burntOut ? 0 :lightstate);
                    }).rotationY(switch (facing) {
                        case EAST -> 270;
                        case SOUTH -> 0;
                        case WEST -> 90;
                        default -> 180;
                    })
                    .build();
        }, AbstractRelayBlock.WATERLOGGED);

    }
    public void relay(RegistryObject<Block> registryObject, String variant) {
        Block block = registryObject.get();
        String path = registryObject.getId().getPath();
        String texturePath = "electriclights:block/" + variant + "_";
        List<ModelFile> active_models = new ArrayList<>();
        List<ModelFile> inactive_models = new ArrayList<>();
        for (int i = 0; i <= 2; i++) {
            active_models.add(this.models().withExistingParent(path.concat("_floor_") + 1, texturePath + "floor_blueprint")
                    .texture("3", texturePath + 1)
                    .texture("0", texturePath.concat("silicate_board"))
                    .texture("4", texturePath.concat("other_button"))
                    .texture("particle", texturePath.concat("silicate_board")));
            //            wall_models.add(this.models().withExistingParent(path.concat("_wall_"), texturePath + "wall_blueprint")
            //                    .texture("3", texturePath + 1));
            //            ceiling_models.add(this.models().withExistingParent(path.concat("_ceiling_"), texturePath + "ceiling_blueprint")
            //                    .texture("3", texturePath + 1));
            inactive_models.add(this.models().withExistingParent(path.concat("_floor_") + 1, texturePath + "floor_blueprint")
                    .texture("3", texturePath + 1)
                    .texture("0", texturePath.concat("silicate_board"))
                    .texture("4", texturePath.concat("other_button"))
                    .texture("particle", texturePath.concat("silicate_board")));
        }
        this.getVariantBuilder(block).forAllStatesExcept(state -> {
            int lightstate = state.getValue(AbstractRelayBlock.LIGHTSTATE);
            Direction facing = state.getValue(AbstractRelayBlock.FACING);
            AttachFace face = state.getValue(AbstractRelayBlock.FACE);
            Boolean disabled = state.getValue(ElectricRelayBlock.DISABLED);
            return ConfiguredModel.builder()
                    .modelFile(switch (disabled) {
                        case true -> inactive_models.get(burntOut ? 0 :lightstate);
                        case false -> active_models.get(burntOut ? 0 :lightstate);
                    }).rotationY(switch (facing) {
                        case EAST -> 270;
                        case SOUTH -> 0;
                        case WEST -> 90;
                        default -> 180;
                    }).rotationX(switch (face) {
                        case FLOOR -> 0;
                        case CEILING -> 180;
                        case WALL -> 90;
                    })
                    .build();
        }, AbstractRelayBlock.WATERLOGGED);

    }
    public void coil(RegistryObject<Block> registryObject, String identifier) {
        Block block = registryObject.get();
        String path = registryObject.getId().getPath();
        String texturePath = "electriclights:block/coil_" + identifier;
        ModelFile model = this.models().withExistingParent(path, "electriclights:block/coil_base")
                .texture("1", texturePath)
                .texture("particle", texturePath);
        this.getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
    }
}
