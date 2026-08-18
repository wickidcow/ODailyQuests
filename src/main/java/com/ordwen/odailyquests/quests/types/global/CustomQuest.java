package com.ordwen.odailyquests.quests.types.global;

import com.ordwen.odailyquests.events.listeners.integrations.ExternalItemIntegration;
import com.ordwen.odailyquests.events.listeners.integrations.slimefun.SlimefunIntegration;
import com.ordwen.odailyquests.quests.features.QuestFeatures;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.quests.types.shared.BasicQuest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public class CustomQuest extends AbstractQuest {

    private static final String MULTIBLOCK_CRAFT_EVENT = "io.github.thebusybiscuit.slimefun4.api.events.MultiBlockCraftEvent";

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

        // Command-completed/custom types keep the historical permissive behavior.
        if (!slimefunItemQuest && !slimefunCraftQuest && !rebarItemQuest
                && !mmoItemQuest && !itemsAdderItemQuest && !mcMMOQuest) {
            return true;
        }

        if (mcMMOQuest) {
            return provided != null
                    && "com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent".equals(provided.getClass().getName());
        }

        if (provided == null) return false;

        if (slimefunCraftQuest
                && !(provided instanceof CraftItemEvent)
                && !MULTIBLOCK_CRAFT_EVENT.equals(provided.getClass().getName())) {
            return false;
        }

        ItemStack stack = extractStack(provided);
        if (stack == null) return false;

        if (rebarItemQuest) return ExternalItemIntegration.isPylonItem(stack);
        if (mmoItemQuest) return ExternalItemIntegration.isMMOItem(stack);
        if (itemsAdderItemQuest) return ExternalItemIntegration.isItemsAdderItem(stack);

        String id = SlimefunIntegration.getItemId(stack);
        if (id == null) return false;

        List<String> requiredIds = QuestFeatures.slimefunIds(this);
        if (!requiredIds.isEmpty() && !requiredIds.contains(id)) return false;

        List<String> addonAliases = QuestFeatures.slimefunAddons(this);
        return addonAliases.isEmpty() || SlimefunIntegration.matchesAddon(stack, addonAliases);
    }

    private ItemStack extractStack(Event event) {
        if (event instanceof EntityPickupItemEvent pickup) return pickup.getItem().getItemStack();
        if (event instanceof CraftItemEvent craft) return craft.getCurrentItem();

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
        return true;
    }
}
