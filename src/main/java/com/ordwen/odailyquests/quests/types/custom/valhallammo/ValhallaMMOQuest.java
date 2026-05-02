package com.ordwen.odailyquests.quests.types.custom.valhallammo;

import com.ordwen.odailyquests.events.listeners.integrations.valhallammo.ValhallaMMOProgressListener;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.quests.types.shared.BasicQuest;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.Locale;

public class ValhallaMMOQuest extends AbstractQuest {

    private final String questType;
    private String requiredSkill;

    public ValhallaMMOQuest(final BasicQuest basicQuest, final String questType) {
        super(basicQuest);
        this.questType = questType;
    }

    @Override
    public String getType() {
        return questType;
    }

    @Override
    public boolean canProgress(final Event provided, final Progression progression) {
        if (provided == null) {
            return false;
        }

        if ("VALHALLA_EXP".equalsIgnoreCase(questType)
                && !provided.getClass().getName().equals(ValhallaMMOProgressListener.EXP_EVENT_CLASS_NAME)) {
            return false;
        }

        if ("VALHALLA_LEVEL_UP".equalsIgnoreCase(questType)
                && !provided.getClass().getName().equals(ValhallaMMOProgressListener.LEVEL_UP_EVENT_CLASS_NAME)) {
            return false;
        }

        if (requiredSkill == null || requiredSkill.isBlank() || requiredSkill.equalsIgnoreCase("ANY")) {
            return true;
        }

        final String eventSkill = getEventSkill(provided);

        return eventSkill != null && eventSkill.equalsIgnoreCase(requiredSkill);
    }

    @Override
    public boolean loadParameters(final ConfigurationSection section, final String file, final String index) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ValhallaMMO")) {
            PluginLogger.configurationError(file, index, null, "You must have ValhallaMMO installed to use this quest.");
            return false;
        }

        requiredSkill = section.getString("skill", "ANY");

        if (requiredSkill != null) {
            requiredSkill = normalize(requiredSkill);
        }

        return true;
    }

    private String getEventSkill(final Event event) {
        try {
            final Object skillObject;

            if (event.getClass().getName().equals(ValhallaMMOProgressListener.EXP_EVENT_CLASS_NAME)) {
                final Method getLeveledSkillMethod = event.getClass().getMethod("getLeveledSkill");
                skillObject = getLeveledSkillMethod.invoke(event);
            } else if (event.getClass().getName().equals(ValhallaMMOProgressListener.LEVEL_UP_EVENT_CLASS_NAME)) {
                final Method getSkillMethod = event.getClass().getMethod("getSkill");
                skillObject = getSkillMethod.invoke(event);
            } else {
                return null;
            }

            if (skillObject == null) {
                return null;
            }

            try {
                final Method getTypeMethod = skillObject.getClass().getMethod("getType");
                final Object typeObject = getTypeMethod.invoke(skillObject);

                if (typeObject != null) {
                    return normalize(typeObject.toString());
                }
            } catch (ReflectiveOperationException ignored) {
                // Fallback below.
            }

            return normalize(skillObject.toString());
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Unable to read ValhallaMMO skill from event.");
            return null;
        }
    }

    private String normalize(final String value) {
        return value
                .toLowerCase(Locale.ROOT)
                .replace(" ", "_")
                .replace("-", "_")
                .trim();
    }
}
