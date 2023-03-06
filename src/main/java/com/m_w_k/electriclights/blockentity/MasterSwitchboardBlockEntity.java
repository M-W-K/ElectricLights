package com.m_w_k.electriclights.blockentity;

import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.util.GraphNode;
import com.m_w_k.electriclights.block.ElectricRelayBlock;
import com.m_w_k.electriclights.block.VoltageBlock;
import com.m_w_k.electriclights.registry.ELBlockEntityRegistry;
import com.m_w_k.electriclights.util.Generator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MasterSwitchboardBlockEntity extends BlockEntity implements IEnergyStorage {
    private Set<GraphNode> connectedNodes;
    private final List<GraphNode> generators;
    private int energy = 0;
    private int voltage = -1;
    private final int maxEnergy = 120000;
    private int servicedLightCount = 0;
    private int ticksSinceLastUpdate = 0;
    private int ticksToNextUpdate = 1;
    private boolean hasCapacitor = false;
    private boolean forceUpdate = false;
    private GraphNode selfNode = null;

    private boolean firstLoad = true;

    private boolean badConnect = false;
    private int ticksWithoutGoodConnect = 0;

    public MasterSwitchboardBlockEntity(BlockPos pos, BlockState state) {
        super(ELBlockEntityRegistry.MASTER_SWITCHBOARD.get(), pos, state);
        generators = new ArrayList<>();
    }

    @Override
    public void onLoad() {
        selfNode = findSelfNode();
        refresh();
    }

    /**
     * Only call this overload if you're providing the same level that the switchboard is on, as it sets the switchboard's own level to the one provided.
     */
    public void refresh(Level levelSwitchboardIsIn) {
        setLevel(levelSwitchboardIsIn);
        refresh();
    }

    /**
     * Safe overload that refreshes based on the switchboard's own level
     */
    public void refresh() {
        voltage = refreshVoltage();
        refreshConnectedList();
        forceUpdate = true;
        this.setChanged();
    }

    public void refreshConnectedList() {
        if (selfNode != null) {
            // ElectricLightsMod.logToConsole("Refreshing connected list");
            Set<GraphNode> oldConnected = null;
            if (connectedNodes != null) oldConnected = new HashSet<>(connectedNodes);
            connectedNodes = ELGraphHandler.getConnectedNodes(selfNode, level);
            connectedNodes.remove(selfNode);
            generators.clear();
            servicedLightCount = 0;
            badConnect = false;
            for (GraphNode node : connectedNodes) {
                if (node.getType().isLight()) {
                    servicedLightCount++;
                } else if (node.getType().isSpecial()) { // don't continue down the else/if chain if we're a relay
                    if (node.getType().isGenerator()) {
                        generators.add(node);
                    } else if (node.getType().isSwitchboard()) {
                        badConnect = true;
                        updateServicedLights(1);
                        break;
                    }
                }

            }
            // make sure to turn off any recently disconnected lights
            if (oldConnected != null) {
                oldConnected.removeAll(connectedNodes);
                updateLights(0, oldConnected);
            }
        } else selfNode = findSelfNode();
    }
    @Nullable
    private GraphNode findSelfNode() {
        // ElectricLightsMod.logToConsole("Finding self node");
        for (GraphNode node : ELGraphHandler.getSwitchboards(level)) {
            if (node.getPos().equals(this.getBlockPos())) {
                return node;
            }
        }
        return null;
    }

    private int refreshVoltage() {
        if (getLevel()!=null) {
            BlockState blockBelow = getLevel().getBlockState(worldPosition.below());
            if (blockBelow.getBlock() instanceof VoltageBlock capacitor) {
                return capacitor.getVoltage();
            } else return -1;
        } else return -1;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MasterSwitchboardBlockEntity self) {
        if (!level.isClientSide && !self.isRemoved()) {
            if (self.servicedLightCount != 0 && !self.badConnect) {
                if (self.voltage != -1) {
                    if (self.ticksToNextUpdate <= self.ticksSinceLastUpdate || self.forceUpdate) {
                        self.energy -= self.voltage * self.servicedLightCount * self.ticksSinceLastUpdate;
                        if (!self.generators.isEmpty()) self.energy += self.retrieveEnergy(self.maxEnergy - self.energy);
                        // ElectricLightsMod.logToConsole(String.valueOf(self.energy));
                        self.ticksToNextUpdate = Math.max(self.energy / (self.voltage * self.servicedLightCount), ElectricLightsMod.MINIMUM_SWITCHBOARD_UPDATE_INTERVAL);
                        if (self.energy <= 0) {
                            self.energy = 0;
                            self.updateServicedLights(1);
                            // if we run out of power, don't bother updating for an extended interval
                            self.ticksToNextUpdate = ElectricLightsMod.MINIMUM_SWITCHBOARD_UPDATE_INTERVAL * 2;
                        } else self.updateServicedLights(self.voltage);
                        if (!self.hasCapacitor) self.hasCapacitor = true;
                        if (self.forceUpdate) self.forceUpdate = false;
                        // ElectricLightsMod.logToConsole(String.valueOf(self.ticksToNextUpdate));
                        self.ticksSinceLastUpdate = 1; // 1 tick to compensate for this calculation tick

                    } else self.ticksSinceLastUpdate++;
                } else if (self.hasCapacitor) {
                    self.updateServicedLights(1);
                    self.hasCapacitor = false;
                }
            } else if (self.ticksWithoutGoodConnect >= ElectricLightsMod.MINIMUM_SWITCHBOARD_UPDATE_INTERVAL * 10) {
                self.refresh(level);
                self.ticksWithoutGoodConnect = 1; // 1 tick to compensate for this calculation tick
            } else self.ticksWithoutGoodConnect++;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        updateServicedLights(0);
    }

    private void updateServicedLights(int state) {
        updateLights(state, connectedNodes);
    }

    private void updateLights(int state, Set<GraphNode> nodes) {
        if (level == null) ElectricLightsMod.logToConsole("Warning! A Master Switchboard doesn't know its level and can't update lights because of it!");
        else if (level.getServer() != null && !level.getServer().isCurrentlySaving() && nodes != null) {
            for (GraphNode node : nodes) {
                BlockState nodeState = level.getBlockState(node.getPos());
                BlockState updatedState = nodeState;
                if (nodeState.getBlock() instanceof ElectricRelayBlock) {
                    // don't allow flooded lights to glow, though things like relays are fine.
                    if (nodeState.getValue(ElectricRelayBlock.WATERLOGGED) && node.isLight()) {
                        nodeState = nodeState.setValue(ElectricRelayBlock.LIGHTSTATE, 0);
                    } else nodeState = nodeState.setValue(ElectricRelayBlock.LIGHTSTATE, state);
                    // only update the light if the state has changed
                    if (updatedState != nodeState) level.setBlockAndUpdate(node.getPos(), nodeState);
                }
            }
        }
    }

    private int retrieveEnergy(int maximum) {
        if (level == null) ElectricLightsMod.logToConsole("Warning! A Master Switchboard doesn't know its level and can't retrieve energy because of it!");
        else {
            if (level.getServer() != null && !level.getServer().isCurrentlySaving()) {
                int fulfilled = maximum;
                for (GraphNode node : generators) {
                    BlockEntity entity = level.getBlockEntity(node.getPos());
                    if (entity instanceof Generator generator) fulfilled -= generator.fetchEnergy(fulfilled);
                }
                return maximum - fulfilled;
            }
        }
        return 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyChange = Math.min(maxReceive,maxEnergy-energy);
        if (!simulate) energy += energyChange;
        return energyChange;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return maxEnergy;
    }

    @Override
    public boolean canReceive() {
        return 0 != maxEnergy-energy;
    }
    // no extraction allowed
    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.energy = tag.getInt("Energy");
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", this.energy);
    }
}
