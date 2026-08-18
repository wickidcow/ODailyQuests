package com.ordwen.odailyquests.tools.updater.config.updates;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.tools.updater.config.ConfigUpdater;

/**
 * Applies maintained 3.0.5 compatibility defaults before marking the configuration upgraded.
 */
public class Update302to305 extends ConfigUpdater {

    public Update302to305(ODailyQuests plugin) {
        super(plugin);
    }

    @Override
    public void apply(ODailyQuests plugin, String version) {
        // The maintained player menu exposes one daily reroll action. Choosing "reroll all"
        // still consumes only this single daily action.
        setDefaultConfigItem("reroll_maximum", 1, config, configFile, true);
        updateVersion(version);
    }
}
