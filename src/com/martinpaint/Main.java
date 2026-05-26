package com.martinpaint;

import com.martinpaint.app.AppController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    static {
        // Set the macOS menu-bar application name before the AWT/FX toolkit starts.
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Paint App");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Paint App");
    }

    public void start(Stage stage) {
        AppController controller = new AppController(stage);
        controller.launch();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
