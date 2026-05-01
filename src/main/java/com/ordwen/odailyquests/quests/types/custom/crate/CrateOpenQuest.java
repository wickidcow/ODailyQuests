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
import su.nightexpress.excellentcrates.api.event.CrateOpenEvent;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * {@code CrateOpenQuest} represents a quest where the player is required
 * to open one or more crates from the <b>ExcellentCrates</b> plugin.
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Listens for {@link CrateOpenEvent} fired by ExcellentCrates.</li>
 *     <li>Supports restricting progression to specific crates via configuration.</li>
 *     <li>If no crates are specified, <i>any</i> crate counts toward the quest.</li>
 *     <li>Validates that ExcellentCrates is installed before accepting configuration.</li>
 * </ul>
 *
 * <p>Dependencies:</p>
 * <ul>
 *     <li>{@link AbstractQuest} for base quest logic.</li>
 *     <li>{@link PluginUtils} to check plugin availability.</li>
 *     <li>Uses {@link PluginLogger} to report configuration errors.</li>
 *     <li>Uses {@link Debugger} for detailed debug output during item matching.</li>
 *     <li>ExcellentCrates plugin for crate opening events.</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * Example YAML configuration for this quest type:
 * <pre>{@code
 * type: CRATE_OPEN
 * required:
 *   - vote_crate
 *   - rare_crate
 * }</pre>
 *
 * <p>or for a single crate:</p>
 * <pre>{@code
 * type: CRATE_OPEN
 * required: vote_crate
 * }</pre>
 *
 * If {@code required} is omitted, any crate will be accepted.
 */
public class CrateOpenQuest extends AbstractQuest {

    /**
     * Configuration key for required crates.
     */
    private static final String REQUIRED = "required";

    /**
     * Set of expected crate names (lowercased for case-insensitive matching).
     */
    private final Set<String> expectedCrate = new HashSet<>();

    /**
     * Constructs a {@code CrateOpenQuest} with the given base quest definition.
     *
     * @param base the parent/basic quest definition
     */
    public CrateOpenQuest(BasicQuest base) {
        super(base);
    }

    /**
     * Returns the quest type identifier for this quest.
     *
     * @return the string {@code "CRATE_OPEN"}
     */
    @Override
    public String getType() {
        return "CRATE_OPEN";
    }

    /**
     * Determines whether the provided event allows this quest to progress.
     *
     * <p>Behavior:</p>
     * <ul>
     *     <li>If the event is a {@link CrateOpenEvent}, the crate name is extracted.</li>
     *     <li>If no crates are configured, any crate counts toward progress.</li>
     *     <li>If crates are configured, only those listed will count.</li>
     * </ul>
     *
     * @param provided    the event to check (expected: {@link CrateOpenEvent})
     * @param progression current player progression (not used here, but part of the contract)
     * @return {@code true} if the event matches quest requirements; otherwise {@code false}
     */
    @Override
    public boolean canProgress(@Nullable Event provided, Progression progression) {
        if (provided instanceof CrateOpenEvent event) {
            final String crate = getCrateName(event);

            if (crate == null || crate.isBlank()) {
                Debugger.write("CrateOpenQuest: could not resolve crate name from ExcellentCrates event.");
                return false;
            }

            Debugger.write("CrateOpenQuest: canProgress checking crate " + crate);
            return expectedCrate.isEmpty() || expectedCrate.contains(crate.toLowerCase(Locale.ROOT));
        }

        return false;
    }

    /**
     * Reads the ExcellentCrates crate name through reflection.
     *
     * <p>This avoids directly compiling against ExcellentCrates' crate implementation,
     * which may extend NightCore classes that are not present on the compile classpath.</p>
     *
     * @param event ExcellentCrates crate open event
     * @return crate name, or {@code null} if it could not be resolved
     */
    @Nullable
    private String getCrateName(CrateOpenEvent event) {
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

    /**
     * Loads quest parameters from the configuration.
     *
     * <p>Recognized key:</p>
     * <ul>
     *     <li>{@code required}: either a single string or a list of crate names (case-insensitive).</li>
     * </ul>
     *
     * @param section configuration section for this quest
     * @param file    file name (for error reporting)
     * @param index   quest index/key (for error reporting)
     * @return {@code true} if configuration was valid and ExcellentCrates is installed,
     * {@code false} otherwise
     */
    @Override
    public boolean loadParameters(ConfigurationSection section, String file, String index) {
        expectedCrate.clear();

        // Ensure dependency is available
        if (!PluginUtils.isPluginEnabled("ExcellentCrates")) {
            PluginLogger.configurationError(file, index, null, "You must have ExcellentCrates installed to use this quest.");
            return false;
        }

        // Accept list or single string
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
