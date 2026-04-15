package com.aquip.tetris.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigStateTest {

    @Test
    void gravityThresholdAcceleratesAndStopsAtConfiguredMinimum() {
        ConfigState config = new ConfigState();

        assertEquals(60, config.gravityThresholdForPieces(0));
        assertTrue(config.gravityThresholdForPieces(10) < config.gravityThresholdForPieces(0));
        assertTrue(config.gravityThresholdForPieces(50) < config.gravityThresholdForPieces(10));
        assertEquals(config.minimumGravityTick, config.gravityThresholdForPieces(2_000));
    }

    @Test
    void softDropUsesFasterThreshold() {
        ConfigState config = new ConfigState();

        assertTrue(config.softDropThresholdForPieces(150) < config.gravityThresholdForPieces(150));
        assertEquals(1, config.softDropThresholdForPieces(500));
    }
}
