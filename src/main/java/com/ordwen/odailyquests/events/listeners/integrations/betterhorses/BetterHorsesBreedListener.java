package com.ordwen.odailyquests.events.listeners.integrations.betterhorses;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.events.listeners.entity.BreedProgressEvent;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional BetterHorses bridge.
 *
 * <p>For breeds BetterHorses approves through BetterHorseBreedEvent, that
 * custom event becomes the authoritative quest signal. The native Bukkit
 * EntityBreedEvent is still used for every other breed. Progression is delayed
 * by one tick so the outer Bukkit event can finish and any cancellation can be
 * honored before the quest advances.</p>
 */
public final class BetterHorsesBreedListener extends PlayerProgressor implements Listener {

    private static final String BETTER_HORSES_EVENT =
            "me.luisgamedev.betterhorses.api.events.BetterHorseBreedEvent";
    private static final long FEEDER_TTL_MILLIS = 120_000L;

    private static final Set<UUID> BETTER_HORSES_CHILDREN = ConcurrentHashMap.newKeySet();

    private final Map<UUID, PendingBreed> pendingBreeds = new ConcurrentHashMap<>();
    private final Map<UUID, RecentFeeder> recentFeeders = new ConcurrentHashMap<>();

    private BetterHorsesBreedListener() {
    }

    public static void register(PluginManager pluginManager, ODailyQuests plugin) {
        final Plugin betterHorses = pluginManager.getPlugin("BetterHorses");
        if (betterHorses == null || !betterHorses.isEnabled()) return;

        BETTER_HORSES_CHILDREN.clear();
        final BetterHorsesBreedListener bridge = new BetterHorsesBreedListener();
        pluginManager.registerEvents(bridge, plugin);

        try {
            final Class<?> rawEventClass = betterHorses.getClass().getClassLoader().loadClass(BETTER_HORSES_EVENT);
            if (!Event.class.isAssignableFrom(rawEventClass)) {
                PluginLogger.warn("BetterHorses breed event is not a Bukkit event; compatibility hook was skipped.");
                return;
            }

            @SuppressWarnings("unchecked")
            final Class<? extends Event> eventClass = (Class<? extends Event>) rawEventClass;
            final Method getChild = rawEventClass.getMethod("getChild");
            final Method getFather = rawEventClass.getMethod("getFather");
            final Method getMother = rawEventClass.getMethod("getMother");

            pluginManager.registerEvent(
                    eventClass,
                    bridge,
                    EventPriority.MONITOR,
                    (ignored, event) -> bridge.onBetterHorsesBreed(event, getChild, getFather, getMother),
                    plugin,
                    true
            );

            PluginLogger.info("BetterHorses breeding compatibility enabled.");
        } catch (ReflectiveOperationException | LinkageError e) {
            PluginLogger.warn("BetterHorses is installed, but its breed event API could not be hooked.");
            PluginLogger.warn("Details: " + e.getMessage());
        }
    }

    /**
     * Returns true when BetterHorses has already approved this foal through
     * BetterHorseBreedEvent. The native listener then defers progression to
     * this bridge so the same breed cannot be counted twice.
     */
    public static boolean handles(EntityBreedEvent event) {
        return event.getEntity() instanceof AbstractHorse child
                && BETTER_HORSES_CHILDREN.contains(child.getUniqueId());
    }

    /**
     * Capture Bukkit's breeder before BetterHorses fires its nested custom event.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVanillaBreedStarted(EntityBreedEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse child)) return;

        UUID breederId = event.getBreeder() instanceof Player player ? player.getUniqueId() : null;
        if (breederId == null) {
            breederId = resolveRecentFeeder(event.getFather(), event.getMother());
        }

        pendingBreeds.put(
                child.getUniqueId(),
                new PendingBreed(breederId, child.getType(), VanillaResult.PENDING, false, false)
        );
    }

    /**
     * Record the final Bukkit event state. BetterHorses progression is delayed
     * until this outer event has completed so cancelled breeding never counts.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onVanillaBreedFinalized(EntityBreedEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse child)) return;

        final UUID childId = child.getUniqueId();
        pendingBreeds.computeIfPresent(childId, (ignored, current) -> {
            if (!current.customApproved()) {
                return null;
            }

            final VanillaResult result = event.isCancelled()
                    ? VanillaResult.CANCELLED
                    : VanillaResult.SUCCESS;

            UUID breederId = current.playerId();
            if (event.getBreeder() instanceof Player player) {
                breederId = player.getUniqueId();
            }

            return new PendingBreed(
                    breederId,
                    child.getType(),
                    result,
                    true,
                    current.fallbackScheduled()
            );
        });
    }

    /**
     * Keep a short-lived record of who put each horse into love mode. This is
     * only used when a BetterHorses custom breed has no usable Bukkit breeder.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHorseEnterLoveMode(EntityEnterLoveModeEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse horse)) return;
        if (!(event.getHumanEntity() instanceof Player player)) return;

        final long now = System.currentTimeMillis();
        recentFeeders.put(horse.getUniqueId(), new RecentFeeder(player.getUniqueId(), now));

        if (recentFeeders.size() > 512) {
            recentFeeders.entrySet().removeIf(entry -> now - entry.getValue().timestamp() > FEEDER_TTL_MILLIS);
        }
    }

    private void onBetterHorsesBreed(Event event, Method getChild, Method getFather, Method getMother) {
        try {
            final Object childObject = getChild.invoke(event);
            if (!(childObject instanceof Entity child)) return;

            final Entity father = getFather.invoke(event) instanceof Entity entity ? entity : null;
            final Entity mother = getMother.invoke(event) instanceof Entity entity ? entity : null;
            final UUID childId = child.getUniqueId();

            final PendingBreed existing = pendingBreeds.get(childId);
            UUID breederId = existing != null ? existing.playerId() : null;
            if (breederId == null) {
                breederId = resolveRecentFeeder(father, mother);
            }

            if (breederId == null) {
                Debugger.write("BetterHorses breed detected for " + child.getType()
                        + ", but no player breeder could be resolved.");
                return;
            }

            final EntityType childType = child.getType();
            final VanillaResult currentResult = existing != null
                    ? existing.vanillaResult()
                    : VanillaResult.NO_VANILLA_EVENT;
            final boolean alreadyScheduled = existing != null && existing.fallbackScheduled();

            pendingBreeds.put(
                    childId,
                    new PendingBreed(breederId, childType, currentResult, true, true)
            );
            BETTER_HORSES_CHILDREN.add(childId);

            if (!alreadyScheduled) {
                scheduleFallback(childId, breederId);
            }
        } catch (ReflectiveOperationException e) {
            PluginLogger.warn("Could not read BetterHorses breed event data: " + e.getMessage());
        }
    }

    private void scheduleFallback(UUID childId, UUID breederId) {
        final Player player = Bukkit.getPlayer(breederId);
        if (player == null) {
            pendingBreeds.remove(childId);
            BETTER_HORSES_CHILDREN.remove(childId);
            return;
        }

        ODailyQuests.morePaperLib.scheduling().entitySpecificScheduler(player).runDelayed(
                () -> runFallback(childId, player),
                () -> {
                    pendingBreeds.remove(childId);
                    BETTER_HORSES_CHILDREN.remove(childId);
                },
                1L
        );
    }

    private void runFallback(UUID childId, Player player) {
        final PendingBreed pending = pendingBreeds.remove(childId);
        BETTER_HORSES_CHILDREN.remove(childId);

        if (pending == null || !pending.customApproved() || !player.isOnline()) return;
        if (pending.vanillaResult() == VanillaResult.CANCELLED) return;

        Debugger.write("BetterHorses progressing BREED quest for "
                + player.getName() + " with " + pending.entityType() + ".");
        setPlayerQuestProgression(
                new BreedProgressEvent(pending.entityType(), "BetterHorses"),
                player,
                1,
                "BREED"
        );
    }

    private UUID resolveRecentFeeder(Entity father, Entity mother) {
        final long now = System.currentTimeMillis();
        final RecentFeeder fatherFeeder = getRecentFeeder(father, now);
        final RecentFeeder motherFeeder = getRecentFeeder(mother, now);

        if (fatherFeeder != null && motherFeeder != null) {
            return fatherFeeder.playerId().equals(motherFeeder.playerId())
                    ? fatherFeeder.playerId()
                    : null;
        }
        if (fatherFeeder != null) return fatherFeeder.playerId();
        if (motherFeeder != null) return motherFeeder.playerId();
        return null;
    }

    private RecentFeeder getRecentFeeder(Entity horse, long now) {
        if (horse == null) return null;

        final RecentFeeder feeder = recentFeeders.get(horse.getUniqueId());
        if (feeder == null) return null;

        if (now - feeder.timestamp() > FEEDER_TTL_MILLIS) {
            recentFeeders.remove(horse.getUniqueId(), feeder);
            return null;
        }
        return feeder;
    }

    private enum VanillaResult {
        PENDING,
        NO_VANILLA_EVENT,
        SUCCESS,
        CANCELLED
    }

    private record PendingBreed(
            UUID playerId,
            EntityType entityType,
            VanillaResult vanillaResult,
            boolean customApproved,
            boolean fallbackScheduled
    ) {
    }

    private record RecentFeeder(UUID playerId, long timestamp) {
    }
}
