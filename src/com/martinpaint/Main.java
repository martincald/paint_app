package com.martinpaint;

import com.martinpaint.app.AppController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage stage) {
        AppController controller = new AppController(stage);
        controller.launch();
    }
}
