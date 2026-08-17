package com.ordwen.odailyquests.commands.interfaces.playerinterface;

import com.ordwen.odailyquests.commands.interfaces.playerinterface.items.ItemType;
import com.ordwen.odailyquests.commands.interfaces.playerinterface.items.PlayerHead;
import com.ordwen.odailyquests.commands.interfaces.playerinterface.items.getters.InterfaceItemGetter;
import com.ordwen.odailyquests.configuration.functionalities.CompleteOnlyOnClick;
import com.ordwen.odailyquests.files.implementations.PlayerInterfaceFile;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.player.progression.QuestLoaderUtils;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.tools.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/** Loads and renders the configurable player quest inventory. */
public class PlayerQuestsInterface extends InterfaceItemGetter {

    private static final String ERROR_OCCURRED = "An error occurred when loading the player interface. ";
    private static final String OUT_OF_BOUNDS = " is out of bounds (slots must be between 1 and defined size).";
    private static final String MATERIAL = "material";

    private final PlayerInterfaceFile playerInterfaceFile;
    private final PlayerHead playerHead;
    private final Map<Integer, List<Integer>> slotQuests = new HashMap<>();
    private final Map<String, List<Integer>> categorySlots = new HashMap<>();
    private final Set<ItemStack> fillItems = new HashSet<>();
    private final Set<ItemStack> closeItems = new HashSet<>();
    private final Map<Integer, List<String>> playerCommandsItems = new HashMap<>();
    private final Map<Integer, List<String>> consoleCommandsItems = new HashMap<>();
    private final Map<Integer, ItemStack> papiItems = new HashMap<>();
    private final Set<Integer> closeOnClickSlots = new HashSet<>();

    private String interfaceName;
    private Inventory playerQuestsInventoryBase;
    private int size;
    private String achievedStr;
    private String statusStr;
    private String progressStr;
    private String completeGetTypeStr;
    private boolean isGlowingEnabled;
    private boolean isStatusDisabled;

    public PlayerQuestsInterface(PlayerInterfaceFile playerInterfaceFile) {
        this.playerInterfaceFile = playerInterfaceFile;
        this.playerHead = new PlayerHead(playerInterfaceFile);
    }

    public void load() {
        final ConfigurationSection section = playerInterfaceFile.getConfig().getConfigurationSection("player_interface");
        if (section == null) {
            PluginLogger.error(ERROR_OCCURRED + "The playerInterface file is not correctly configured.");
            return;
        }

        loadVariables(section);

        final ConfigurationSection questsSection = section.getConfigurationSection("quests");
        if (questsSection == null) {
            PluginLogger.error(ERROR_OCCURRED + "The quests section is not defined in the playerInterface file.");
            return;
        }
        loadQuestsSlots(questsSection);

        final ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection == null) {
            PluginLogger.warn("The items section is not defined in the playerInterface file.");
            return;
        }
        loadItems(itemsSection);
        PluginLogger.fine("Player quests interface successfully loaded.");
    }

    public Inventory getPlayerQuestsInterface(Player player) {
        final Map<String, PlayerQuests> activeQuests = QuestsManager.getActiveQuests();
        if (!activeQuests.containsKey(player.getName())) {
            PluginLogger.error("Impossible to find the player " + player.getName() + " in the active quests.");
            PluginLogger.error("It can happen if the player try to open the interface while the server/plugin is reloading.");
            PluginLogger.error("If the problem persist, please contact the developer.");
            return null;
        }

        final PlayerQuests playerQuests = activeQuests.get(player.getName());
        if (QuestLoaderUtils.isTimeToRenew(player, activeQuests)) return getPlayerQuestsInterface(player);

        final Inventory inventory = Bukkit.createInventory(new PlayerQuestsHolder(), size, TextFormatter.format(player, interfaceName));
        inventory.setContents(playerQuestsInventoryBase.getContents());
        if (!papiItems.isEmpty()) applyPapiItems(player, playerQuests, inventory);
        inventory.setContents(playerHead.setPlayerHead(inventory, player, size).getContents());
        applyQuestsItems(player, playerQuests.getQuests(), playerQuests, inventory);
        return inventory;
    }

    private void loadVariables(ConfigurationSection interfaceConfig) {
        slotQuests.clear();
        categorySlots.clear();
        fillItems.clear();
        closeItems.clear();
        playerCommandsItems.clear();
        consoleCommandsItems.clear();
        papiItems.clear();
        closeOnClickSlots.clear();
        playerHead.load();

        interfaceName = TextFormatter.format(interfaceConfig.getString(".inventory_name"));
        isGlowingEnabled = interfaceConfig.getBoolean("glowing_if_achieved");
        isStatusDisabled = interfaceConfig.getBoolean("disable_status");
        size = interfaceConfig.getInt(".size");
        playerQuestsInventoryBase = Bukkit.createInventory(null, size, "BASIC");
        achievedStr = interfaceConfig.getString(".achieved");
        statusStr = interfaceConfig.getString(".status");
        progressStr = interfaceConfig.getString(".progress");
        completeGetTypeStr = interfaceConfig.getString(".complete_get_type");
    }

    private void loadQuestsSlots(ConfigurationSection questsSection) {
        final ConfigurationSection categoriesSection = questsSection.getConfigurationSection("categories");
        if (categoriesSection != null) loadCategorySlots(categoriesSection);

        for (String index : questsSection.getKeys(false)) {
            if (index.equalsIgnoreCase("categories")) continue;
            final int slot;
            try {
                slot = Integer.parseInt(index) - 1;
            } catch (NumberFormatException exception) {
                PluginLogger.error(ERROR_OCCURRED + "Invalid quest slot key: " + index + ".");
                continue;
            }

            if (questsSection.isList(index)) slotQuests.put(slot, questsSection.getIntegerList(index));
            else slotQuests.put(slot, Collections.singletonList(questsSection.getInt(index)));
        }
    }

    private void loadCategorySlots(ConfigurationSection categoriesSection) {
        for (String category : categoriesSection.getKeys(false)) {
            final List<Integer> slots = categoriesSection.getIntegerList(category);
            if (slots.isEmpty()) {
                PluginLogger.error(ERROR_OCCURRED + "No slots defined for category " + category + ".");
                continue;
            }
            categorySlots.put(category.toLowerCase(Locale.ROOT), slots);
        }
    }

    private void loadItems(ConfigurationSection itemsSection) {
        for (String element : itemsSection.getKeys(false)) {
            final ConfigurationSection elementSection = itemsSection.getConfigurationSection(element);
            if (elementSection == null) {
                configurationError(element, "item", "The item is not defined.");
                continue;
            }
            final ConfigurationSection itemSection = elementSection.getConfigurationSection("item");
            if (itemSection == null) {
                configurationError(element, "item", "The item is not defined.");
                continue;
            }

            final String material = itemSection.getString(MATERIAL);
            if (material == null) {
                configurationError(element, MATERIAL, "The material of the item is not defined.");
                continue;
            }

            final ItemStack item = getItemStack(element, material, itemSection);
            final List<Integer> slots = getSlots(itemSection);
            final List<ItemFlag> flags = getItemFlags(element, itemSection);
            if (flags == null) continue;

            loadItemType(elementSection, item, itemSection, slots, flags);
            loadPlaceholderItem(slots, item);
            addIntoBaseInventory(element, slots, item);
            loadCloseOnClickItems(elementSection, slots);
        }
    }

    private void loadCloseOnClickItems(ConfigurationSection elementSection, List<Integer> slots) {
        if (!elementSection.getBoolean("close_on_click", false)) return;
        for (int slot : slots) {
            if (slot > 0 && slot <= size) closeOnClickSlots.add(slot - 1);
        }
    }

    private static List<Integer> getSlots(ConfigurationSection itemSection) {
        return itemSection.isList("slot") ? itemSection.getIntegerList("slot") : List.of(itemSection.getInt("slot"));
    }

    private @Nullable List<ItemFlag> getItemFlags(String element, ConfigurationSection itemSection) {
        if (!itemSection.isList("flags")) return Collections.emptyList();
        final List<ItemFlag> flags = new ArrayList<>();
        for (String raw : itemSection.getStringList("flags")) {
            if (raw == null) continue;
            final String normalized = raw.trim().toUpperCase(Locale.ROOT);
            try {
                flags.add(ItemFlag.valueOf(normalized));
            } catch (IllegalArgumentException exception) {
                configurationError(element, "item.flags", normalized + " is not a valid ItemFlag.");
                return null;
            }
        }
        return flags;
    }

    private ItemStack getItemStack(String element, String material, ConfigurationSection itemSection) {
        ItemStack item;
        if (material.equals("CUSTOM_HEAD")) {
            item = ItemUtils.getCustomHead(itemSection.getString("texture"));
        } else if (material.contains(":")) {
            item = getItem(material, element, MATERIAL);
        } else {
            try {
                item = new ItemStack(Material.valueOf(material));
            } catch (IllegalArgumentException exception) {
                configurationError(element, MATERIAL, material + " is not a valid material.");
                item = null;
            }
        }
        return item == null ? new ItemStack(Material.BARRIER) : item;
    }

    private void addIntoBaseInventory(String element, List<Integer> slots, ItemStack item) {
        for (int slot : slots) {
            if (slot > 0 && slot <= size) playerQuestsInventoryBase.setItem(slot - 1, item);
            else PluginLogger.error(ERROR_OCCURRED + "The slot defined for the item " + element + OUT_OF_BOUNDS);
        }
    }

    private void loadPlaceholderItem(List<Integer> slots, ItemStack item) {
        boolean hasPlaceholders = false;
        if (item.hasItemMeta()) {
            final ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && containsPlaceholder(meta.getDisplayName())) hasPlaceholders = true;
            if (!hasPlaceholders && meta.hasLore() && meta.getLore() != null) {
                for (String line : meta.getLore()) {
                    if (containsPlaceholder(line)) {
                        hasPlaceholders = true;
                        break;
                    }
                }
            }
        }
        if (hasPlaceholders) slots.forEach(slot -> papiItems.put(slot - 1, item));
    }

    private boolean containsPlaceholder(String text) {
        return text != null && Pattern.compile("%[^%\\s]+%").matcher(text).find();
    }

    private void loadItemType(ConfigurationSection elementSection, ItemStack item, ConfigurationSection itemSection,
                              List<Integer> slots, List<ItemFlag> flags) {
        final String itemTypeRaw = elementSection.getString("type");
        if (itemTypeRaw == null) {
            configurationError(elementSection.getName(), "type", "The item type is not defined.");
            return;
        }

        final ItemType itemType;
        try {
            itemType = ItemType.valueOf(itemTypeRaw);
        } catch (IllegalArgumentException exception) {
            configurationError(elementSection.getName(), "type", itemTypeRaw + " is not a valid ItemType.");
            return;
        }

        final ItemMeta baseMeta = getItemMeta(item, itemSection, flags);
        switch (itemType) {
            case FILL -> {
                if (baseMeta == null) return;
                baseMeta.setDisplayName(ChatColor.RESET + "");
                item.setItemMeta(baseMeta);
                fillItems.add(item);
            }
            case CLOSE -> {
                if (baseMeta == null) return;
                item.setItemMeta(baseMeta);
                closeItems.add(item);
            }
            case PLAYER_COMMAND -> registerCommandItems(item, baseMeta, elementSection, slots, playerCommandsItems);
            case CONSOLE_COMMAND -> registerCommandItems(item, baseMeta, elementSection, slots, consoleCommandsItems);
        }
    }

    private void registerCommandItems(ItemStack item, ItemMeta baseMeta, ConfigurationSection elementSection,
                                      List<Integer> slots, Map<Integer, List<String>> targetMap) {
        if (baseMeta == null) return;
        final List<String> commands = elementSection.getStringList("commands");
        item.setItemMeta(baseMeta);
        for (int slot : slots) targetMap.put(slot - 1, commands);
    }

    private void applyQuestsItems(Player player, Map<AbstractQuest, Progression> questsMap,
                                  PlayerQuests playerQuests, Inventory inventory) {
        int i = 0;
        final Map<String, Integer> categoryUsage = new HashMap<>();
        for (Map.Entry<AbstractQuest, Progression> entry : questsMap.entrySet()) {
            final AbstractQuest quest = entry.getKey();
            final Progression progression = entry.getValue();
            final ItemStack itemStack = getQuestItem(quest, progression);
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) continue;

            configureItemMeta(itemMeta, quest, progression, player, playerQuests);
            itemStack.setItemMeta(itemMeta);
            final int menuItemAmount = quest.getMenuItemAmount();
            itemStack.setAmount(menuItemAmount == 0 ? progression.getRequiredAmount() : menuItemAmount);
            placeItemInInventory(i, resolveSlotsForQuest(quest.getCategoryName(), i, categoryUsage), itemStack, inventory);
            i++;
        }
    }

    private ItemStack getQuestItem(AbstractQuest quest, Progression progression) {
        return progression.isAchieved() ? quest.getAchievedItem().clone() : quest.getMenuItem().clone();
    }

    private void configureItemMeta(ItemMeta itemMeta, AbstractQuest quest, Progression progression,
                                   Player player, PlayerQuests playerQuests) {
        String displayName = TextFormatter.format(player, quest.getQuestName());
        itemMeta.setDisplayName(QuestPlaceholders.replaceQuestPlaceholders(
                displayName, player, quest, progression, playerQuests, null));
        itemMeta.setLore(generateLore(quest, progression, player, playerQuests));

        if (progression.isAchieved() && isGlowingEnabled) {
            itemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
        }

        // HIDE_ATTRIBUTES is already included by ItemFlag.values(); the old zero-value
        // AttributeModifier was redundant and used an API marked for removal.
        itemMeta.addItemFlags(ItemFlag.values());
    }

    private List<String> generateLore(AbstractQuest quest, Progression progression, Player player,
                                      PlayerQuests playerQuests) {
        final List<String> lore = new ArrayList<>(quest.getQuestDesc());
        final String status = getQuestStatus(progression, player);
        final ListIterator<String> iterator = lore.listIterator();
        while (iterator.hasNext()) {
            String formatted = QuestPlaceholders.replaceQuestPlaceholders(
                    iterator.next(), player, quest, progression, playerQuests, status);
            iterator.set(TextFormatter.format(player, formatted));
        }

        if (statusStr != null && !statusStr.isEmpty() && !isStatusDisabled) lore.add(TextFormatter.format(player, statusStr));
        if (progression.isAchieved() && achievedStr != null && !achievedStr.isEmpty() && !isStatusDisabled) {
            lore.add(TextFormatter.format(achievedStr));
        } else if (progressStr != null && !progressStr.isEmpty() && !isStatusDisabled) {
            lore.add(TextFormatter.format(QuestPlaceholders.replaceQuestPlaceholders(
                    TextFormatter.format(player, progressStr), player, quest, progression, playerQuests, status)));
        }

        if (shouldDisplayManualCompletionHint(progression)) {
            final String hint = getCompleteGetTypeStr();
            if (hint != null && !hint.isEmpty()) lore.add(TextFormatter.format(player, hint));
        }
        return lore;
    }

    private void placeItemInInventory(int questIndex, List<Integer> slots, ItemStack itemStack, Inventory inventory) {
        if (slots == null) {
            PluginLogger.error(ERROR_OCCURRED + "Slot not defined for quest " + (questIndex + 1));
            return;
        }
        for (int slot : slots) {
            if (slot > 0 && slot <= size) inventory.setItem(slot - 1, itemStack);
            else PluginLogger.error(ERROR_OCCURRED + "Slot " + slot + " for quest " + (questIndex + 1) + OUT_OF_BOUNDS);
        }
    }

    private @Nullable List<Integer> resolveSlotsForQuest(String categoryName, int questIndex,
                                                         Map<String, Integer> categoryUsage) {
        if (!categorySlots.isEmpty()) {
            final String key = categoryName.toLowerCase(Locale.ROOT);
            final List<Integer> slots = categorySlots.get(key);
            if (slots == null || slots.isEmpty()) {
                PluginLogger.error(ERROR_OCCURRED + "Slot not defined for category " + categoryName + ".");
                return null;
            }
            final int usage = categoryUsage.getOrDefault(key, 0);
            if (usage >= slots.size()) {
                PluginLogger.error(ERROR_OCCURRED + "Not enough slots configured for category " + categoryName + ".");
                return null;
            }
            categoryUsage.put(key, usage + 1);
            return Collections.singletonList(slots.get(usage));
        }
        return slotQuests.get(questIndex);
    }

    private void applyPapiItems(Player player, PlayerQuests playerQuests, Inventory inventory) {
        for (Map.Entry<Integer, ItemStack> entry : papiItems.entrySet()) {
            final int slot = entry.getKey();
            final ItemStack itemCopy = entry.getValue().clone();
            if (slot < 0 || slot >= size) {
                PluginLogger.error(ERROR_OCCURRED + "An item with placeholders defined for slot " + (slot + 1) + OUT_OF_BOUNDS);
                continue;
            }

            final ItemMeta meta = itemCopy.getItemMeta();
            if (meta == null) continue;
            if (meta.hasDisplayName()) meta.setDisplayName(TextFormatter.format(player, meta.getDisplayName()));
            final List<String> lore = meta.getLore();
            if (lore != null) {
                final ListIterator<String> iterator = lore.listIterator();
                while (iterator.hasNext()) {
                    final String formatted = TextFormatter.format(player, iterator.next());
                    iterator.set(QuestPlaceholders.replaceQuestPlaceholders(
                            formatted, player, null, null, playerQuests, null));
                }
            }
            meta.setLore(lore);
            itemCopy.setItemMeta(meta);
            inventory.setItem(slot, itemCopy);
        }
    }

    private ItemMeta getItemMeta(ItemStack itemStack, ConfigurationSection section, List<ItemFlag> flags) {
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return null;
        if (section.contains("custom_model_data")) meta.setCustomModelData(section.getInt("custom_model_data"));

        final String name = section.getString("name");
        if (name != null) meta.setDisplayName(TextFormatter.format(name));

        final List<String> lore = section.getStringList("lore");
        final ListIterator<String> iterator = lore.listIterator();
        while (iterator.hasNext()) iterator.set(TextFormatter.format(iterator.next()));
        meta.setLore(lore);

        if (flags != null && !flags.isEmpty()) meta.addItemFlags(flags.toArray(new ItemFlag[0]));
        return meta;
    }

    public String getQuestStatus(Progression progression, Player player) {
        if (progression.isAchieved()) return TextFormatter.format(player, getAchievedStr());
        if (shouldDisplayManualCompletionHint(progression)) {
            final String hint = getCompleteGetTypeStr();
            if (hint != null && !hint.isEmpty()) return TextFormatter.format(player, hint);
        }
        final String formatted = QuestPlaceholders.replaceProgressPlaceholders(
                getProgressStr(), progression.getAdvancement(), progression.getRequiredAmount(), progression.getRewardAmount());
        return TextFormatter.format(player, formatted);
    }

    private boolean shouldDisplayManualCompletionHint(Progression progression) {
        return CompleteOnlyOnClick.isEnabled() && !progression.isAchieved()
                && progression.getAdvancement() >= progression.getRequiredAmount();
    }

    public boolean isFillItem(ItemStack itemStack) { return fillItems.contains(itemStack); }
    public boolean isCloseItem(ItemStack itemStack) { return closeItems.contains(itemStack); }
    public boolean isPlayerCommandItem(int slot) { return playerCommandsItems.containsKey(slot); }
    public boolean isConsoleCommandItem(int slot) { return consoleCommandsItems.containsKey(slot); }
    public List<String> getPlayerCommands(int slot) { return playerCommandsItems.get(slot); }
    public List<String> getConsoleCommands(int slot) { return consoleCommandsItems.get(slot); }
    public String getAchievedStr() { return achievedStr; }
    public String getProgressStr() { return progressStr; }
    public String getCompleteGetTypeStr() { return completeGetTypeStr; }
    public boolean shouldCloseOnClick(int slot) { return closeOnClickSlots.contains(slot); }
}
