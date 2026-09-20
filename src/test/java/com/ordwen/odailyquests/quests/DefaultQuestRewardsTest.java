package com.ordwen.odailyquests.quests;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultQuestRewardsTest {

    private static final List<String> FABLE_RESOURCES = List.of(
            "quests/good.yml",
            "quests/evil.yml"
    );

    private static final List<String> SERVER_SPECIFIC_REWARD_TOKENS = List.of(
            "val exp ",
            "eco give ",
            "em money add ",
            "questadmin givepoints ",
            "crates key give ",
            "scoreboard players add ",
            "lp user "
    );

    @Test
    void bundledFableRewardsStayServerNeutral() throws IOException {
        for (String resource : FABLE_RESOURCES) {
            final String content = readResource(resource);

            assertTrue(content.contains("xp give %player_name%"),
                    resource + " should retain a vanilla XP reward");

            final String lowered = content.toLowerCase();
            for (String token : SERVER_SPECIFIC_REWARD_TOKENS) {
                assertFalse(lowered.contains(token),
                        resource + " must not ship a server-specific reward command: " + token);
            }
        }
    }

    private String readResource(String path) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, "Missing test resource: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
