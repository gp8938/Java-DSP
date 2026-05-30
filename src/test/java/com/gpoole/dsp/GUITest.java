package com.gpoole.dsp;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testfx.framework.junit5.ApplicationTest;
import static org.junit.jupiter.api.Assertions.*;

@DisabledIf("isHeadless")
class GUITest extends ApplicationTest {

    private GUIFX app;

    private static boolean isHeadless() {
        return Boolean.getBoolean("java.awt.headless") || "true".equals(System.getenv("CI"));
    }

    @Override
    public void start(Stage stage) throws Exception {
        app = new GUIFX();
        app.start(stage);
    }

    @Test
    void applicationStarts() {
        assertNotNull(app);
    }
}
