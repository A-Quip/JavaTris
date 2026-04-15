package com.aquip.tetris.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigStateTest {

    @Test
    void gravityThresholdAcceleratesAndStopsAtConfiguredMinimum() {
        ConfigState config = new ConfigState();

        assertEquals(60, config.gravityThresholdForPieces(0));
        assertEquals(59, config.gravityThresholdForPieces(10));
        assertEquals(55, config.gravityThresholdForPieces(50));
        assertEquals(40, config.gravityThresholdForPieces(200));
    }

    @Test
    void softDropUsesFasterThreshold() {
        ConfigState config = new ConfigState();

        assertEquals((60 - 15)/8, config.softDropThresholdForPieces(150));
        assertEquals((60 - 50)/8, config.softDropThresholdForPieces(500));
    }
}
