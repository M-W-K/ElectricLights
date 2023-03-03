package com.m_w_k.electriclights;

import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

// TODO implement dimension-aware graph; unique graph for every level?
public class ElectricLightsGraph extends SavedData {
    private final Graph<GraphNode, DefaultEdge> g;
    private ConnectivityInspector<GraphNode, DefaultEdge> connectivityInspector;
    private boolean connectivityInspectorInvalid = false;
    private final List<GraphNode> switchboards = new ArrayList<>();
    ElectricLightsGraph() {
        g = new SimpleGraph<>(DefaultEdge.class);
        connectivityInspector = new ConnectivityInspector<>(g);
    }

    void refreshSwitchboards(Level level) {
        for (GraphNode node : switchboards) {
            BlockEntity blockEntity = level.getBlockEntity(node.getPos());
            if (blockEntity != null && blockEntity.getClass() == MasterSwitchboardBlockEntity.class) {
                MasterSwitchboardBlockEntity switchboard = (MasterSwitchboardBlockEntity) blockEntity;
                switchboard.refresh(level);
            }
        }
    }
    void setInspectorInvalid() {
        connectivityInspectorInvalid = true;
    }

    List<GraphNode> getSwitchboards() {
        return switchboards;
    }

    void addNode(GraphNode node) {
        g.addVertex(node);
        if (node.isSwitchboard())  switchboards.add(node);
        setInspectorInvalid();
    }
    void addConnection(GraphNode node1, GraphNode node2) {
        g.addEdge(node1, node2);
        setInspectorInvalid();
    }
    void removeNode(GraphNode node) {
        g.removeVertex(node);
        if (node.isSwitchboard()) switchboards.remove(node);
        setInspectorInvalid();
    }
    // when removing a node, all connections to that node are automatically removed as well. Thus, this method should only very rarely be used.
    void removeConnection(GraphNode node1, GraphNode node2) {
        g.removeEdge(node1, node2);
        setInspectorInvalid();
    }

    Set<GraphNode> getConnectedNodes(GraphNode node) {
        if (connectivityInspectorInvalid) connectivityInspector = new ConnectivityInspector<>(g);
        return connectivityInspector.connectedSetOf(node);
    }
    boolean nodeExists(GraphNode node) {
        return g.containsVertex(node);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ElectricLightsMod.logToConsole("Saving Graph!");
        String[] data = this.parsed();
        // ElectricLightsMod.logToConsole("Saving the following node data: " + data[0]);
        // ElectricLightsMod.logToConsole("Saving the following edge data: " + data[1]);
        tag.putString("ELGraphNodes", data[0]);
        tag.putString("ELGraphEdges", data[1]);
        // ElectricLightsMod.logToConsole("Saved Graph Successfully.");
        return tag;
    }

    protected @NotNull ElectricLightsGraph createWithLog() {
        ElectricLightsMod.logToConsole("No saved Graph was found, generating new one.");
        return create();
    }

    @Contract(" -> new")
    protected static @NotNull ElectricLightsGraph create() {
        return new ElectricLightsGraph();
    }

    protected @NotNull ElectricLightsGraph load(@NotNull CompoundTag tag) {
        ElectricLightsMod.logToConsole("Found saved Graph, now loading.");
        // ElectricLightsMod.logToConsole("Graph contains the following node data: " + tag.getString("ELGraphNodes"));
        // ElectricLightsMod.logToConsole("Graph contains the following edge data: " + tag.getString("ELGraphEdges"));
        ElectricLightsGraph graph = create();
        GraphNode[] nodes = deparseNodes(tag.getString("ELGraphNodes"));
        GraphNode[][] edges = deparseEdges(tag.getString("ELGraphEdges"));
        for (GraphNode node : nodes) {
            graph.addNode(node);
        }
        for (GraphNode[] edge : edges) {
            graph.addConnection(edge[0], edge[1]);
        }
        // ElectricLightsMod.logToConsole("Loaded Graph Successfully.");
        return graph;
    }

    ElectricLightsGraph recallFromStorage(@NotNull DimensionDataStorage storage) {
        return storage.computeIfAbsent(this::load, this::createWithLog, "ElectricLightsGraph");
    }

    @Contract("_ -> new")
    private static @NotNull GraphNode generateNode(String[] data) {
        if (Objects.equals(data[3], "SWITCHBOARD")) {
            GraphNode node = new GraphNode(new BlockPos(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])), false, true);
            ElectricLightsMod.electricLightsGraph.switchboards.add(node);
            return node;
        } else return new GraphNode(new BlockPos(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])), Boolean.parseBoolean(data[3]), false);
    }
    @Contract("_ -> new")
    private static @NotNull GraphNode generateNode(String unsplitData) {
        String[] data = unsplitData.split(" ");
        return generateNode(data);
    }

    GraphNode[] getNodes() {
        return g.vertexSet().toArray(new GraphNode[0]);
    }
    GraphNode[][] getEdges() {
        DefaultEdge[] edges = g.edgeSet().toArray(new DefaultEdge[0]);
        List<GraphNode[]> edgesParsed = new ArrayList<>();
        for (DefaultEdge edge : edges) {
            String[] nodes = edge.toString().replace('(',' ').replace(')', ' ').strip().split(":");
            edgesParsed.add(new GraphNode[] {generateNode(nodes[0]),generateNode(nodes[1])});
        }
        return edgesParsed.toArray(new GraphNode[0][0]);
    }

    protected String[] parsed() {
        // toString() produces string of format "([v1, v2, v3, v4], [{v1,v2}, {v2,v3}, {v3,v4}, {v4,v1}])"

        // "(v1, v2, v3, v4" and "{v1,v2}, {v2,v3}, {v3,v4}, {v4,v1}])"
        String[] graph = g.toString().replaceAll("\\[","").split("], ");

        // "v1, v2, v3, v4"
        graph[0] = graph[0].replaceFirst("\\(","");

        // "v1,v2, v2,v3, v3,v4, v4,v1"
        graph[1] = graph[1].replaceFirst("]\\)", "").replaceAll("\\{","").replaceAll("}","");
        return graph;
    }

    protected static GraphNode[] deparseNodes(@NotNull String parsedNodes) {
        if (parsedNodes.equals("")) { // handle empty state to prevent errors
            return new GraphNode[] {};
        } else {
            List<GraphNode> nodes = new ArrayList<>();
            String[] splitNodes = parsedNodes.split(", "); // Split nodes apart
            for (String node : splitNodes) { // Add split nodes to array
                nodes.add(generateNode(node.split(" ")));
            }
            return nodes.toArray(new GraphNode[0]);
        }
    }

    protected static GraphNode[][] deparseEdges(@NotNull String parsedEdges) {
        if (parsedEdges.equals("")) { // handle empty state to prevent errors
            return new GraphNode[][] {};
        } else {
            List<GraphNode[]> edges = new ArrayList<>();
            String[] splitEdges = parsedEdges.split(", "); // Split edges apart
            for (String edge : splitEdges) { // Add edges to array
                String[] nodes = edge.split(",");
                edges.add(new GraphNode[]{generateNode(nodes[0]), generateNode(nodes[1])});
            }
            return edges.toArray(new GraphNode[0][0]);
        }
    }
}
