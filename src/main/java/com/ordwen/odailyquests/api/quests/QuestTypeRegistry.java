package com.ordwen.odailyquests.api.quests;

import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.tools.PluginLogger;

import java.util.HashMap;

/** Registry for built-in and addon quest types. */
public class QuestTypeRegistry extends HashMap<String, Class<? extends AbstractQuest>> {

    /**
     * Registers a quest type without silently replacing an existing implementation.
     * Registering the same class twice is treated as an idempotent no-op so legacy setup
     * code and reloads remain compatible.
     */
    public void registerQuestType(String type, Class<? extends AbstractQuest> questClass) {
        if (type == null || type.isBlank() || questClass == null) {
            PluginLogger.warn("Ignored an invalid quest type registration.");
            return;
        }

        final String normalized = type.trim().toUpperCase();
        final Class<? extends AbstractQuest> existing = this.putIfAbsent(normalized, questClass);
        if (existing != null && !existing.equals(questClass)) {
            PluginLogger.warn("Quest type '" + normalized + "' is already registered to "
                    + existing.getName() + "; refusing conflicting registration from " + questClass.getName() + ".");
        }
    }

    /** Explicit override for addon authors that intentionally replace a type. */
    public void replaceQuestType(String type, Class<? extends AbstractQuest> questClass) {
        if (type == null || type.isBlank() || questClass == null) return;
        this.put(type.trim().toUpperCase(), questClass);
    }

    public Class<? extends AbstractQuest> getMainClass(String type) {
        return type == null ? null : this.get(type.trim().toUpperCase());
    }
}
