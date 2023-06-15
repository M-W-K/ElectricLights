package com.m_w_k.electriclights.blockentity;

import com.m_w_k.electriclights.ELConfig;
import com.m_w_k.electriclights.block.AbstractRelayBlock;
import com.m_w_k.electriclights.block.BurnOutAbleLightBlock;
import com.m_w_k.electriclights.block.MasterSwitchboardBlock;
import com.m_w_k.electriclights.network.ELPacketHandler;
import com.m_w_k.electriclights.network.SwitchboardHumPacket;
import com.m_w_k.electriclights.util.*;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.block.VoltageBlock;
import com.m_w_k.electriclights.registry.ELBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.m_w_k.electriclights.block.MasterSwitchboardBlock.DISABLED;

public class MasterSwitchboardBlockEntity extends BlockEntity implements IEnergyStorage {
    private Set<GraphNode> connectedNodes;
    private final List<GraphNode> generators;
    private int energy = 0;
    private int voltage = -1;
    private static final int maxEnergy = 120000;
    private int servicedLightCount = 0;
    private int ticksSinceLastUpdate = 0;
    private int ticksToNextUpdate = 1;
    private boolean hasCapacitor = false;
    private boolean forceUpdate = false;
    private GraphNode selfNode = null;

    private boolean badConnect = false;
    private int ticksWithoutGoodConnect = 0;

    private boolean disabled = false;

    private int ticksToSoundUpdate = 0;
    private boolean finalSoundUpdate = false;
    private boolean outOfPower = false;

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
        voltage = findVoltage();
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
                switch (node.getType()) {
                    case LIGHT -> servicedLightCount++;
                    case GENERATOR -> generators.add(node);
                    case SWITCHBOARD -> {
                        badConnect = true;
                        updateServicedLights(1);
                    }
                }
                // break statement must be outside the switch block
                if (badConnect) break;

            }
            // make sure to turn off any recently disconnected lights
            if (oldConnected != null) {
                oldConnected.removeAll(connectedNodes);
                // run an intersection with all known lights to make sure we don't attempt to update a removed light
                oldConnected.retainAll(ELGraphHandler.getAllNodes(level));
                updateLights(0, oldConnected, false);
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

    private int findVoltage() {
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
                    // retrieve energy continuously, using a simulated energy limit
                    if (!self.generators.isEmpty()) {
                        int interpolatedEnergy = self.disabled ? 0 : (self.voltage + 2) * self.servicedLightCount * self.ticksSinceLastUpdate;
                        self.energy += self.retrieveEnergy(maxEnergy + interpolatedEnergy - self.energy);
                    }

                    if (self.ticksToNextUpdate <= self.ticksSinceLastUpdate || self.forceUpdate) {
                        if (state.getValue(DISABLED) != self.disabled) {
                            if (self.disabled) {
                                self.ticksSinceLastUpdate = 0;
                            } else {
                                self.doEnergyCalculations();
                            }
                            self.disabled = state.getValue(DISABLED);
                        }

                        if (!self.disabled) {
                            self.doEnergyCalculations();
                        } else {
                            self.updateServicedLights(0);
                            self.ticksToNextUpdate = ELConfig.SERVER.minimumSwitchboardUpdateInterval();
                        }
                        if (!self.hasCapacitor) self.hasCapacitor = true;
                        if (self.forceUpdate) self.forceUpdate = false;
                        self.ticksSinceLastUpdate = 1; // 1 tick to compensate for this calculation tick

                    } else self.ticksSinceLastUpdate++;

                    if (!self.disabled && !self.outOfPower) {
                        // do sound updates every 10 ticks while active
                        if (self.ticksToSoundUpdate <= 10) {
                            self.ticksToSoundUpdate++;
                        } else {
                            ELPacketHandler.sendToNearClients(new SwitchboardHumPacket(pos, true), pos, 16, level);
                            self.ticksToSoundUpdate = 0;
                        }
                    } else if (!self.finalSoundUpdate) {
                        ELPacketHandler.sendToNearClients(new SwitchboardHumPacket(pos, false), pos, 16, level);
                        self.finalSoundUpdate = true;
                    }

                } else if (self.hasCapacitor) {
                    self.updateServicedLights(1);
                    self.hasCapacitor = false;
                }
            } else if (self.ticksWithoutGoodConnect >= ELConfig.SERVER.minimumSwitchboardUpdateInterval() * 10) {
                self.refresh(level);
                self.ticksWithoutGoodConnect = 1; // 1 tick to compensate for this calculation tick
            } else self.ticksWithoutGoodConnect++;
        }
    }

    private void doEnergyCalculations() {
        if (outOfPower) {
            ticksToSoundUpdate = 20;
            outOfPower = false;
        }
        energy -= (voltage + 2) * servicedLightCount * ticksSinceLastUpdate;
        ticksToNextUpdate = Math.max(energy / ((voltage + 2) * servicedLightCount), ELConfig.SERVER.minimumSwitchboardUpdateInterval());
        if (energy <= 0) {
            energy = 0;
            updateServicedLights(0);
            outOfPower = true;
            // if we run out of power, don't bother updating for an extended interval
            ticksToNextUpdate = ELConfig.SERVER.minimumSwitchboardUpdateInterval() * 2;
        } else updateServicedLights(voltage);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        updateServicedLights(0);
    }

    private void updateServicedLights(int state) {
        updateLights(state, connectedNodes, true);
    }

    private void updateLights(int state, Set<GraphNode> nodes, boolean updateSelf) {
        if (level == null) ElectricLightsMod.logToConsole("Warning! A Master Switchboard doesn't know its level and can't update lights because of it!");
        else if (level.getServer() != null && !level.getServer().isCurrentlySaving() && nodes != null) {
            if (updateSelf) {
                // Update the attached block to the new state
                BlockPos pos = this.getBlockPos();
                if (!isRemoved()) {
                    // No need to safe set because we act as a chunkloader
                    level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(MasterSwitchboardBlock.LIGHTSTATE, state));
                }
                // Kill the sound if we get shut down, we won't actually go through the main update loop anymore
                if (state <= 1) ELPacketHandler.sendToNearClients(new SwitchboardHumPacket(pos, false), pos, 16, level);
            }

            for (GraphNode node : nodes) {
                GraphNode.NodeType nodeType = node.getType();
                if (!nodeType.isSpecial()) {
                    SafeBlockSetter.ChangeBlockStateInterface lambda = (a) -> a.setValue(AbstractRelayBlock.LIGHTSTATE, state);
                    // do burn out math if the light can be burnt out (misc > 0)
                    if (node.getMisc() > 0) {
                        // Misc is light age + 1, or + 9 for waterlogged lights
                        int currentAge = node.getMisc();
                        if (currentAge != 8 && currentAge != 16) {
                            // lasts on average 28 state changes, or only 14 if waterlogged.
                            double rand = Math.random();
                            if (rand < 0.25 || (rand < 0.5 && currentAge > 8)) {
                                // Only pass the more complex lambda if an age increment is okayed
                                lambda = (a) -> {
                                    // Only do age increment if the lightstate changed
                                    if (a.getValue(ELBlockStateProperties.LIGHTSTATE) == state)
                                        return a;
                                    node.setMisc(currentAge + 1);
                                    ELGraphHandler.markGraphAsDirty(level);
                                    return a.setValue(AbstractRelayBlock.LIGHTSTATE, state)
                                            // waterlogged blocks should still get a value between 1 and 7
                                            .setValue(BurnOutAbleLightBlock.AGE, (currentAge) % 8);
                                };
                            }
                        }
                    }
                    SafeBlockSetter.safeChangeBlockAndUpdate(node.getPos(), lambda, level);
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
                    if (entity instanceof ELGenerator generator) fulfilled -= generator.fetchEnergy(fulfilled);
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
    public static int getMaxEnergy() {
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

    public void forceUpdate() {
        forceUpdate = true;
    }

    public void setTicksToSoundUpdate(int ticksToSoundUpdate) {
        this.ticksToSoundUpdate = ticksToSoundUpdate;
        this.finalSoundUpdate = false;
    }
}
