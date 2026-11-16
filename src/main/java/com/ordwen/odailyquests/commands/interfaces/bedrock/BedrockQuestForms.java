package com.ordwen.odailyquests.commands.interfaces.bedrock;

import com.ordwen.odailyquests.ODailyQuests;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * API-free bridge: safe to load even on servers that do not have Floodgate or
 * Cumulus installed. Only resolves FloodgateQuestForms when Floodgate is present.
 */
public final class BedrockQuestForms {
    private BedrockQuestForms() {}

    public static boolean openIfAvailable(Player player) {
        if (!enabled()) return false;
        try {
            return FloodgateQuestForms.openIfAvailable(player);
        } catch (LinkageError exception) {
            ODailyQuests.INSTANCE.getLogger().warning(
                    "Bedrock forms could not load the Floodgate/Cumulus API: " + exception.getMessage());
            return false;
        }
    }

    public static boolean openRerollIfAvailable(Player player) {
        if (!enabled()) return false;
        try {
            return FloodgateQuestForms.openRerollIfAvailable(player);
        } catch (LinkageError exception) {
            ODailyQuests.INSTANCE.getLogger().warning(
                    "Bedrock reroll forms could not load the Floodgate/Cumulus API: " + exception.getMessage());
            return false;
        }
    }

    private static boolean enabled() {
        final ODailyQuests plugin = ODailyQuests.INSTANCE;
        return plugin != null && plugin.isEnabled()
                && plugin.getFilesManager() != null
                && plugin.getFilesManager().getConfigurationFile() != null
                && plugin.getFilesManager().getConfigurationFile().getConfig()
                        .getBoolean("bedrock_forms.enabled", true)
                && Bukkit.getPluginManager().isPluginEnabled("floodgate");
    }
}
