package com.ordwen.odailyquests.events.listeners.integrations.valhallammo;

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

public final class ValhallaMMOProgressListener extends PlayerProgressor implements Listener, EventExecutor {

    public static final String EXP_EVENT_CLASS_NAME = "me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent";
    public static final String LEVEL_UP_EVENT_CLASS_NAME = "me.athlaeos.valhallammo.event.PlayerSkillLevelUpEvent";

    public static void register(final PluginManager pluginManager, final ODailyQuests plugin) {
        final Plugin valhallaMMO = pluginManager.getPlugin("ValhallaMMO");
        if (valhallaMMO == null || !valhallaMMO.isEnabled()) {
            return;
        }

        final ValhallaMMOProgressListener listener = new ValhallaMMOProgressListener();

        registerEvent(pluginManager, plugin, valhallaMMO, listener, EXP_EVENT_CLASS_NAME);
        registerEvent(pluginManager, plugin, valhallaMMO, listener, LEVEL_UP_EVENT_CLASS_NAME);
    }

    private static void registerEvent(
            final PluginManager pluginManager,
            final ODailyQuests plugin,
            final Plugin valhallaMMO,
            final ValhallaMMOProgressListener listener,
            final String eventClassName
    ) {
        try {
            final Class<?> rawEventClass = valhallaMMO.getClass().getClassLoader().loadClass(eventClassName);
            final Class<? extends Event> eventClass = rawEventClass.asSubclass(Event.class);

            pluginManager.registerEvent(
                    eventClass,
                    listener,
                    EventPriority.NORMAL,
                    listener,
                    plugin,
                    true
            );

            PluginLogger.info("Hooked into ValhallaMMO event: " + eventClassName);
        } catch (ClassNotFoundException exception) {
            PluginLogger.warn("Cannot hook into ValhallaMMO: event class not found: " + eventClassName);
        } catch (ClassCastException exception) {
            PluginLogger.warn("Cannot hook into ValhallaMMO: class is not a Bukkit Event: " + eventClassName);
        }
    }

    @Override
    public void execute(final Listener listener, final Event event) throws EventException {
        try {
            final String eventClassName = event.getClass().getName();

            if (EXP_EVENT_CLASS_NAME.equals(eventClassName)) {
                handleExperienceGain(event);
                return;
            }

            if (LEVEL_UP_EVENT_CLASS_NAME.equals(eventClassName)) {
                handleLevelUp(event);
            }
        } catch (ReflectiveOperationException exception) {
            throw new EventException(exception);
        }
    }

    private void handleExperienceGain(final Event event) throws ReflectiveOperationException {
        Debugger.write("[ValhallaMMOProgressListener] ValhallaMMO experience gained.");

        final Player player = getPlayer(event);
        if (player == null) {
            return;
        }

        final Method getAmountMethod = event.getClass().getMethod("getAmount");
        final Object amountObject = getAmountMethod.invoke(event);

        if (!(amountObject instanceof Number number)) {
            return;
        }

        final int progressAmount = Math.max(1, (int) Math.ceil(number.doubleValue()));

        setPlayerQuestProgression(event, player, progressAmount, "VALHALLA_EXP");
    }

    private void handleLevelUp(final Event event) throws ReflectiveOperationException {
        Debugger.write("[ValhallaMMOProgressListener] ValhallaMMO skill level changed.");

        final Player player = getPlayer(event);
        if (player == null) {
            return;
        }

        final Method getLevelFromMethod = event.getClass().getMethod("getLevelFrom");
        final Method getLevelToMethod = event.getClass().getMethod("getLevelTo");

        final Object levelFromObject = getLevelFromMethod.invoke(event);
        final Object levelToObject = getLevelToMethod.invoke(event);

        if (!(levelFromObject instanceof Number levelFrom) || !(levelToObject instanceof Number levelTo)) {
            return;
        }

        final int gainedLevels = levelTo.intValue() - levelFrom.intValue();

        if (gainedLevels <= 0) {
            return;
        }

        setPlayerQuestProgression(event, player, gainedLevels, "VALHALLA_LEVEL_UP");
    }

    private Player getPlayer(final Event event) throws ReflectiveOperationException {
        final Method getPlayerMethod = event.getClass().getMethod("getPlayer");
        final Object playerObject = getPlayerMethod.invoke(event);

        if (playerObject instanceof Player player) {
            return player;
        }

        return null;
    }
}
