package com.m_w_k.electriclights.util;

import com.m_w_k.electriclights.block.AbstractRelayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * The point of this class is to buffer calls of level.setBlockAndUpdate until the chunk that the update was called for is loaded.
 * Meant to prevent lag machines based on constantly loading and unloading chunks via diverse methods
 */
@Mod.EventBusSubscriber
public class SafeBlockSetter {

    private static final Map<ChunkPos, HashMap<BlockPos, ChangeBlockStateInterface>> CHUNK_MAP = new HashMap<>();

    /**
     * Watches for chunk load events and checks if the loaded chunk has a block that needs to update
     */
    @SubscribeEvent
    public static void watchForLoad(ChunkEvent.Load event) {
        HashMap<BlockPos, ChangeBlockStateInterface> positions = CHUNK_MAP.get(event.getChunk().getPos());
        if (positions != null && positions.size() > 0) {
            LevelAccessor level = event.getLevel();
            for (BlockPos pos : positions.keySet()) {
                level.setBlock(pos, positions.get(pos).changeState(level.getBlockState(pos)), 3);
            }
            CHUNK_MAP.remove(event.getChunk().getPos());
        }
    }

    /**
     * Makes sure the chunk being updated is loaded before setting the block, otherwise waits until the chunk is loaded by something else.
     * @return Whether the block was immediately set or not.
     */
    public static boolean safeSetBlockAndUpdate(BlockPos pos, BlockState state, Level level) {
        return safeChangeBlockAndUpdate(pos, (a) -> state, level);
    }
    /**
     * Makes sure the chunk being updated is loaded before setting the block, otherwise waits until the chunk is loaded by something else.
     * Runs the provided lambda on the already present BlockState before overriting.
     * @return Whether the block was immediately set or not.
     */
    public static boolean safeChangeBlockAndUpdate(BlockPos pos, ChangeBlockStateInterface lambda, Level level) {
        if (level.isLoaded(pos)) {
            try {
                level.setBlockAndUpdate(pos, lambda.changeState(level.getBlockState(pos)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            ChunkPos chunkPos = new ChunkPos(pos);
            if (CHUNK_MAP.containsKey(chunkPos)) {
                HashMap<BlockPos, ChangeBlockStateInterface> positions = CHUNK_MAP.get(chunkPos);
                positions.put(pos, lambda);
            } else {
                HashMap<BlockPos, ChangeBlockStateInterface> positions = new HashMap<>();
                positions.put(pos, lambda);
                CHUNK_MAP.put(chunkPos, positions);
            }
            return false;
        }
    }

    public interface ChangeBlockStateInterface {
        BlockState changeState(BlockState state);
    }
}
