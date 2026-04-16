package com.vcs;

import com.vcs.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        primaryStage.setTitle("Система за управление на версии");
        primaryStage.setScene(new Scene(loader.load(), 400, 300));
        primaryStage.show();
    }

    @Override
    public void stop() {
        HibernateUtil.getSessionFactory().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}