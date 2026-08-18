package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.api.quests.QuestTypeRegistry;
import com.ordwen.odailyquests.quests.types.custom.items.EMFFishQuest;
import com.ordwen.odailyquests.quests.types.global.CustomQuest;

import java.util.List;

/** Registers quest types added by the maintained wider-compatibility expansion. */
public final class MaintainedQuestTypes {

    private static final List<String> CUSTOM_TYPES = List.of(
            "SLIMEFUN_ITEM",
            "SLIMEFUN_CRAFT",
            "REBAR_ITEM",
            "MCMMO_EXP",
            "MMOITEM_ITEM",
            "ITEMSADDER_ITEM"
    );

    private MaintainedQuestTypes() {}

    public static void register(QuestTypeRegistry registry) {
        registry.registerQuestType("EMF_FISH", EMFFishQuest.class);
        for (String type : CUSTOM_TYPES) {
            registry.registerQuestType(type, CustomQuest.class);
        }
    }

    public static List<String> requiredTypeNames() {
        return List.of(
                "EMF_FISH",
                "SLIMEFUN_ITEM",
                "SLIMEFUN_CRAFT",
                "REBAR_ITEM",
                "MCMMO_EXP",
                "MMOITEM_ITEM",
                "ITEMSADDER_ITEM"
        );
    }
}
