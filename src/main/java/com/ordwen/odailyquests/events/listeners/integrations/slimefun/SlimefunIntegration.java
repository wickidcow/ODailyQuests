package com.ordwen.odailyquests.events.listeners.integrations.slimefun;

import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** Reflection-only Slimefun hook; Slimefun remains an optional dependency. */
public final class SlimefunIntegration {
    private static volatile boolean initialized;
    private static volatile boolean available;
    private static Method getByItem;
    private static Method getId;
    private static Method getAddon;

    private SlimefunIntegration() {}

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            Class<?> slimefunItem = Class.forName("io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem");
            getByItem = slimefunItem.getMethod("getByItem", ItemStack.class);
            getId = slimefunItem.getMethod("getId");
            getAddon = slimefunItem.getMethod("getAddon");
            available = true;
        } catch (ReflectiveOperationException exception) {
            available = false;
            PluginLogger.warn("Slimefun is installed but its SlimefunItem API could not be resolved: " + exception.getMessage());
        }
    }

    private static Object resolveItem(ItemStack itemStack) throws ReflectiveOperationException {
        if (itemStack == null || itemStack.getType().isAir()) return null;
        init();
        if (!available) return null;
        return getByItem.invoke(null, itemStack);
    }

    public static String getItemId(ItemStack itemStack) {
        try {
            Object slimefunItem = resolveItem(itemStack);
            if (slimefunItem == null) return null;
            Object id = getId.invoke(slimefunItem);
            return id == null ? null : id.toString().trim().toUpperCase(Locale.ROOT);
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Unable to resolve a Slimefun item ID: " + exception.getMessage());
            return null;
        }
    }

    public static String getAddonName(ItemStack itemStack) {
        try {
            Object slimefunItem = resolveItem(itemStack);
            if (slimefunItem == null) return null;
            return addonName(slimefunItem);
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Unable to resolve Slimefun addon ownership: " + exception.getMessage());
            return null;
        }
    }

    public static boolean matchesAddon(ItemStack itemStack, List<String> aliases) {
        if (aliases == null || aliases.isEmpty()) return true;
        String addon = normalize(getAddonName(itemStack));
        if (addon.isEmpty()) return false;
        return aliases.stream().map(SlimefunIntegration::normalize).anyMatch(addon::equals);
    }

    /** Checks the live Slimefun registry so renamed/forked Bukkit plugin jars can still match by addon ownership. */
    public static boolean isAnyAddonPresent(List<String> aliases) {
        if (aliases == null || aliases.isEmpty()) return true;
        init();
        if (!available) return false;

        try {
            Class<?> slimefun = Class.forName("io.github.thebusybiscuit.slimefun4.implementation.Slimefun");
            Object registry = slimefun.getMethod("getRegistry").invoke(null);
            Object itemsObject = registry.getClass().getMethod("getSlimefunItems").invoke(registry);
            if (!(itemsObject instanceof Collection<?> items)) return false;

            List<String> normalizedAliases = aliases.stream()
                    .map(SlimefunIntegration::normalize)
                    .filter(value -> !value.isEmpty())
                    .toList();

            for (Object item : items) {
                String addonName = normalize(addonName(item));
                if (!addonName.isEmpty() && normalizedAliases.contains(addonName)) return true;
            }
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Unable to inspect registered Slimefun addons: " + exception.getMessage());
        }
        return false;
    }

    private static String addonName(Object slimefunItem) throws ReflectiveOperationException {
        Object addon = getAddon.invoke(slimefunItem);
        if (addon == null) return null;
        Method getName = addon.getClass().getMethod("getName");
        Object name = getName.invoke(addon);
        return name == null ? null : name.toString();
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
