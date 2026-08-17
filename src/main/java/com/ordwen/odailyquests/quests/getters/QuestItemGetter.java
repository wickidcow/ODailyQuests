package com.ordwen.odailyquests.quests.getters;

import com.ordwen.odailyquests.commands.interfaces.playerinterface.items.getters.ItemGetter;
import com.ordwen.odailyquests.tools.TextFormatter;
import com.ordwen.odailyquests.tools.Pair;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.List;

public class QuestItemGetter extends ItemGetter implements IQuestItem {

    @Override
    public ItemStack getItem(String material, String fileName, String questIndex, String parameter) {
        final String[] split = material.split(":", 2);
        return switch (split[0]) {
            case "oraxen" -> this.getOraxenItem(split[1], fileName, questIndex, parameter);
            case "nexo" -> this.getNexoItem(split[1], fileName, questIndex, parameter);
            case "itemsadder" -> this.getItemsAdderItem(split[1], fileName, questIndex, parameter);
            case "mmoitems" -> this.getMMOItemsItem(split[1], fileName, questIndex, parameter);
            case "customhead" -> this.getCustomHead(split[1], fileName, questIndex, parameter);
            case "custommodeldata" -> this.getCustomModelDataItem(split[1], fileName, questIndex, parameter);
            case "itemmodel" -> this.getItemModelItem(split[1], fileName, questIndex, parameter);
            default -> null;
        };
    }

    @Override
    public ItemStack getOraxenItem(String namespace, String fileName, String questIndex, String parameter) {
        final Pair<String, ItemStack> result = super.getOraxenItem(namespace);
        if (!result.first().isEmpty()) {
            PluginLogger.configurationError(fileName, questIndex, parameter, result.first());
            return null;
        }
        return result.second();
    }

    @Override
    public ItemStack getNexoItem(String namespace, String fileName, String questIndex, String parameter) {
        final Pair<String, ItemStack> result = super.getNexoItem(namespace);
        if (!result.first().isEmpty()) {
            PluginLogger.configurationError(fileName, questIndex, parameter, result.first());
            return null;
        }
        return result.second();
    }

    @Override
    public ItemStack getItemsAdderItem(String namespace, String fileName, String questIndex, String parameter) {
        final Pair<String, ItemStack> result = super.getItemsAdderItem(namespace);
        if (!result.first().isEmpty()) {
            PluginLogger.configurationError(fileName, questIndex, parameter, result.first());
            return null;
        }
        return result.second();
    }

    @Override
    public ItemStack getMMOItemsItem(String namespace, String fileName, String questIndex, String parameter) {
        final Pair<String, ItemStack> result = super.getMMOItemsItem(namespace);
        if (!result.first().isEmpty()) {
            PluginLogger.configurationError(fileName, questIndex, parameter, result.first());
            return null;
        }
        return result.second();
    }

    @Override
    public ItemStack getCustomModelDataItem(String customModelData, String fileName, String questIndex, String parameter) {
        final String[] split = customModelData.split(":");
        if (split.length != 2) {
            PluginLogger.configurationError(fileName, questIndex, parameter, "You need to provide the item and the custom model data.");
            return null;
        }

        final Material material = Material.getMaterial(split[0].toUpperCase());
        if (material == null) {
            PluginLogger.configurationError(fileName, questIndex, parameter, "The material " + split[0] + " does not exist.");
            return null;
        }

        int cmd;
        try {
            cmd = Integer.parseInt(split[1]);
        } catch (Exception e) {
            PluginLogger.configurationError(fileName, questIndex, parameter, split[1] + " is not a number!");
            return null;
        }

        final Pair<String, ItemStack> result = super.getCustomModelDataItem(material, cmd);
        if (!result.first().isEmpty()) {
            PluginLogger.configurationError(fileName, questIndex, parameter, result.first());
            return null;
        }
        return result.second();
    }

    @Override
    public ItemStack getCustomHead(String texture, String fileName, String questIndex, String parameter) {
        final Pair<String, ItemStack> result = super.getCustomHead(texture);
        if (!result.first().isEmpty()) {
            PluginLogger.configurationError(fileName, questIndex, parameter, result.first());
            return null;
        }
        return result.second();
    }

    public ItemStack loadCustomItem(ConfigurationSection provided, String file, String index) {
        final ConfigurationSection section = provided.getConfigurationSection(".custom_item");
        if (section == null) {
            PluginLogger.configurationError(file, index, null, "The custom item is not defined.");
            return null;
        }

        final String type = section.getString(".type");
        if (type == null) {
            PluginLogger.configurationError(file, index, "type", "The type of the custom item is not defined.");
            return null;
        }

        final ItemStack requiredItem = getItemStackFromMaterial(type, file, index, "type (CUSTOM_ITEM)");
        if (requiredItem == null) return null;

        final ItemMeta meta = requiredItem.getItemMeta();
        if (meta == null) {
            PluginLogger.configurationError(file, index, null, "The custom item cannot have a custom name or lore.");
            return null;
        }

        meta.setDisplayName(TextFormatter.format(section.getString(".name")));
        final List<String> lore = section.getStringList(".lore");
        for (String str : lore) {
            lore.set(lore.indexOf(str), TextFormatter.format(str));
        }
        meta.setLore(lore);
        requiredItem.setItemMeta(meta);
        return requiredItem;
    }

    public ItemStack getItemStackFromMaterial(String material, String fileName, String questIndex, String parameter) {
        final ItemStack requiredItem;
        if (material.contains(":")) {
            requiredItem = getItem(material, fileName, questIndex, parameter);
            if (requiredItem == null) {
                PluginLogger.configurationError(fileName, questIndex, parameter, "Invalid material type detected.");
                return null;
            }
        } else {
            try {
                requiredItem = new ItemStack(Material.valueOf(material));
            } catch (Exception e) {
                PluginLogger.configurationError(fileName, questIndex, parameter, "Invalid material type detected.");
                return null;
            }
        }
        return requiredItem;
    }

    @Override
    public ItemStack getItemModelItem(String itemModel, String fileName, String questIndex, String parameter) {
        final Pair<String, ItemStack> result = super.getItemModelItem(itemModel);
        if (!result.first().isEmpty()) {
            PluginLogger.configurationError(fileName, questIndex, parameter, result.first());
            return null;
        }
        return result.second();
    }

    /**
     * Load potion attributes using the modern base PotionType API.
     */
    public PotionMeta loadPotionItem(ConfigurationSection section, String fileName, String questIndex, ItemStack requiredItem) {
        final ConfigurationSection potionSection = section.getConfigurationSection(".potion");
        if (potionSection == null) return null;

        final String rawType = potionSection.getString("type");
        if (rawType == null) {
            PluginLogger.configurationError(fileName, questIndex, "type", "Potion type is not defined.");
            return null;
        }

        final PotionType baseType;
        try {
            baseType = PotionType.valueOf(rawType.toUpperCase());
        } catch (IllegalArgumentException e) {
            PluginLogger.configurationError(fileName, questIndex, "type", "Invalid potion type.");
            return null;
        }

        final boolean upgraded = potionSection.getBoolean("upgraded", false);
        final boolean extended = potionSection.getBoolean("extended", false);
        if (upgraded && extended) {
            PluginLogger.configurationError(fileName, questIndex, null, "Potion cannot be both upgraded and extended.");
            return null;
        }

        String resolvedName = baseType.name();
        if (upgraded && !resolvedName.startsWith("STRONG_")) resolvedName = "STRONG_" + resolvedName;
        if (extended && !resolvedName.startsWith("LONG_")) resolvedName = "LONG_" + resolvedName;

        final PotionType resolvedType;
        try {
            resolvedType = PotionType.valueOf(resolvedName);
        } catch (IllegalArgumentException e) {
            PluginLogger.configurationError(fileName, questIndex, "potion",
                    "This potion type does not support the requested upgraded/extended state.");
            return null;
        }

        if (requiredItem.getType() != Material.POTION
                && requiredItem.getType() != Material.SPLASH_POTION
                && requiredItem.getType() != Material.LINGERING_POTION) {
            return null;
        }

        final PotionMeta potionMeta = (PotionMeta) requiredItem.getItemMeta();
        if (potionMeta == null) return null;
        potionMeta.setBasePotionType(resolvedType);
        return potionMeta;
    }
}
