package com.ordwen.odailyquests.quests.types.item;

import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.shared.BasicQuest;
import com.ordwen.odailyquests.quests.types.shared.ItemQuest;
// import com.ordwen.odailyquests.tools.PluginUtils;
// import net.Indyuce.mmoitems.api.event.CraftMMOItemEvent;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.inventory.*;

public class CraftQuest extends ItemQuest {

    private static final String PAPER_ITEM_CRAFTED_EVENT =
            "io.papermc.paper.event.inventory.ItemCraftedEvent";

    public CraftQuest(BasicQuest base) {
        super(base);
    }

    @Override
    public String getType() {
        return "CRAFT";
    }

    @Override
    public boolean canProgress(Event provided, Progression progression) {
        if (provided instanceof CraftItemEvent event) {
            final ItemStack item;
            if (event.getRecipe() instanceof ComplexRecipe complexRecipe) {
                item = new ItemStack(Material.valueOf(complexRecipe.getKey().getKey().toUpperCase()));
            } else {
                final ItemStack result = event.getCurrentItem();
                if (result == null) return false;
                item = result.clone();
            }
            return super.isRequiredItem(item, progression);
        }

        /*
         * Paper's ItemCraftedEvent is intentionally handled reflectively so this
         * plugin remains loadable on Bukkit/Spigot and older supported runtimes
         * where that Paper API class is not present.
         *
         * PaperItemCraftedListener passes the real post-craft event here. Without
         * this branch every CRAFT quest is rejected on runtimes where that listener
         * replaces CraftItemListener.
         */
        if (PAPER_ITEM_CRAFTED_EVENT.equals(provided.getClass().getName())) {
            final ItemStack item = getPaperCraftedItem(provided);
            return item != null && super.isRequiredItem(item, progression);
        }

        if (provided instanceof SmithItemEvent event) {
            final ItemStack item = event.getCurrentItem();
            return super.isRequiredItem(item, progression);
        }

//        if (PluginUtils.isPluginEnabled("MMOItems") && provided instanceof CraftMMOItemEvent event) {
//            final ItemStack item = event.getResult();
//            return  super.isRequiredItem(item, progression);
//        }

        return false;
    }

    private ItemStack getPaperCraftedItem(Event event) {
        try {
            final Object rawItem = event.getClass().getMethod("getCraftedItem").invoke(event);
            return rawItem instanceof ItemStack item ? item.clone() : null;
        } catch (ReflectiveOperationException | LinkageError exception) {
            Debugger.write("CraftQuest: unable to read Paper ItemCraftedEvent result: "
                    + exception.getMessage());
            return null;
        }
    }
}
