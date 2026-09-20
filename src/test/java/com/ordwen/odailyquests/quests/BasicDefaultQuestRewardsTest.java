package com.ordwen.odailyquests.quests;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicDefaultQuestRewardsTest {

    private record Expected(int gold, int xp) {}

    @Test
    void everyBundledBasicQuestUsesTheExpectedRewardBundle() throws IOException {
        final Map<String, Expected> expected = Map.of(
                "quests/easy.yml", new Expected(1000, 550),
                "quests/medium.yml", new Expected(2000, 2920),
                "quests/hard.yml", new Expected(4000, 8670)
        );

        for (Map.Entry<String, Expected> entry : expected.entrySet()) {
            final String content = readResource(entry.getKey());
            final Expected reward = entry.getValue();

            assertEquals(12, count(content, "reward_type: COMMAND"),
                    entry.getKey() + " should keep 12 bundled quests");
            assertEquals(12, count(content, "eco give %player% " + reward.gold()),
                    entry.getKey() + " should give the expected gold on every quest");
            assertEquals(12, count(content, "xp give %player% " + reward.xp()),
                    entry.getKey() + " should give the expected XP on every quest");
            assertEquals(12, count(content, "questadmin givepoints %player% 1"),
                    entry.getKey() + " should give exactly 1 quest point on every quest");
            assertTrue(content.contains("&71 Quest Point"),
                    entry.getKey() + " should advertise the 1 Quest Point reward");
        }
    }

    private int count(String content, String needle) {
        int count = 0;
        int from = 0;
        while ((from = content.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }

    private String readResource(String path) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, "Missing test resource: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
