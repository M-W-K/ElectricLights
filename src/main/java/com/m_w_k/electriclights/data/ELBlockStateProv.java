package com.m_w_k.electriclights.data;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.block.*;
import com.m_w_k.electriclights.registry.ELBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
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
        this.light(ELBlockRegistry.DRAGON_LIGHT, "light");
        this.relay(ELBlockRegistry.ELECTRIC_RELAY, "relay");
        this.coil(ELBlockRegistry.VOLTAGE_COIL_L_BLOCK, "low");
        this.coil(ELBlockRegistry.VOLTAGE_COIL_M_BLOCK, "med");
        this.coil(ELBlockRegistry.VOLTAGE_COIL_H_BLOCK, "high");
        this.extendableGeneratorAndExtension(ELBlockRegistry.SOLAR_BLOCK, ELBlockRegistry.SOLAR_EXTENSION_BLOCK, "solar");
        this.switchboard();
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
                    .texture("2", texturePath + i)
                    .texture("particle", texturePath + i));
            wall_models.add(this.models().withExistingParent(path.concat("_wall_") + i, texturePath + "wall_blueprint")
                    .texture("2", texturePath + i)
                    .texture("particle", texturePath + i));
            ceiling_models.add(this.models().withExistingParent(path.concat("_ceiling_") + i, texturePath + "ceiling_blueprint")
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
            active_models.add(this.models().withExistingParent(path.concat("_active_") + i, texturePath + "active_blueprint")
                    .texture("3", texturePath + i)
                    .texture("0", texturePath.concat("silicate_board"))
                    .texture("4", texturePath.concat("other_button"))
                    .texture("particle", texturePath.concat("silicate_board")));
            inactive_models.add(this.models().withExistingParent(path.concat("_inactive_") + i, texturePath + "inactive_blueprint")
                    .texture("3", texturePath + 0)
                    .texture("0", texturePath.concat("silicate_board"))
                    .texture("4", texturePath.concat("other_button_off"))
                    .texture("particle", texturePath.concat("silicate_board")));
        }
        this.getVariantBuilder(block).forAllStatesExcept(state -> {
            int lightstate = state.getValue(AbstractRelayBlock.LIGHTSTATE);
            lightstate = Math.min(lightstate, 2);
            Direction facing = state.getValue(AbstractRelayBlock.FACING);
            AttachFace face = state.getValue(AbstractRelayBlock.FACE);
            boolean wall = face == AttachFace.WALL;
            boolean disabled = state.getValue(ElectricRelayBlock.DISABLED);
            return ConfiguredModel.builder()
                    .modelFile(disabled ? inactive_models.get(lightstate) : active_models.get(lightstate)
                    ).rotationY(switch (facing) {
                        case EAST -> wall ? 270 : 90;
                        case SOUTH -> wall ? 0 : 180;
                        case WEST -> wall? 90 : 270;
                        default -> wall? 180 : 0;
                    }).rotationX(switch (face) {
                        case FLOOR -> 0;
                        case CEILING -> 180;
                        case WALL -> 270;
                    })
                    .build();
        }, AbstractRelayBlock.WATERLOGGED, ElectricRelayBlock.POWERED);

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

    public void extendableGeneratorAndExtension(RegistryObject<Block> genRegistryObject, RegistryObject<Block> extensionRegistryObject, String identifier) {
        Block block = genRegistryObject.get();
        String texturePath = identifier.concat("_generator");
        ModelFile model = this.models().getExistingFile(modLoc(texturePath));
        this.getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model)
                .rotationY(state.getValue(ExtendableGeneratorBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 0 : 90)
                .build());
        this.generatorExtension(extensionRegistryObject, identifier);
    }

    private void generatorExtension(RegistryObject<Block> registryObject, String identifier) {
        Block block = registryObject.get();
        String texturePath = identifier.concat("_extension");
        ModelFile model = this.models().getExistingFile(modLoc(texturePath));
        this.getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model)
                .rotationY(state.getValue(ExtendableGeneratorBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 0 : 90)
                .build());
    }

    public void switchboard() {
        Block block = ELBlockRegistry.SWITCHBOARD_BLOCK.get();
        String path = ELBlockRegistry.SWITCHBOARD_BLOCK.getId().getPath();
        String texturePath = "electriclights:block/switchboard_";
        List<ModelFile> active_models = new ArrayList<>();
        List<ModelFile> inactive_models = new ArrayList<>();
        for (int i = 0; i <= 2; i++) {
            active_models.add(this.models().withExistingParent(path.concat("_active_") + i, texturePath + "active_blueprint")
                    .texture("1", texturePath + i)
                    .texture("0", texturePath.concat("base"))
                    .texture("particle", texturePath.concat("base")));
            inactive_models.add(this.models().withExistingParent(path.concat("_inactive_") + i, texturePath + "inactive_blueprint")
                    .texture("1", texturePath + 0)
                    .texture("0", texturePath.concat("base"))
                    .texture("particle", texturePath.concat("base")));
        }
        this.getVariantBuilder(block).forAllStatesExcept(state -> {
            int lightstate = state.getValue(MasterSwitchboardBlock.LIGHTSTATE);
            lightstate = Math.min(lightstate, 2);
            Direction facing = state.getValue(MasterSwitchboardBlock.FACING);
            boolean disabled = state.getValue(MasterSwitchboardBlock.DISABLED);
            return ConfiguredModel.builder()
                    .modelFile(disabled ? inactive_models.get(lightstate) : active_models.get(lightstate)
                    ).rotationY(switch (facing) {
                        case EAST -> 90;
                        case SOUTH -> 180;
                        case WEST -> 270;
                        default -> 0;
                    })
                    .build();
        }, MasterSwitchboardBlock.WATERLOGGED, MasterSwitchboardBlock.POWERED);
    }
}
