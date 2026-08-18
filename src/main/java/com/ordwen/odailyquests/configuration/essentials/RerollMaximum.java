package com.ordwen.odailyquests.configuration.essentials;

import com.ordwen.odailyquests.configuration.ConfigFactory;
import com.ordwen.odailyquests.configuration.IConfigurable;
import com.ordwen.odailyquests.files.implementations.ConfigurationFile;

/** Maintained daily reroll limit. */
public class RerollMaximum implements IConfigurable {

    private final ConfigurationFile configurationFile;
    private int rerollMaximumConf;

    public RerollMaximum(ConfigurationFile configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Override
    public void load() {
        // 3.0.5 intentionally gives normal players one reroll action per daily quest set.
        // The bypass permission remains available for administrators. Older configs often used
        // -1 (unlimited), so clamp the runtime value as well as migrating the stored default.
        rerollMaximumConf = 1;
    }

    private static RerollMaximum getInstance() {
        return ConfigFactory.getConfig(RerollMaximum.class);
    }

    public static int getMaxRerolls() {
        return getInstance().rerollMaximumConf;
    }
}
