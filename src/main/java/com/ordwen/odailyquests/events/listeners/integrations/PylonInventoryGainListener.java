package com.ordwen.odailyquests.events.listeners.integrations;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Paper-only bridge for Pylon machines that place finished items directly into a player's
 * inventory instead of firing a Bukkit crafting/pickup event. The listener observes net slot
 * gains over one tick so moving an existing item between player inventory slots does not count.
 */
public final class PylonInventoryGainListener extends PlayerProgressor implements Listener, EventExecutor {

    public static final String SLOT_CHANGE_EVENT = "io.papermc.paper.event.player.PlayerInventorySlotChangeEvent";

    private final ODailyQuests plugin;
    private final Map<UUID, Map<String, Integer>> pending = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> pickupSuppression = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, ItemStack>> samples = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> scheduled = ConcurrentHashMap.newKeySet();

    private PylonInventoryGainListener(ODailyQuests plugin) {
        this.plugin = plugin;
    }

    public static void register(PluginManager pluginManager, ODailyQuests plugin) {
        try {
            Class<?> raw = Class.forName(SLOT_CHANGE_EVENT);
            Class<? extends Event> eventClass = raw.asSubclass(Event.class);
            PylonInventoryGainListener listener = new PylonInventoryGainListener(plugin);

            // The Bukkit listener is used only to suppress normal world pickups, which are
            // already handled by ExternalItemProgressListener.
            pluginManager.registerEvents(listener, plugin);
            pluginManager.registerEvent(eventClass, listener, EventPriority.MONITOR, listener, plugin, true);
            PluginLogger.info("Hooked Paper inventory changes for Pylon machine-output progression.");
        } catch (ClassNotFoundException | ClassCastException exception) {
            PluginLogger.fine("Paper inventory slot event unavailable; Pylon direct-inventory output tracking disabled.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem().getItemStack();
        NamespacedKey key = ExternalItemIntegration.getRebarItemKey(stack);
        if (!isPylonKey(key)) return;

        int picked = Math.max(0, stack.getAmount() - event.getRemaining());
        if (picked <= 0) return;
        pickupSuppression.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .merge(key.toString(), picked, Integer::sum);
        scheduleFlush(player);
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (!SLOT_CHANGE_EVENT.equals(event.getClass().getName())) return;
        try {
            Method getPlayer = event.getClass().getMethod("getPlayer");
            Method getOld = event.getClass().getMethod("getOldItemStack");
            Method getNew = event.getClass().getMethod("getNewItemStack");

            Object playerObject = getPlayer.invoke(event);
            Object oldObject = getOld.invoke(event);
            Object newObject = getNew.invoke(event);
            if (!(playerObject instanceof Player player)
                    || !(oldObject instanceof ItemStack oldStack)
                    || !(newObject instanceof ItemStack newStack)) {
                return;
            }

            // Machine collection happens while the player's own inventory view is active.
            // Ignore chest/merchant/etc. views so retrieving an old Pylon item from storage
            // does not look like fresh production.
            if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) return;

            NamespacedKey oldKey = ExternalItemIntegration.getRebarItemKey(oldStack);
            NamespacedKey newKey = ExternalItemIntegration.getRebarItemKey(newStack);

            if (isPylonKey(oldKey) && oldKey.equals(newKey)) {
                int delta = newStack.getAmount() - oldStack.getAmount();
                if (delta != 0) record(player, newKey, delta, newStack);
            } else {
                if (isPylonKey(oldKey) && oldStack.getAmount() > 0) {
                    record(player, oldKey, -oldStack.getAmount(), oldStack);
                }
                if (isPylonKey(newKey) && newStack.getAmount() > 0) {
                    record(player, newKey, newStack.getAmount(), newStack);
                }
            }
        } catch (ReflectiveOperationException exception) {
            throw new EventException(exception);
        }
    }

    private void record(Player player, NamespacedKey key, int delta, ItemStack sample) {
        UUID uuid = player.getUniqueId();
        String id = key.toString();
        pending.computeIfAbsent(uuid, ignored -> new ConcurrentHashMap<>()).merge(id, delta, Integer::sum);
        if (delta > 0) {
            samples.computeIfAbsent(uuid, ignored -> new ConcurrentHashMap<>()).put(id, sample.clone());
        }
        scheduleFlush(player);
    }

    private void scheduleFlush(Player player) {
        UUID uuid = player.getUniqueId();
        if (!scheduled.add(uuid)) return;

        ODailyQuests.morePaperLib.scheduling().entitySpecificScheduler(player).runDelayed(
                () -> flush(player),
                () -> clear(uuid),
                1L
        );
    }

    private void flush(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> deltas = pending.remove(uuid);
        Map<String, Integer> suppressed = pickupSuppression.remove(uuid);
        Map<String, ItemStack> itemSamples = samples.remove(uuid);
        scheduled.remove(uuid);

        if (!player.isOnline() || deltas == null || deltas.isEmpty()) return;
        for (Map.Entry<String, Integer> entry : deltas.entrySet()) {
            int gain = entry.getValue();
            if (gain <= 0) continue;

            int pickupAmount = suppressed == null ? 0 : Math.max(0, suppressed.getOrDefault(entry.getKey(), 0));
            gain -= Math.min(gain, pickupAmount);
            if (gain <= 0) continue;

            ItemStack sample = itemSamples == null ? null : itemSamples.get(entry.getKey());
            if (sample == null) continue;

            Debugger.write("PylonInventoryGainListener: net direct inventory gain " + gain + " of " + entry.getKey() + ".");
            setPlayerQuestProgression(new PylonInventoryGainEvent(player, sample), player, gain, "REBAR_ITEM");
        }
    }

    private void clear(UUID uuid) {
        pending.remove(uuid);
        pickupSuppression.remove(uuid);
        samples.remove(uuid);
        scheduled.remove(uuid);
    }

    private static boolean isPylonKey(NamespacedKey key) {
        return key != null && "pylon".equalsIgnoreCase(key.getNamespace());
    }
}
