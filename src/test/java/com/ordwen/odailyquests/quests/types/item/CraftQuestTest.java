package com.ordwen.odailyquests.quests.types.item;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftQuestTest {

    @Test
    void plainMaterialRequirementAcceptsMetadataDecoratedCraftResult() {
        assertTrue(CraftQuest.isPlainMaterialMatch(
                        Material.CROSSBOW,
                        false,
                        Material.CROSSBOW),
                "A plain CROSSBOW requirement should accept a plugin-modified CROSSBOW result");
    }

    @Test
    void metadataSpecificRequirementDoesNotFallBackToMaterialOnly() {
        assertFalse(CraftQuest.isPlainMaterialMatch(
                        Material.CROSSBOW,
                        true,
                        Material.CROSSBOW),
                "Meta-specific requirements must remain strict");
    }

    @Test
    void differentMaterialNeverMatches() {
        assertFalse(CraftQuest.isPlainMaterialMatch(
                Material.CROSSBOW,
                false,
                Material.BOW));
    }
}
