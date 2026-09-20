package com.ordwen.odailyquests.events.listeners.item;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantItemListenerTest {

    @Test
    void enchantListenerObservesFinalSuccessfulEventState() throws NoSuchMethodException {
        final Method method = EnchantItemListener.class.getDeclaredMethod(
                "onEnchantItemEvent", EnchantItemEvent.class);

        final EventHandler handler = method.getAnnotation(EventHandler.class);

        assertNotNull(handler, "EnchantItemEvent handler must remain registered");
        assertEquals(EventPriority.MONITOR, handler.priority(),
                "ENCHANT progression must observe the final event state");
        assertTrue(handler.ignoreCancelled(),
                "Cancelled enchants must never progress ENCHANT quests");
    }
}
