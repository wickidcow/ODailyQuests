package com.ordwen.odailyquests.quests.types.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftQuestTest {

    @Test
    void plainMaterialRequirementAcceptsMetadataDecoratedCraftResult() {
        final ItemStack required = new ItemStack(Material.CROSSBOW);
        final ItemStack provided = new ItemStack(Material.CROSSBOW) {
            @Override
            public boolean hasItemMeta() {
                return true;
            }
        };

        assertTrue(CraftQuest.isPlainMaterialMatch(required, provided),
                "A plain CROSSBOW requirement should accept a plugin-modified CROSSBOW result");
    }

    @Test
    void metadataSpecificRequirementDoesNotFallBackToMaterialOnly() {
        final ItemStack required = new ItemStack(Material.CROSSBOW) {
            @Override
            public boolean hasItemMeta() {
                return true;
            }
        };
        final ItemStack provided = new ItemStack(Material.CROSSBOW);

        assertFalse(CraftQuest.isPlainMaterialMatch(required, provided),
                "Meta-specific requirements must remain strict");
    }

    @Test
    void differentMaterialNeverMatches() {
        final ItemStack required = new ItemStack(Material.CROSSBOW);
        final ItemStack provided = new ItemStack(Material.BOW);

        assertFalse(CraftQuest.isPlainMaterialMatch(required, provided));
    }
}
