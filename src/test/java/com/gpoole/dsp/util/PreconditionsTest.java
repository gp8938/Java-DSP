package com.gpoole.dsp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Preconditions} validation utility.
 */
class PreconditionsTest {

    @Test
    void checkArgumentPasses() {
        assertDoesNotThrow(() -> Preconditions.checkArgument(true, "ok"));
    }

    @Test
    void checkArgumentFails() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkArgument(false, "bad value"));
        assertEquals("bad value", ex.getMessage());
    }

    @Test
    void checkNotNullPasses() {
        assertDoesNotThrow(() -> Preconditions.checkNotNull("hello", "param"));
    }

    @Test
    void checkNotNullFails() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> Preconditions.checkNotNull(null, "param"));
        assertTrue(ex.getMessage().contains("param"));
    }

    @Test
    void checkArrayLengthPasses() {
        assertDoesNotThrow(() -> Preconditions.checkArrayLength(new double[]{1, 2, 3}, 2, "arr"));
    }

    @Test
    void checkArrayLengthTooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkArrayLength(new double[]{1}, 5, "arr"));
    }

    @Test
    void checkArrayLengthNullArray() {
        assertThrows(NullPointerException.class,
                () -> Preconditions.checkArrayLength(null, 1, "arr"));
    }

    @Test
    void checkPowerOfTwoPasses() {
        assertDoesNotThrow(() -> Preconditions.checkPowerOfTwo(1));
        assertDoesNotThrow(() -> Preconditions.checkPowerOfTwo(2));
        assertDoesNotThrow(() -> Preconditions.checkPowerOfTwo(1024));
    }

    @Test
    void checkPowerOfTwoFails() {
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkPowerOfTwo(0));
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkPowerOfTwo(3));
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkPowerOfTwo(-4));
    }

    @Test
    void checkSampleRatePasses() {
        assertDoesNotThrow(() -> Preconditions.checkSampleRate(44100));
        assertDoesNotThrow(() -> Preconditions.checkSampleRate(384000));
    }

    @Test
    void checkSampleRateFails() {
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkSampleRate(0));
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkSampleRate(-1));
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkSampleRate(500_000));
    }
}
