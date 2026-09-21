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

import java.util.List;

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
            return isRequiredCraftResult(item, progression);
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
            return item != null && isRequiredCraftResult(item, progression);
        }

        if (provided instanceof SmithItemEvent event) {
            final ItemStack item = event.getCurrentItem();
            return item != null && isRequiredCraftResult(item, progression);
        }

//        if (PluginUtils.isPluginEnabled("MMOItems") && provided instanceof CraftMMOItemEvent event) {
//            final ItemStack item = event.getResult();
//            return  isRequiredCraftResult(item, progression);
//        }

        return false;
    }

    /**
     * Craft quests are allowed to match a plain material requirement even when
     * another plugin decorates the crafted result with lore, PDC data, quality,
     * attributes, or other metadata.
     *
     * <p>This matters for integrations such as ValhallaMMO, which can transform
     * a vanilla equipment result after crafting. A quest configured simply as
     * CROSSBOW should still mean "craft a crossbow", not "craft a pristine
     * metadata-free crossbow". Custom/meta-specific quest requirements remain
     * strict because the material-only fallback is only used when the configured
     * required ItemStack has no item metadata.</p>
     */
    private boolean isRequiredCraftResult(ItemStack provided, Progression progression) {
        if (super.isRequiredItem(provided, progression)) {
            return true;
        }

        final List<ItemStack> requiredItems = getRequiredItems();
        if (requiredItems == null || requiredItems.isEmpty()) {
            return true;
        }

        if (isRandomRequired()) {
            final int selected = progression.getSelectedRequiredIndex();
            if (selected < 0 || selected >= requiredItems.size()) {
                return false;
            }
            return isPlainMaterialMatch(requiredItems.get(selected), provided);
        }

        for (ItemStack required : requiredItems) {
            if (isPlainMaterialMatch(required, provided)) {
                return true;
            }
        }

        return false;
    }

    static boolean isPlainMaterialMatch(ItemStack required, ItemStack provided) {
        if (required == null || provided == null) {
            return false;
        }

        if (required.getType() != provided.getType()) {
            return false;
        }

        // Preserve strict matching for custom/model/PDC/potion/meta-specific requirements.
        if (required.hasItemMeta()) {
            return false;
        }

        Debugger.write("CraftQuest: accepting metadata-modified crafted result by material: "
                + provided.getType() + ".");
        return true;
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
