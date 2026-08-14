package com.gpoole.dsp;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationTest;
import static org.junit.jupiter.api.Assertions.*;

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
}
