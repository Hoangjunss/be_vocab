package com.toiec.demo.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XpPolicyTest {

    @Test
    @DisplayName("getXpForQuality - returns correct XP for quality values")
    void getXpForQuality_ShouldReturnCorrectXp() {
        assertThat(XpPolicy.getXpForQuality(4)).isEqualTo(15);
        assertThat(XpPolicy.getXpForQuality(3)).isEqualTo(10);
        assertThat(XpPolicy.getXpForQuality(2)).isEqualTo(5);
        assertThat(XpPolicy.getXpForQuality(1)).isEqualTo(0);
        assertThat(XpPolicy.getXpForQuality(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("getXpForQuality - handles out of range values safely")
    void getXpForQuality_OutOfRange() {
        assertThat(XpPolicy.getXpForQuality(100)).isEqualTo(0);
        assertThat(XpPolicy.getXpForQuality(-5)).isEqualTo(0);
    }

    @Test
    @DisplayName("getXpForQuality - validation for quality 1 (forget)")
    void getXpForQuality_Quality1() {
        assertThat(XpPolicy.getXpForQuality(1)).isEqualTo(0);
    }

    @Test
    @DisplayName("getXpForQuality - validation for quality 0 (blackout)")
    void getXpForQuality_Quality0() {
        assertThat(XpPolicy.getXpForQuality(0)).isEqualTo(0);
    }
}
