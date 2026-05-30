package com.gpoole.dsp;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationTest;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(HeadlessCondition.class)
class GUITest extends ApplicationTest {

    private GUIFX app;

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
