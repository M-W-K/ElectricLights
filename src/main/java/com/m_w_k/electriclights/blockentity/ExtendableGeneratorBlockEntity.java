package com.m_w_k.electriclights.blockentity;

import com.m_w_k.electriclights.util.Generator;
import com.m_w_k.electriclights.util.GraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ExtendableGeneratorBlockEntity extends BaseContainerBlockEntity implements Generator {
    protected List<BlockPos> extensionPositions = new ArrayList<>();
    protected int misc; // Miscellaneous value
    protected int energyGenerated; // Energy we've built up
    protected static final int softEnergyCap = 12000;
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ExtendableGeneratorBlockEntity.this.extensionPositions.size();
                case 1 -> ExtendableGeneratorBlockEntity.this.misc;
                case 2 -> ExtendableGeneratorBlockEntity.this.energyGenerated;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                // no editing extension count
                case 1 -> ExtendableGeneratorBlockEntity.this.misc = value;
                case 2 -> ExtendableGeneratorBlockEntity.this.energyGenerated = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public ExtendableGeneratorBlockEntity(BlockPos pos, BlockState state, BlockEntityType<?> blockEntityType) {
        super(blockEntityType, pos, state);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.getLevel() != null && this.getLevel().getBlockEntity(this.getBlockPos()) != this) {
            return false;
        } else {
            return player.distanceToSqr(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5) <= 64.0;
        }
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int p_18941_) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int p_18942_, int p_18943_) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int p_18944_, ItemStack p_18945_) {

    }

    @Override
    public void clearContent() {

    }

    public int fetchEnergy(int amountRequested) {
        amountRequested = Math.min(energyGenerated, amountRequested);
        energyGenerated -= amountRequested;
        return amountRequested;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.extensionPositions = extensionDataHelper(tag.getString("Extensions"));
        this.misc = tag.getInt("Misc");
        this.energyGenerated = tag.getInt("EnergyGenerated");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Extensions", extensionDataHelper(extensionPositions));
        tag.putInt("Misc", this.misc);
        tag.putInt("EnergyGenerated", this.energyGenerated);
    }

    private static List<BlockPos> extensionDataHelper(String data) {
        if (data.equals("")) { // handle empty state to prevent errors
            return new ArrayList<>();
        } else {
            List<BlockPos> extensions = new ArrayList<>();
            for (String extension : data.split(";")) {
                List<Integer> coords = Arrays.stream(extension.split(",")).sequential().map(Integer::parseInt).toList();
                extensions.add(new BlockPos(coords.get(0), coords.get(1), coords.get(2)));
            }
            return extensions;
        }
    }
    private static String extensionDataHelper(List<BlockPos> extensionPositions) {
        StringBuilder data = new StringBuilder();
        for (BlockPos extension : extensionPositions) {
            data.append(extension.getX()).append(",").append(extension.getY()).append(",").append(extension.getZ()).append(";");
        }
        return data.toString();
    }

    public void noteExtension(boolean registerNew, BlockPos pos) {
        if (registerNew) extensionPositions.add(pos);
        else extensionPositions.remove(pos);
    }

    public static int getSoftEnergyCap() {
        return softEnergyCap;
    }
}
