package com.ordwen.odailyquests.events.listeners.integrations.slimefun;

import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Locale;

/** Reflection-only Slimefun hook; Slimefun remains an optional dependency. */
public final class SlimefunIntegration {
    private static volatile boolean initialized;
    private static volatile boolean available;
    private static Method getByItem;
    private static Method getId;

    private SlimefunIntegration() {}

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            Class<?> slimefunItem = Class.forName("io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem");
            getByItem = slimefunItem.getMethod("getByItem", ItemStack.class);
            getId = slimefunItem.getMethod("getId");
            available = true;
        } catch (ReflectiveOperationException exception) {
            available = false;
            PluginLogger.warn("Slimefun is installed but its SlimefunItem API could not be resolved: " + exception.getMessage());
        }
    }

    public static String getItemId(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) return null;
        init();
        if (!available) return null;
        try {
            Object slimefunItem = getByItem.invoke(null, itemStack);
            if (slimefunItem == null) return null;
            Object id = getId.invoke(slimefunItem);
            return id == null ? null : id.toString().trim().toUpperCase(Locale.ROOT);
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Unable to resolve a Slimefun item ID: " + exception.getMessage());
            return null;
        }
    }
}
