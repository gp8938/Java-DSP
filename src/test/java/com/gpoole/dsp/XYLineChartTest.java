package com.gpoole.dsp;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationTest;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(HeadlessCondition.class)
class XYLineChartTest extends ApplicationTest {

    private XYLineChart chart;

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
