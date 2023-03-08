package com.m_w_k.electriclights.data;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.block.BurnOutAbleLightBlock;
import com.m_w_k.electriclights.block.ElectricRelayBlock;
import com.m_w_k.electriclights.registry.ELBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
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
        this.relay(ELBlockRegistry.ELECTRIC_LIGHT, "light");
        this.relay(ELBlockRegistry.DRAGON_LIGHT, "light");
        // commented out because textures / blueprint models are missing
        // this.relay(ELBlockRegistry.ELECTRIC_RELAY, "relay");
    }
    public void relay(RegistryObject<Block> registryObject, String variant) {
        Block block = registryObject.get();
        String path = registryObject.getId().getPath();
        String texturePath = "electriclights:block/" + variant + "_";
        List<ModelFile> floor_models = new ArrayList<>();
        List<ModelFile> wall_models = new ArrayList<>();
        List<ModelFile> ceiling_models = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            floor_models.add(this.models().withExistingParent(path.concat("_floor_") + i,texturePath + "floor_blueprint")
                    .texture("2",texturePath + i)
                    .texture("particle",texturePath + i));
            wall_models.add(this.models().withExistingParent(path.concat("_wall_") + i,texturePath + "wall_blueprint")
                    .texture("2",texturePath + i)
                    .texture("particle",texturePath + i));
            ceiling_models.add(this.models().withExistingParent(path.concat("_ceiling_") + i,texturePath + "ceiling_blueprint")
                    .texture("2",texturePath + i)
                    .texture("particle",texturePath + i));
        }
        this.getVariantBuilder(block).forAllStatesExcept(state -> {
            int lightstate = state.getValue(ElectricRelayBlock.LIGHTSTATE);
            boolean burntOut = false;
            if (state.getBlock() instanceof BurnOutAbleLightBlock) burntOut = state.getValue(BurnOutAbleLightBlock.AGE) == 7;
            Direction facing = state.getValue(ElectricRelayBlock.FACING);
            return ConfiguredModel.builder()
                    .modelFile(switch (facing) {
                        case DOWN -> floor_models.get(burntOut ? 0 :lightstate);
                        case UP -> ceiling_models.get(burntOut ? 0 :lightstate);
                        default -> wall_models.get(burntOut ? 0 :lightstate);
                    }).rotationY(switch (facing) {
                        case EAST -> 90;
                        case SOUTH -> 180;
                        case WEST -> 270;
                        default -> 0;
                    })
                    .build();
        }, ElectricRelayBlock.WATERLOGGED);

    }
}
