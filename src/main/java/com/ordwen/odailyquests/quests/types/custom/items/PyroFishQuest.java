package com.ordwen.odailyquests.quests.types.custom.items;

import com.ordwen.odailyquests.events.listeners.integrations.pyrofishingpro.PyroFishCatchListener;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.quests.types.shared.BasicQuest;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class PyroFishQuest extends AbstractQuest {

    private final Set<String> expectedFish = new HashSet<>();

    public PyroFishQuest(final BasicQuest basicQuest) {
        super(basicQuest);
    }

    @Override
    public String getType() {
        return "PYRO_FISH";
    }

    @Override
    public boolean canProgress(final Event provided, final Progression progression) {
        if (provided == null || !provided.getClass().getName().equals(PyroFishCatchListener.EVENT_CLASS_NAME)) {
            return false;
        }

        if (expectedFish.isEmpty()) {
            return true;
        }

        final String caughtFish = getCaughtFishKey(provided);
        return caughtFish != null && expectedFish.contains(caughtFish);
    }

    @Override
    public boolean loadParameters(final ConfigurationSection section, final String file, final String index) {
        expectedFish.clear();

        if (!Bukkit.getPluginManager().isPluginEnabled("PyroFishingPro")) {
            PluginLogger.configurationError(file, index, null, "You must have PyroFishingPro installed to use this quest.");
            return false;
        }

        if (section.isList("required")) {
            for (String fish : section.getStringList("required")) {
                if (checkFormat(fish, file, index)) {
                    expectedFish.add(fish.toLowerCase());
                }
            }
        } else if (section.isString("required")) {
            final String fish = section.getString("required");
            if (checkFormat(fish, file, index)) {
                expectedFish.add(fish.toLowerCase());
            }
        }

        return true;
    }

    private String getCaughtFishKey(final Event event) {
        try {
            final Method getTier = event.getClass().getMethod("getTier");
            final Method getFishNumber = event.getClass().getMethod("getFishNumber");

            final Object tierObject = getTier.invoke(event);
            final Object fishNumberObject = getFishNumber.invoke(event);

            if (tierObject == null || fishNumberObject == null) {
                return null;
            }

            return tierObject.toString().toLowerCase() + ":" + fishNumberObject;
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Unable to read PyroFishingPro fish data from PyroFishCatchEvent.");
            return null;
        }
    }

    private boolean checkFormat(final String fish, final String file, final String index) {
        if (fish == null || fish.isBlank()) {
            PluginLogger.configurationError(file, index, "required", "Invalid fish format: " + fish);
            return false;
        }

        final String[] split = fish.split(":");
        if (split.length != 2 || split[0].isBlank() || split[1].isBlank()) {
            PluginLogger.configurationError(file, index, "required", "Invalid fish format: " + fish + ". Expected format: <tier>:<id>");
            return false;
        }

        try {
            Integer.parseInt(split[1]);
        } catch (NumberFormatException exception) {
            PluginLogger.configurationError(file, index, "required", "Invalid fish id in: " + fish + ". The id must be a number.");
            return false;
        }

        return true;
    }
}
