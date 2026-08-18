package com.ordwen.odailyquests.files;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.files.implementations.*;
import com.ordwen.odailyquests.quests.features.DefaultQuestPacks;
import com.ordwen.odailyquests.quests.features.MaintainedQuestTypes;

public class FilesManager {

    private final ODailyQuests plugin;

    private final ConfigurationFile configurationFile;
    private final PlayerInterfaceFile playerInterfaceFile;
    private final TotalRewardsFile totalRewardsFile;
    private final ProgressionFile progressionFile;

    public FilesManager(ODailyQuests plugin) {
        this.plugin = plugin;

        this.configurationFile = new ConfigurationFile(plugin);
        this.playerInterfaceFile = new PlayerInterfaceFile(plugin);
        this.totalRewardsFile = new TotalRewardsFile(plugin);
        this.progressionFile = new ProgressionFile(plugin);
    }

    public void load() {
        MaintainedQuestTypes.register(plugin.getAPI().getQuestTypeRegistry());

        configurationFile.load();

        if (DefaultQuestPacks.ensureConfig(configurationFile.getConfig(), configurationFile.getFile())) {
            configurationFile.load();
        }

        playerInterfaceFile.load();
        totalRewardsFile.load();
        progressionFile.load();

        new MessagesFile(plugin).load();
        new QuestsFiles(plugin).load();
    }

    public ConfigurationFile getConfigurationFile() {
        return configurationFile;
    }

    public PlayerInterfaceFile getPlayerInterfaceFile() {
        return playerInterfaceFile;
    }

    public TotalRewardsFile getTotalRewardsFile() {
        return totalRewardsFile;
    }

    public ProgressionFile getProgressionFile() {
        return progressionFile;
    }
}
