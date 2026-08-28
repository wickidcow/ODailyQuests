package com.ordwen.odailyquests.commands.interfaces.playerinterface.items;

import com.ordwen.odailyquests.api.ODailyQuestsAPI;
import com.ordwen.odailyquests.commands.interfaces.playerinterface.items.getters.InterfaceItemGetter;
import com.ordwen.odailyquests.files.implementations.PlayerInterfaceFile;
import com.ordwen.odailyquests.nms.NMSHandler;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.QuestPlaceholders;
import com.ordwen.odailyquests.tools.TextFormatter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerHead extends InterfaceItemGetter {

    private static final String SLOT_PARAMETER = "slot";

    private final PlayerInterfaceFile playerInterfaceFile;

    private boolean enabled;
    private final Set<Integer> slots = new HashSet<>();

    private ItemStack head;
    private ItemMeta meta;

    public PlayerHead(PlayerInterfaceFile playerInterfaceFile) {
        this.playerInterfaceFile = playerInterfaceFile;
    }

    /**
     * Init player head / information item.
     *
     * <p>The configured material may be a normal player head or any supported external item
     * such as Nexo, ItemsAdder, Oraxen, or MMOItems. Player ownership is only applied when the
     * resulting item actually uses {@link SkullMeta}.</p>
     */
    public void load() {
        final ConfigurationSection section = playerInterfaceFile.getConfig().getConfigurationSection("player_interface.player_head");
        if (section == null) {
            PluginLogger.error("Player head section not found in the player interface file.");
            enabled = false;
            return;
        }

        enabled = section.getBoolean(".enabled");
        slots.clear();
        head = null;
        meta = null;
        if (!enabled) return;

        if (section.isString(".material")) {
            final String material = section.getString(".material");
            if (material != null) {
                head = this.getItem(material, "player_head", ".material");
            }
        }

        if (head == null) {
            head = new ItemStack(Material.PLAYER_HEAD, 1);
        }

        meta = head.getItemMeta();
        if (meta == null) {
            PluginLogger.error("Unable to load metadata for the configured player information item.");
            return;
        }

        meta.setDisplayName(TextFormatter.format(section.getString(".item_name")));
        meta.setLore(section.getStringList(".item_description"));

        if (section.isInt(".custom_model_data")) {
            meta.setCustomModelData(section.getInt(".custom_model_data"));
        }

        if (section.isString(".item_model")) {
            final String itemModel = section.getString(".item_model");
            if (itemModel != null) {
                NMSHandler.trySetItemModel(meta, itemModel);
            }
        }

        head.setItemMeta(meta);

        if (section.isList(SLOT_PARAMETER)) {
            for (int configuredSlot : section.getIntegerList(SLOT_PARAMETER)) {
                slots.add(configuredSlot - 1);
            }
        } else {
            slots.add(section.getInt(SLOT_PARAMETER) - 1);
        }
    }

    public Inventory setPlayerHead(Inventory inventory, Player player, int size) {
        if (!enabled) return inventory;

        for (int slot : slots) {
            if (slot >= 0 && slot < size) {
                inventory.setItem(slot, getPlayerHead(player));
            } else {
                PluginLogger.error("An error occurred when loading the player interface.");
                PluginLogger.error("The slot defined for the player head is out of bounds.");
            }
        }

        return inventory;
    }

    public ItemStack getPlayerHead(Player player) {
        if (head == null || meta == null) {
            return new ItemStack(Material.PLAYER_HEAD, 1);
        }

        final ItemStack rendered = head.clone();
        final ItemMeta clone = meta.clone();

        if (clone.hasDisplayName()) {
            clone.setDisplayName(TextFormatter.format(player, clone.getDisplayName()
                    .replace("%player_name%", player.getName())));
        }

        if (clone instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }

        final List<String> lore = clone.getLore();
        if (lore != null) {
            final PlayerQuests playerQuests = ODailyQuestsAPI.getPlayerQuests(player.getName());
            for (int index = 0; index < lore.size(); index++) {
                String line = TextFormatter.format(player, lore.get(index));
                line = QuestPlaceholders.replaceQuestPlaceholders(
                        TextFormatter.format(line), player, null, null, playerQuests, null);
                lore.set(index, line);
            }
            clone.setLore(lore);
        }

        rendered.setItemMeta(clone);
        return rendered;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
