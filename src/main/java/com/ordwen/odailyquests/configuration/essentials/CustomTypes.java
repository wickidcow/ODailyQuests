package com.ordwen.odailyquests.configuration.essentials;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.ConfigFactory;
import com.ordwen.odailyquests.configuration.IConfigurable;
import com.ordwen.odailyquests.files.implementations.ConfigurationFile;
import com.ordwen.odailyquests.quests.types.custom.items.EMFFishQuest;
import com.ordwen.odailyquests.quests.types.global.CustomQuest;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CustomTypes implements IConfigurable {

    public static final String SLIMEFUN_ITEM = "SLIMEFUN_ITEM";
    public static final String SLIMEFUN_CRAFT = "SLIMEFUN_CRAFT";
    public static final String REBAR_ITEM = "REBAR_ITEM";
    public static final String MMOITEM_ITEM = "MMOITEM_ITEM";
    public static final String ITEMSADDER_ITEM = "ITEMSADDER_ITEM";
    public static final String MCMMO_EXP = "MCMMO_EXP";
    public static final String EMF_FISH = "EMF_FISH";

    private final ConfigurationFile configurationFile;
    private final Set<String> types = new HashSet<>();

    public CustomTypes(ConfigurationFile configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Override
    public void load() {
        types.clear();

        // Built into the maintained fork. Their external plugins remain soft dependencies.
        registerBuiltin(SLIMEFUN_ITEM, CustomQuest.class);
        registerBuiltin(SLIMEFUN_CRAFT, CustomQuest.class);
        registerBuiltin(REBAR_ITEM, CustomQuest.class);
        registerBuiltin(MMOITEM_ITEM, CustomQuest.class);
        registerBuiltin(ITEMSADDER_ITEM, CustomQuest.class);
        registerBuiltin(MCMMO_EXP, CustomQuest.class);

        // Use the existing concrete EMF implementation so manually configured fish filters
        // continue to work; an empty required list still means "any EvenMoreFish fish".
        registerBuiltin(EMF_FISH, EMFFishQuest.class);

        for (String customType : configurationFile.getConfig().getStringList("custom_types")) {
            if (customType == null || customType.isBlank()) continue;
            String normalized = customType.trim().toUpperCase(Locale.ROOT);
            types.add(normalized);
            ODailyQuests.INSTANCE.registerQuestType(normalized, CustomQuest.class);
        }
    }

    private void registerBuiltin(String type, Class<? extends com.ordwen.odailyquests.quests.types.AbstractQuest> questClass) {
        types.add(type);
        ODailyQuests.INSTANCE.registerQuestType(type, questClass);
    }

    private static CustomTypes getInstance() {
        return ConfigFactory.getConfig(CustomTypes.class);
    }

    public static Set<String> getCustomTypes() {
        return getInstance().types;
    }
}
