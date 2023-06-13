package com.m_w_k.electriclights.util;

import com.m_w_k.electriclights.ElectricLightsMod;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ElectricLightsGraph extends SavedData {
    private final Graph<GraphNode, DefaultEdge> g;
    private Level selfLevel;

    private ConnectivityInspector<GraphNode, DefaultEdge> connectivityInspector;
    private boolean connectivityInspectorInvalid = false;
    private final List<GraphNode> switchboards = new ArrayList<>();
    private final List<GraphNode> generators = new ArrayList<>();
    private ElectricLightsGraph() {
        g = new SimpleGraph<>(DefaultEdge.class);
        connectivityInspector = new ConnectivityInspector<>(g);
    }

    void refreshSwitchboards(Level level) {
        for (GraphNode node : switchboards) {
            BlockEntity blockEntity = level.getBlockEntity(node.getPos());
            if (blockEntity instanceof MasterSwitchboardBlockEntity switchboard) {
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
    List<GraphNode> getGenerators() {
        return generators;
    }

    void addNode(GraphNode node) {
        g.addVertex(node);
        if (node.getType().isSwitchboard()) switchboards.add(node);
        else if (node.getType().isGenerator()) generators.add(node);
        setInspectorInvalid();
    }
    void addConnection(GraphNode node1, GraphNode node2) {
        g.addEdge(node1, node2);
        setInspectorInvalid();
    }
    void removeNode(GraphNode node) {
        g.removeVertex(node);
        if (node.getType().isSwitchboard()) switchboards.remove(node);
        else if (node.getType().isGenerator()) generators.remove(node);
        setInspectorInvalid();
    }

    /**
     * When removing a node, all connections to that node are automatically removed as well. Because of that, this method should almost never be used.
     */
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
        ElectricLightsMod.logToConsole("Saving Graph for level '{}'/{}", selfLevel, selfLevel.dimension().location());
        String[] data = this.parsed();
        CompoundTag nodes = new CompoundTag();
        GraphNode[] graphNodes = deparseNodes(data[0]);
        nodes.putInt("Count", graphNodes.length);
        for (int i = 0; i < graphNodes.length; i++) {
            nodes.putString(String.valueOf(i), graphNodes[i].toString());
        }
        CompoundTag edges = new CompoundTag();
        GraphNode[][] graphEdges = deparseEdges(data[1]);
        edges.putInt("Count", graphEdges.length);
        for (int i = 0; i < graphEdges.length; i++) {
            edges.putString(String.valueOf(i), graphEdges[i][0].toString() + ',' + graphEdges[i][1].toString());
        }
        tag.put("GraphNodes", nodes);
        tag.put("GraphEdges", edges);
        return tag;
    }

    protected @NotNull ElectricLightsGraph createWithLog(Level level) {
        ElectricLightsMod.logToConsole("No saved Graph was found for '{}'/{}, generating new one.", level, level.dimension().location());
        return create(level);
    }
    protected static @NotNull ElectricLightsGraph create(Level level) {
        ElectricLightsGraph graph = new ElectricLightsGraph();
        graph.selfLevel = level;
        return graph;
    }

    /**
     * Dangerous, only use if you plan to quickly generate a new graph using non-static methods
     */
    protected static @NotNull ElectricLightsGraph create() {
        return new ElectricLightsGraph();
    }

    protected @NotNull ElectricLightsGraph load(@NotNull CompoundTag tag, Level level) {
        ElectricLightsMod.logToConsole("Found saved Graph for '{}'/{}, now loading.", level, level.dimension().location());
        // ElectricLightsMod.logToConsole("Graph contains the following node data: " + tag.getString("ELGraphNodes"));
        // ElectricLightsMod.logToConsole("Graph contains the following edge data: " + tag.getString("ELGraphEdges"));
        ElectricLightsGraph graph = create();
        GraphNode[] nodes = loadNodes(tag.getCompound("GraphNodes"));
        GraphNode[][] edges = loadEdges(tag.getCompound("GraphEdges"));
        for (GraphNode node : nodes) {
            graph.addNode(node);
        }
        for (GraphNode[] edge : edges) {
            graph.addConnection(edge[0], edge[1]);
        }
        graph.selfLevel = level;
        return graph;
    }

    ElectricLightsGraph recallFromStorage(@NotNull DimensionDataStorage storage, Level level) {
        return storage.computeIfAbsent( (tag) -> load(tag, level), () -> createWithLog(level), "ElectricLightsGraph");
    }

    @Contract("_ -> new")
    private static @NotNull GraphNode generateNode(String[] data) {
        // don't bother with misc unless it is present
        if (data.length == 4) return new GraphNode(new BlockPos(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])), GraphNode.NodeType.valueOf(data[3]));
        else return new GraphNode(new BlockPos(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])), GraphNode.NodeType.valueOf(data[3]), Integer.parseInt(data[4]));
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
        String[] graph = {"",""};

        Set<GraphNode> vertices = g.vertexSet();
        Set<DefaultEdge> edges = g.edgeSet();
        graph[0] = vertices.toString()
                .replace("[","")
                .replace("]","");
        graph[1] = edges.toString()
                .replace("[(", "")
                .replace(")]", "")
                .replaceAll("\\), \\(", ", ")
                .replaceAll(" : ", ",");

        // return should be of type ["v1, v2, v3, v4", "v1,v2, v2,v3, v3,v4, v4,v1"]
        return graph;
    }

    protected static GraphNode[] deparseNodes(@NotNull String parsedNodes) {
        if (parsedNodes.equals("")) { // handle empty state to prevent errors
            return new GraphNode[] {};
        } else {
            List<GraphNode> nodes = new ArrayList<>();
            String[] splitNodes = parsedNodes.split(", "); // Split nodes apart
            for (String node : splitNodes) { // Add split nodes to array
                nodes.add(generateNode(node));
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
                edges.add(new GraphNode[] {generateNode(nodes[0]), generateNode(nodes[1])});
            }
            return edges.toArray(new GraphNode[0][0]);
        }
    }

    protected static GraphNode[] loadNodes(CompoundTag nodeTag) {
        int size = nodeTag.getInt("Count");
        List<GraphNode> nodes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            nodes.add(generateNode(nodeTag.getString(String.valueOf(i))));
        }
        return nodes.toArray(new GraphNode[0]);
    }

    protected static GraphNode[][] loadEdges(CompoundTag nodeTag) {
        int size = nodeTag.getInt("Count");
        List<GraphNode[]> nodes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String[] edge = nodeTag.getString(String.valueOf(i)).split(",");
            nodes.add(new GraphNode[] {generateNode(edge[0]),generateNode(edge[1])});
        }
        return nodes.toArray(new GraphNode[0][0]);
    }
}
