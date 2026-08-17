package com.ordwen.odailyquests.configuration.essentials;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.ConfigFactory;
import com.ordwen.odailyquests.configuration.IConfigurable;
import com.ordwen.odailyquests.files.implementations.ConfigurationFile;
import com.ordwen.odailyquests.quests.types.global.CustomQuest;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CustomTypes implements IConfigurable {

    public static final String SLIMEFUN_ITEM = "SLIMEFUN_ITEM";

    private final ConfigurationFile configurationFile;
    private final Set<String> types = new HashSet<>();

    public CustomTypes(ConfigurationFile configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Override
    public void load() {
        types.clear();

        // Built into the maintained fork. Slimefun itself remains a soft dependency.
        types.add(SLIMEFUN_ITEM);
        ODailyQuests.INSTANCE.registerQuestType(SLIMEFUN_ITEM, CustomQuest.class);

        for (String customType : configurationFile.getConfig().getStringList("custom_types")) {
            if (customType == null || customType.isBlank()) continue;
            String normalized = customType.trim().toUpperCase(Locale.ROOT);
            types.add(normalized);
            ODailyQuests.INSTANCE.registerQuestType(normalized, CustomQuest.class);
        }
    }

    private static CustomTypes getInstance() {
        return ConfigFactory.getConfig(CustomTypes.class);
    }

    public static Set<String> getCustomTypes() {
        return getInstance().types;
    }
}
