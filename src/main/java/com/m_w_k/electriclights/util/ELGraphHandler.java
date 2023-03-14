package com.m_w_k.electriclights.util;

import com.m_w_k.electriclights.ELConfig;
import com.m_w_k.electriclights.ElectricLightsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ELGraphHandler {
    private static final Map<Level, ElectricLightsGraph> electricLightsGraphs = new HashMap<>();
    @Nullable
    private static ElectricLightsGraph getGraphForLevel(Level level) {
        return electricLightsGraphs.get(level);
    }
    public static boolean addGraphNodeAndAutoConnect(GraphNode node, Level level) {
        ElectricLightsGraph graph = getGraphForLevel(level);
        if (graph == null) return false;
        GraphNode[] graphNodes = graph.getNodes();
        graph.addNode(node);
        for (GraphNode graphNode : graphNodes) {
            if (areConnected(node, graphNode)) graph.addConnection(node, graphNode);
        }
        graph.refreshSwitchboards(level);
        graph.setDirty();
        return true;
    }
    public static boolean removeGraphNode(GraphNode node, Level level) {
        ElectricLightsGraph graph = getGraphForLevel(level);
        if (graph == null) return false;
        graph.removeNode(node);
        graph.refreshSwitchboards(level);
        graph.setDirty();
        return true;
    }
    public static List<GraphNode> getSwitchboards(Level level) {
        ElectricLightsGraph graph = getGraphForLevel(level);
        if (graph == null) return new ArrayList<>();
        return graph.getSwitchboards();
    }
    public static List<GraphNode> getGenerators(Level level) {
        ElectricLightsGraph graph = getGraphForLevel(level);
        if (graph == null) return new ArrayList<>();
        return graph.getGenerators();
    }
    public static Set<GraphNode> getConnectedNodes(GraphNode node, Level level) {
        ElectricLightsGraph graph = getGraphForLevel(level);
        if (graph == null) return new HashSet<>();
        return graph.getConnectedNodes(node);
    }
    public static boolean nodeExists(GraphNode node, Level level) {
        ElectricLightsGraph graph = getGraphForLevel(level);
        if (graph == null) return false;
        return graph.nodeExists(node);
    }

    public static void loadGraphs(MinecraftServer server) {
        Iterable<ServerLevel> levels = server.getAllLevels();
        levels.forEach(serverLevel -> electricLightsGraphs.put(serverLevel,ElectricLightsGraph.create().recallFromStorage(serverLevel.getDataStorage(),serverLevel)));
    }

    @Contract(pure = true)
    public static boolean areConnected(GraphNode node1, GraphNode node2) {
        return !node1.toString().equals(node2.toString()) && areConnected(node1.getPos(), node2.getPos());
    }
    @Contract(pure = true)
    public static boolean areConnected(BlockPos pos1, BlockPos pos2) {
        return !pos1.equals(pos2) && (pos1.distSqr(pos2) <= ELConfig.SERVER.nodeConnectDistSqr());
    }
}
