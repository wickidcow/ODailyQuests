package com.ordwen.odailyquests.quests.types.global;

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

import java.util.List;

public class CustomQuest extends AbstractQuest {

    public CustomQuest(BasicQuest base) {
        super(base);
    }

    @Override
    public String getType() {
        return this.getQuestType();
    }

    @Override
    public boolean canProgress(@Nullable Event provided, Progression progression) {
        if (!"SLIMEFUN_ITEM".equalsIgnoreCase(getQuestType())) return true;
        if (provided == null) return false;

        ItemStack stack = null;
        if (provided instanceof EntityPickupItemEvent pickup) stack = pickup.getItem().getItemStack();
        else if (provided instanceof CraftItemEvent craft) stack = craft.getCurrentItem();

        String id = SlimefunIntegration.getItemId(stack);
        if (id == null) return false;
        List<String> requiredIds = QuestFeatures.slimefunIds(this);
        return requiredIds.isEmpty() || requiredIds.contains(id);
    }

    @Override
    public boolean loadParameters(ConfigurationSection section, String file, String index) {
        return true;
    }
}
