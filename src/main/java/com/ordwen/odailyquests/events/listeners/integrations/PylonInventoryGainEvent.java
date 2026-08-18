package com.ordwen.odailyquests.events.listeners.integrations;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Internal event object used only to pass a verified net Pylon inventory gain through the
 * normal quest progression matcher. It is not fired on Bukkit's event bus.
 */
public final class PylonInventoryGainEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ItemStack itemStack;

    public PylonInventoryGainEvent(Player player, ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack.clone();
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
