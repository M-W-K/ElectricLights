package com.m_w_k.electriclights.util;

import com.m_w_k.electriclights.ElectricLightsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
            if (!node.toString().equals(graphNode.toString())) {
                BlockPos pos1 = node.getPos();
                BlockPos pos2 = graphNode.getPos();
                if (pos1.distSqr(pos2) <= ElectricLightsMod.NODE_CONNECT_DIST_SQR) {
                    graph.addConnection(node, graphNode);
                }
            }
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
}
