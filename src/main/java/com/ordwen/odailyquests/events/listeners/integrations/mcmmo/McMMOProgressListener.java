package com.ordwen.odailyquests.events.listeners.integrations.mcmmo;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

/** Reflection-only mcMMO hook so mcMMO remains a true optional dependency. */
public final class McMMOProgressListener extends PlayerProgressor implements Listener, EventExecutor {

    public static final String XP_EVENT_CLASS_NAME = "com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent";

    public static void register(PluginManager pluginManager, ODailyQuests odq) {
        Plugin mcMMO = pluginManager.getPlugin("mcMMO");
        if (mcMMO == null || !mcMMO.isEnabled()) return;

        try {
            Class<?> raw = mcMMO.getClass().getClassLoader().loadClass(XP_EVENT_CLASS_NAME);
            Class<? extends Event> eventClass = raw.asSubclass(Event.class);
            McMMOProgressListener listener = new McMMOProgressListener();
            pluginManager.registerEvent(eventClass, listener, EventPriority.MONITOR, listener, odq, true);
            PluginLogger.info("Hooked into mcMMO XP event.");
        } catch (ClassNotFoundException | ClassCastException exception) {
            PluginLogger.warn("Cannot hook into mcMMO XP event: " + exception.getMessage());
        }
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (!XP_EVENT_CLASS_NAME.equals(event.getClass().getName())) return;
        try {
            Method getPlayer = event.getClass().getMethod("getPlayer");
            Method getRawXpGained = event.getClass().getMethod("getRawXpGained");
            Object playerObject = getPlayer.invoke(event);
            Object xpObject = getRawXpGained.invoke(event);
            if (!(playerObject instanceof Player player) || !(xpObject instanceof Number number)) return;

            int amount = Math.max(1, (int) Math.ceil(number.doubleValue()));
            Debugger.write("McMMOProgressListener: gained " + amount + " mcMMO XP.");
            setPlayerQuestProgression(event, player, amount, "MCMMO_EXP");
        } catch (ReflectiveOperationException exception) {
            throw new EventException(exception);
        }
    }
}
