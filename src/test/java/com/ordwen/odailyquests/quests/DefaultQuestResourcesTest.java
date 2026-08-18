package com.ordwen.odailyquests.quests;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultQuestResourcesTest {

    private static final Pattern QUEST_ENTRY = Pattern.compile("(?m)^  \\d+:$");

    @Test
    void sevenMaintainedCategoryResourcesAreBundled() throws IOException {
        for (String category : new String[]{"easy", "medium", "hard", "good", "evil", "tech", "wildcard"}) {
            String yaml = resource("quests/" + category + ".yml");
            assertTrue(yaml.contains("quests:"), category + ".yml must contain a quests section");
        }
    }

    @Test
    void techPoolIsPhysicallyPopulatedAndDependencyTagged() throws IOException {
        String yaml = resource("quests/tech.yml");
        assertFalse(yaml.contains("quests: {}"), "Tech must never ship as an empty runtime-only shell");
        assertEquals(27, countQuests(yaml), "Unexpected base built-in Tech quest count");
        assertTrue(yaml.contains("quest_type: SLIMEFUN_CRAFT"));
        assertTrue(yaml.contains("quest_type: SLIMEFUN_ITEM"));
        assertTrue(yaml.contains("quest_type: REBAR_ITEM"));
        assertTrue(yaml.contains("default_pack: slimefun-core"));
        assertTrue(yaml.contains("default_pack: pylon-rebar"));
    }

    @Test
    void exactPylonDefaultsArePhysicallyBundled() throws IOException {
        String yaml = resource("quests/pylon-defaults.yml");
        assertEquals(4, occurrences(yaml, "    quest_type: REBAR_ITEM"), "Unexpected exact Pylon default count");
        assertEquals(4, occurrences(yaml, "    default_pack: pylon-rebar"), "Every Pylon default must be dependency tagged");
        assertTrue(yaml.contains("pylon:shimmer_magnet"));
        assertTrue(yaml.contains("pylon:diamond_hammer"));
        assertTrue(yaml.contains("pylon:elevator_1"));
        assertTrue(yaml.contains("pylon:reactivated_wither_skull"));
        assertTrue(yaml.contains("Craft a Shimmer Magnet"));
        assertTrue(yaml.contains("Craft a Diamond Hammer"));
        assertTrue(yaml.contains("Craft an Elevator I"));
        assertTrue(yaml.contains("Craft a Reactivated Wither Skull"));
    }

    @Test
    void wildCardPoolIsPhysicallyPopulatedAndProviderAware() throws IOException {
        String yaml = resource("quests/wildcard.yml");
        assertFalse(yaml.contains("quests: {}"), "Wild Card must never ship as an empty runtime-only shell");
        assertEquals(18, countQuests(yaml), "Unexpected built-in Wild Card quest count");
        assertTrue(yaml.contains("default_pack: valhallammo"));
        assertTrue(yaml.contains("default_pack: evenmorefish"));
        assertTrue(yaml.contains("default_pack: pyrofishingpro"));
        assertTrue(yaml.contains("default_pack: mcmmo"));
        assertTrue(yaml.contains("default_pack: mmoitems"));
        assertTrue(yaml.contains("default_pack: itemsadder"));

        assertTrue(yaml.contains("valhalla reward power_spendableskillpoints_add"));
        assertTrue(yaml.contains("emf admin bait Shrimp"));
        assertTrue(yaml.contains("fish addentropy"));
        assertTrue(yaml.contains("fish addxp"));
        assertTrue(yaml.contains("addxp %player_name% mining"));
    }

    @Test
    void maintainedDefaultConfigListsSevenDailyCategories() throws IOException {
        String config = resource("config.yml");
        for (String category : new String[]{"easy", "medium", "hard", "good", "evil", "tech", "wildcard"}) {
            assertTrue(config.contains("  " + category + ": 1"), "Missing default daily category: " + category);
        }
    }

    @Test
    void maintainedPlayerInterfaceHasSevenCategorySlotsAndDynamicTotals() throws IOException {
        String yaml = resource("playerInterface.yml");
        for (String category : new String[]{"easy", "medium", "hard", "good", "evil", "tech", "wildcard"}) {
            assertTrue(yaml.contains("      " + category + ":"), "Missing player-interface category: " + category);
        }
        assertTrue(yaml.contains("%totalQuests%"), "Player interface must not hardcode the current quest total");
        assertFalse(yaml.contains("/&b9"), "Player interface must not assume the old nine-quest layout");
        assertFalse(yaml.contains("%progressPercent%%%"), "Progress percentage must render with only one literal percent sign");
    }

    private static int countQuests(String yaml) {
        Matcher matcher = QUEST_ENTRY.matcher(yaml);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    private static String resource(String path) throws IOException {
        try (InputStream input = DefaultQuestResourcesTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing bundled resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
