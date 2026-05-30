package com.gpoole.dsp;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testfx.framework.junit5.ApplicationTest;
import static org.junit.jupiter.api.Assertions.*;

@DisabledIf("isHeadless")
class XYLineChartTest extends ApplicationTest {

    private XYLineChart chart;

    private static boolean isHeadless() {
        return Boolean.getBoolean("java.awt.headless") || "true".equals(System.getenv("CI"));
    }

    @Override
    public void start(Stage stage) {
        chart = new XYLineChart("Test");
        stage.setScene(new javafx.scene.Scene(new javafx.scene.layout.VBox(chart.getNode())));
        stage.show();
    }

    @Test
    void chartRenders() {
        assertNotNull(chart);
    }

    @Test
    void setDataDoesNotThrow() {
        double[] data = new double[256];
        chart.setData(data, 48000);
    }

    @Test
    void multipleSetDataCallsDontThrow() {
        double[] data = new double[128];
        for (int i = 0; i < 10; i++) {
            chart.setData(data, 44100);
        }
    }
}
