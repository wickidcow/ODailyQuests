package com.ordwen.odailyquests.tools.updater.config.updates;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.tools.updater.config.ConfigUpdater;

/**
 * Marks existing 3.0.5 configurations as upgraded to the 3.0.6 compatibility release.
 *
 * <p>No administrator settings need to be rewritten for this update. In particular,
 * existing ItemsAdder, Nexo, Oraxen, and MMOItems integration settings are preserved.</p>
 */
public class Update305to306 extends ConfigUpdater {

    public Update305to306(ODailyQuests plugin) {
        super(plugin);
    }

    @Override
    public void apply(ODailyQuests plugin, String version) {
        updateVersion(version);
    }
}
