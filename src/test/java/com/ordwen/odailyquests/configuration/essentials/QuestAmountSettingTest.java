package com.ordwen.odailyquests.configuration.essentials;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuestAmountSettingTest {

    @Test
    void zeroIsAcceptedAsDisabledStaticCategory() {
        final QuestAmountSetting setting = QuestAmountSetting.from("hard", 0);

        assertFalse(setting.isDynamic());
        assertEquals(0, setting.getStaticAmount());
    }

    @Test
    void positiveStaticAmountsRemainSupported() {
        final QuestAmountSetting setting = QuestAmountSetting.from("easy", 2);

        assertFalse(setting.isDynamic());
        assertEquals(2, setting.getStaticAmount());
    }

    @Test
    void negativeStaticAmountsRemainInvalid() {
        assertThrows(IllegalArgumentException.class, () -> QuestAmountSetting.from("evil", -1));
    }
}
