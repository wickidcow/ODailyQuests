package com.ordwen.odailyquests.quests;

import com.ordwen.odailyquests.api.quests.QuestTypeRegistry;
import com.ordwen.odailyquests.quests.features.MaintainedQuestTypes;
import com.ordwen.odailyquests.quests.types.custom.items.EMFFishQuest;
import com.ordwen.odailyquests.quests.types.global.CustomQuest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MaintainedQuestTypesTest {

    @Test
    void widerCompatibilityQuestTypesAreAlwaysRegistered() {
        QuestTypeRegistry registry = new QuestTypeRegistry();
        MaintainedQuestTypes.register(registry);

        assertEquals(7, MaintainedQuestTypes.requiredTypeNames().size());
        assertSame(EMFFishQuest.class, registry.getMainClass("EMF_FISH"));
        for (String type : new String[]{
                "SLIMEFUN_ITEM", "SLIMEFUN_CRAFT", "REBAR_ITEM", "MCMMO_EXP", "MMOITEM_ITEM", "ITEMSADDER_ITEM"
        }) {
            assertSame(CustomQuest.class, registry.getMainClass(type), "Wrong maintained implementation for " + type);
        }
    }
}
