package com.m_w_k.electriclights.blockentity;

import com.m_w_k.electriclights.Alternator.AlternatorMenu;
import com.m_w_k.electriclights.ElectricLightsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import static com.m_w_k.electriclights.block.AlternatorBlock.LIT;

public class AlternatorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, StackedContentsCompatible, Generator {
    private static final int[] SLOTS_FUEL = {0};
    protected NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int litTime; // The current fuel burning progress time.
    private int litDuration; // Total time it takes a fuel item to burn.
    private int energyGenerated; // Energy we've built up
    private final int softEnergyCap = 12000;
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AlternatorBlockEntity.this.litTime;
                case 1 -> AlternatorBlockEntity.this.litDuration;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> AlternatorBlockEntity.this.litTime = value;
                case 1 -> AlternatorBlockEntity.this.litDuration = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    private LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    public AlternatorBlockEntity(BlockPos pos, BlockState state) {
        super(ElectricLightsMod.ALTERNATOR_GENERATOR.get(), pos, state);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new AlternatorMenu(id, playerInventory, this, this.dataAccess);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        if (!this.remove && direction != null && capability == ForgeCapabilities.ITEM_HANDLER) {
            if (direction == Direction.NORTH) {
                return handlers[0].cast();
            } else if (direction == Direction.SOUTH) {
                return handlers[1].cast();
            } else if (direction == Direction.EAST) {
                return handlers[2].cast();
            } else {
                return handlers[3].cast();
            }
        }
        return super.getCapability(capability, direction);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<? extends IItemHandler> handler : this.handlers) {
            handler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.handlers = SidedInvWrapper.create(this, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlternatorBlockEntity self) {
        if (!level.isClientSide()) {
            if (self.litTime >= self.litDuration) {
                if (self.softEnergyCap > self.energyGenerated) self.litDuration = getBurnTime(self.getItem(0));
                else self.litDuration = 0;
                if (self.litDuration > 0) {
                    if (self.softEnergyCap > self.energyGenerated) self.removeItem(0,1);
                    self.litTime = 1;
                    self.energyGenerated += ElectricLightsMod.ALTERNATOR_ENERGY_FACTOR;
                } else if (self.litTime != 0) self.litTime = 0;
            } else {
                self.litTime++;
                self.energyGenerated += ElectricLightsMod.ALTERNATOR_ENERGY_FACTOR;
            }
            if (state.getValue(LIT) != self.isLit()) level.setBlockAndUpdate(pos, state.setValue(LIT, self.isLit()));
        }
    }

    public boolean isLit() {
        return this.litTime > 0;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.items, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.items, index);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.items.set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
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
        return this.items.size();
    }

    @Override
    public void fillStackedContents(StackedContents helper) {
        for (ItemStack itemstack : this.items) {
            helper.accountStack(itemstack);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOTS_FUEL;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return burnable(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("menu." + ElectricLightsMod.MODID + ".alternator");
    }

    public static int getBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, null);
    }
    public static boolean burnable(ItemStack stack) {
        return getBurnTime(stack) > 0;
    }
    public int fetchEnergy(int amountRequested) {
        amountRequested = Math.min(energyGenerated, amountRequested);
        energyGenerated -= amountRequested;
        return amountRequested;
    }
    public int getLitTime() {
        return litTime;
    }
    public int getLitDuration() {
        return litDuration;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.litTime = tag.getInt("LitTime");
        this.litDuration = tag.getInt("LitDuration");
        this.energyGenerated = tag.getInt("EnergyGenerated");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("LitTime", this.litTime);
        tag.putInt("LitDuration", this.litDuration);
        tag.putInt("EnergyGenerated", this.energyGenerated);
        ContainerHelper.saveAllItems(tag, this.items);
    }
}
