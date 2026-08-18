package com.ordwen.odailyquests.events.listeners.integrations;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Reflection-only item detection for optional integrations that should never become hard runtime dependencies.
 */
public final class ExternalItemIntegration {

    private ExternalItemIntegration() {}

    public static boolean isRebarItem(ItemStack stack) {
        if (stack == null) return false;
        try {
            Class<?> rebarItem = loadPluginClass("Rebar", "io.github.pylonmc.rebar.item.RebarItem");
            if (rebarItem == null) return false;
            Method method = rebarItem.getMethod("isRebarItem", ItemStack.class);
            Object result = method.invoke(null, stack);
            return result instanceof Boolean bool && bool;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    /**
     * Returns the Rebar/Pylon namespaced key when possible. Pylon content normally uses the pylon namespace.
     */
    public static NamespacedKey getRebarItemKey(ItemStack stack) {
        if (stack == null) return null;
        try {
            Class<?> rebarItem = loadPluginClass("Rebar", "io.github.pylonmc.rebar.item.RebarItem");
            if (rebarItem == null) return null;
            Object wrapped = rebarItem.getMethod("fromStack", ItemStack.class).invoke(null, stack);
            if (wrapped == null) return null;
            Object key = wrapped.getClass().getMethod("getKey").invoke(wrapped);
            return key instanceof NamespacedKey namespacedKey ? namespacedKey : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    public static boolean isPylonItem(ItemStack stack) {
        NamespacedKey key = getRebarItemKey(stack);
        return key != null && "pylon".equalsIgnoreCase(key.getNamespace());
    }

    public static boolean isMMOItem(ItemStack stack) {
        if (stack == null) return false;
        try {
            Class<?> mmoItems = loadPluginClass("MMOItems", "net.Indyuce.mmoitems.MMOItems");
            if (mmoItems == null) return false;
            Method getType = mmoItems.getMethod("getType", ItemStack.class);
            return getType.invoke(null, stack) != null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    public static boolean isItemsAdderItem(ItemStack stack) {
        if (stack == null) return false;
        try {
            Class<?> customStack = loadPluginClass("ItemsAdder", "dev.lone.itemsadder.api.CustomStack");
            if (customStack == null) return false;
            Method byItemStack = customStack.getMethod("byItemStack", ItemStack.class);
            return byItemStack.invoke(null, stack) != null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static Class<?> loadPluginClass(String pluginName, String className) throws ClassNotFoundException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null || !plugin.isEnabled()) return null;
        return plugin.getClass().getClassLoader().loadClass(className);
    }
}
