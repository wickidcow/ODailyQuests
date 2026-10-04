package com.ordwen.odailyquests.events.listeners.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Internal progression event used by optional breeding integrations when a
 * successful breed cannot be attributed through Bukkit's EntityBreedEvent.
 */
public final class BreedProgressEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final EntityType entityType;
    private final String source;

    public BreedProgressEvent(EntityType entityType, String source) {
        this.entityType = entityType;
        this.source = source;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getSource() {
        return source;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
