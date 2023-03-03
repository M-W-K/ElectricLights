package com.m_w_k.electriclights;

import net.minecraftforge.eventbus.api.Event;

public class GraphUpdateEvent extends Event {
    protected final GraphNode node;
    protected final boolean isCreateEvent;

    public GraphUpdateEvent(GraphNode node, boolean isCreateEvent) {
        this.node = node;
        this.isCreateEvent = isCreateEvent;
    }

    public GraphNode getNode() {
        return node;
    }

    public boolean isCreateEvent() {
        return isCreateEvent;
    }
}
