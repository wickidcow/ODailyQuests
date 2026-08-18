package com.ordwen.odailyquests.quests.types.global;

import com.ordwen.odailyquests.events.listeners.integrations.ExternalItemIntegration;
import com.ordwen.odailyquests.events.listeners.integrations.PylonInventoryGainEvent;
import com.ordwen.odailyquests.events.listeners.integrations.slimefun.SlimefunIntegration;
import com.ordwen.odailyquests.quests.features.QuestFeatures;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.quests.types.shared.BasicQuest;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomQuest extends AbstractQuest {

    private static final String MULTIBLOCK_CRAFT_EVENT = "io.github.thebusybiscuit.slimefun4.api.events.MultiBlockCraftEvent";
    private static final String MCMMO_XP_EVENT = "com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent";

    private String requiredSkill = "ANY";
    private List<String> requiredPylonKeys = List.of();
    private boolean allowPylonInventoryGain;

    public CustomQuest(BasicQuest base) {
        super(base);
    }

    @Override
    public String getType() {
        return this.getQuestType();
    }

    @Override
    public boolean canProgress(@Nullable Event provided, Progression progression) {
        String type = getQuestType();
        boolean slimefunItemQuest = "SLIMEFUN_ITEM".equalsIgnoreCase(type);
        boolean slimefunCraftQuest = "SLIMEFUN_CRAFT".equalsIgnoreCase(type);
        boolean rebarItemQuest = "REBAR_ITEM".equalsIgnoreCase(type);
        boolean mmoItemQuest = "MMOITEM_ITEM".equalsIgnoreCase(type);
        boolean itemsAdderItemQuest = "ITEMSADDER_ITEM".equalsIgnoreCase(type);
        boolean mcMMOQuest = "MCMMO_EXP".equalsIgnoreCase(type);

        if (!slimefunItemQuest && !slimefunCraftQuest && !rebarItemQuest
                && !mmoItemQuest && !itemsAdderItemQuest && !mcMMOQuest) {
            return true;
        }

        if (mcMMOQuest) return matchesMcMMOSkill(provided);
        if (provided == null) return false;

        if (slimefunCraftQuest
                && !(provided instanceof CraftItemEvent)
                && !MULTIBLOCK_CRAFT_EVENT.equals(provided.getClass().getName())) {
            return false;
        }

        if (rebarItemQuest && provided instanceof PylonInventoryGainEvent && !allowPylonInventoryGain) {
            return false;
        }

        ItemStack stack = extractStack(provided);
        if (stack == null) return false;

        if (rebarItemQuest) return matchesPylonItem(stack);
        if (mmoItemQuest) return ExternalItemIntegration.isMMOItem(stack);
        if (itemsAdderItemQuest) return ExternalItemIntegration.isItemsAdderItem(stack);

        String id = SlimefunIntegration.getItemId(stack);
        if (id == null) return false;

        List<String> requiredIds = QuestFeatures.slimefunIds(this);
        if (!requiredIds.isEmpty() && !requiredIds.contains(id)) return false;

        List<String> addonAliases = QuestFeatures.slimefunAddons(this);
        return addonAliases.isEmpty() || SlimefunIntegration.matchesAddon(stack, addonAliases);
    }

    private boolean matchesPylonItem(ItemStack stack) {
        NamespacedKey key = ExternalItemIntegration.getRebarItemKey(stack);
        if (key == null || !"pylon".equalsIgnoreCase(key.getNamespace())) return false;
        if (requiredPylonKeys.isEmpty()) return true;
        return requiredPylonKeys.contains(key.toString().toLowerCase(Locale.ROOT));
    }

    private boolean matchesMcMMOSkill(@Nullable Event event) {
        if (event == null || !MCMMO_XP_EVENT.equals(event.getClass().getName())) return false;
        if (requiredSkill.isBlank() || "ANY".equals(requiredSkill)) return true;

        try {
            Method getSkill = event.getClass().getMethod("getSkill");
            Object skill = getSkill.invoke(event);
            if (skill == null) return false;
            String actual = skill instanceof Enum<?> value ? value.name() : skill.toString();
            return requiredSkill.equals(actual.trim().toUpperCase(Locale.ROOT));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private ItemStack extractStack(Event event) {
        if (event instanceof EntityPickupItemEvent pickup) return pickup.getItem().getItemStack();
        if (event instanceof CraftItemEvent craft) return craft.getCurrentItem();
        if (event instanceof PylonInventoryGainEvent gain) return gain.getItemStack();

        if (MULTIBLOCK_CRAFT_EVENT.equals(event.getClass().getName())) {
            try {
                Method getOutput = event.getClass().getMethod("getOutput");
                Object result = getOutput.invoke(event);
                return result instanceof ItemStack stack ? stack : null;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean loadParameters(ConfigurationSection section, String file, String index) {
        requiredSkill = section.getString(".skill", "ANY").trim().toUpperCase(Locale.ROOT);
        requiredPylonKeys = loadPylonKeys(section);
        allowPylonInventoryGain = section.getBoolean(".pylon_inventory_gain", false);
        return true;
    }

    private List<String> loadPylonKeys(ConfigurationSection section) {
        List<String> raw = new ArrayList<>();
        if (section.isList("pylon_keys")) {
            raw.addAll(section.getStringList("pylon_keys"));
        } else if (section.isString("pylon_keys")) {
            String value = section.getString("pylon_keys");
            if (value != null) raw.add(value);
        }
        if (section.isString("pylon_key")) {
            String value = section.getString("pylon_key");
            if (value != null) raw.add(value);
        }

        List<String> normalized = new ArrayList<>();
        for (String value : raw) {
            if (value == null || value.isBlank()) continue;
            String key = value.trim().toLowerCase(Locale.ROOT);
            if (!key.contains(":")) key = "pylon:" + key;
            normalized.add(key);
        }
        return List.copyOf(normalized);
    }
}
