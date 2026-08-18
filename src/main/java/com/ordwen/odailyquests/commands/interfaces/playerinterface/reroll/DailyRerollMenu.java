package com.ordwen.odailyquests.commands.interfaces.playerinterface.reroll;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.features.RerollService;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.tools.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** GUI flow opened from the top-right Daily Reroll button. */
public final class DailyRerollMenu implements Listener {

    private static final int CHOICE_REROLL_ONE = 11;
    private static final int CHOICE_REROLL_ALL = 15;
    private static final int CHOICE_CANCEL = 22;
    private static final int[] QUEST_SLOTS = {10, 11, 12, 13, 14, 15, 16};

    private enum Mode { CHOICE, SINGLE }

    private static final class Holder implements InventoryHolder {
        private final Mode mode;
        private final Map<Integer, Integer> questIndexes;
        private Inventory inventory;

        private Holder(Mode mode, Map<Integer, Integer> questIndexes) {
            this.mode = mode;
            this.questIndexes = questIndexes;
        }

        private void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return Objects.requireNonNull(inventory, "Reroll menu inventory has not been initialized yet");
        }
    }

    public static void openChoice(Player player) {
        if (!RerollService.canRerollToday(player, true)) return;

        final Holder holder = new Holder(Mode.CHOICE, Map.of());
        final Inventory inventory = Bukkit.createInventory(holder, 27, ChatColor.AQUA + "Daily Reroll");
        holder.setInventory(inventory);
        fill(inventory);

        inventory.setItem(CHOICE_REROLL_ONE, item(
                Material.BOOK,
                ChatColor.AQUA + "Reroll One Quest",
                List.of(
                        ChatColor.GRAY + "Choose one daily quest",
                        ChatColor.GRAY + "to replace with a new one.",
                        "",
                        ChatColor.YELLOW + "Uses today's reroll."
                )
        ));
        inventory.setItem(CHOICE_REROLL_ALL, item(
                Material.NETHER_STAR,
                ChatColor.RED + "Reroll All Quests",
                List.of(
                        ChatColor.GRAY + "Replace your entire current",
                        ChatColor.GRAY + "daily quest set at once.",
                        "",
                        ChatColor.YELLOW + "Still counts as one reroll."
                )
        ));
        inventory.setItem(CHOICE_CANCEL, item(Material.BARRIER, ChatColor.RED + "Cancel", List.of()));
        player.openInventory(inventory);
    }

    private static void openSingle(Player player) {
        final PlayerQuests playerQuests = QuestsManager.getActiveQuests().get(player.getName());
        if (playerQuests == null || !RerollService.canRerollToday(player, true)) return;

        final List<AbstractQuest> quests = new ArrayList<>(playerQuests.getQuests().keySet());
        final Map<Integer, Integer> slotToQuest = new HashMap<>();
        final Holder holder = new Holder(Mode.SINGLE, slotToQuest);
        final Inventory inventory = Bukkit.createInventory(holder, 27, ChatColor.AQUA + "Choose a Quest to Reroll");
        holder.setInventory(inventory);
        fill(inventory);

        final int count = Math.min(quests.size(), QUEST_SLOTS.length);
        for (int i = 0; i < count; i++) {
            final AbstractQuest quest = quests.get(i);
            final int slot = QUEST_SLOTS[i];
            final ItemStack display = quest.getMenuItem().clone();
            final ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(TextFormatter.format(quest.getQuestName()));
                meta.setLore(List.of(
                        ChatColor.GRAY + "Category: " + ChatColor.WHITE + quest.getCategoryName(),
                        "",
                        ChatColor.YELLOW + "Click to reroll this quest",
                        ChatColor.DARK_GRAY + "Quest #" + (i + 1)
                ));
                display.setItemMeta(meta);
            }
            display.setAmount(1);
            inventory.setItem(slot, display);
            slotToQuest.put(slot, i + 1);
        }

        inventory.setItem(22, item(Material.ARROW, ChatColor.YELLOW + "Back", List.of()));
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Holder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

        final int slot = event.getRawSlot();
        if (holder.mode == Mode.CHOICE) {
            if (slot == CHOICE_REROLL_ONE) {
                openSingle(player);
            } else if (slot == CHOICE_REROLL_ALL) {
                final boolean success = RerollService.rerollAll(player);
                showDailyMenuAfterAction(player, success);
            } else if (slot == CHOICE_CANCEL) {
                player.closeInventory();
            }
            return;
        }

        if (slot == 22) {
            openChoice(player);
            return;
        }

        final Integer questIndex = holder.questIndexes.get(slot);
        if (questIndex == null) return;
        final boolean success = RerollService.rerollOne(player, questIndex);
        showDailyMenuAfterAction(player, success);
    }

    private static void showDailyMenuAfterAction(Player player, boolean success) {
        if (!success) {
            player.closeInventory();
            return;
        }
        final Inventory daily = ODailyQuests.INSTANCE.getInterfacesManager()
                .getPlayerQuestsInterface().getPlayerQuestsInterface(player);
        if (daily != null) player.openInventory(daily);
        else player.closeInventory();
    }

    private static void fill(Inventory inventory) {
        final ItemStack filler = item(Material.BLUE_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
