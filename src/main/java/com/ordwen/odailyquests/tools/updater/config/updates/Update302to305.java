package com.ordwen.odailyquests.tools.updater.config.updates;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.tools.updater.config.ConfigUpdater;

/**
 * Marks pre-3.0.5 configurations as upgraded after the maintained compatibility migrations
 * performed by FilesManager have populated the seven-category and dependency-pack settings.
 */
public class Update302to305 extends ConfigUpdater {

    public Update302to305(ODailyQuests plugin) {
        super(plugin);
    }

    @Override
    public void apply(ODailyQuests plugin, String version) {
        updateVersion(version);
    }
}
