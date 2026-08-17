package com.ordwen.odailyquests.quests.types.custom.crate;

import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.quests.types.shared.BasicQuest;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.PluginUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Quest completed by opening crates from ExcellentCrates.
 *
 * <p>The integration is intentionally reflection-based so ODailyQuests can build even when
 * ExcellentCrates does not publish its API artifact to a public Maven repository.</p>
 */
public class CrateOpenQuest extends AbstractQuest {

    private static final String REQUIRED = "required";
    private static final String CRATE_OPEN_EVENT = "su.nightexpress.excellentcrates.api.event.CrateOpenEvent";
    private final Set<String> expectedCrate = new HashSet<>();

    public CrateOpenQuest(BasicQuest base) {
        super(base);
    }

    @Override
    public String getType() {
        return "CRATE_OPEN";
    }

    @Override
    public boolean canProgress(@Nullable Event provided, Progression progression) {
        if (provided == null || !CRATE_OPEN_EVENT.equals(provided.getClass().getName())) {
            return false;
        }

        final String crate = getCrateName(provided);
        if (crate == null || crate.isBlank()) {
            Debugger.write("CrateOpenQuest: could not resolve crate name from ExcellentCrates event.");
            return false;
        }

        Debugger.write("CrateOpenQuest: canProgress checking crate " + crate);
        return expectedCrate.isEmpty() || expectedCrate.contains(crate.toLowerCase(Locale.ROOT));
    }

    @Nullable
    private String getCrateName(Event event) {
        try {
            final Method getCrateMethod = event.getClass().getMethod("getCrate");
            final Object crate = getCrateMethod.invoke(event);
            if (crate == null) {
                return null;
            }

            final Method getNameMethod = crate.getClass().getMethod("getName");
            final Object name = getNameMethod.invoke(crate);
            return name == null ? null : name.toString();
        } catch (ReflectiveOperationException exception) {
            Debugger.write("CrateOpenQuest: failed to read crate name from ExcellentCrates event: " + exception.getMessage());
            return null;
        }
    }

    @Override
    public boolean loadParameters(ConfigurationSection section, String file, String index) {
        expectedCrate.clear();

        if (!PluginUtils.isPluginEnabled("ExcellentCrates")) {
            PluginLogger.configurationError(file, index, null, "You must have ExcellentCrates installed to use this quest.");
            return false;
        }

        if (section.isList(REQUIRED)) {
            for (String crate : section.getStringList(REQUIRED)) {
                if (crate != null && !crate.isBlank()) {
                    expectedCrate.add(crate.toLowerCase(Locale.ROOT));
                }
            }
        } else if (section.isString(REQUIRED)) {
            final String crate = section.getString(REQUIRED);
            if (crate != null && !crate.isBlank()) {
                expectedCrate.add(crate.toLowerCase(Locale.ROOT));
            }
        }

        return true;
    }
}
