package com.gpoole.dsp;

import javafx.application.Platform;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class XYLineChartTest {

    @Test
    void chartRenders() throws Exception {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException toolkitAlreadyRunning) {
            // toolkit shared with other tests in this JVM — fine
        }

        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                XYLineChart chart = new XYLineChart("Test");
                assertNotNull(chart.getNode());
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(10, TimeUnit.SECONDS), "JavaFX thread did not run");
        Throwable t = failure.get();
        if (t instanceof RuntimeException re) {
            throw re;
        }
        if (t instanceof Error err) {
            throw err;
        }
        assertNull(t);
    }
}
