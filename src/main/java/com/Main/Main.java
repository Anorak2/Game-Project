package com.Main;

import com.Main.Games.CheckersController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage stage){
        try {
            URL fxmlLocation = getClass().getResource("/fxml/menu.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load(), 150, 400);
            stage.setResizable(false);
            //stage.setTitle("Adam's Games");
            stage.setScene(scene);
            stage.show();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

    public static void testCheckers(){
        CheckersController c = new CheckersController();
        double numTests = 8;
        long total = 0, startTime;

        for(int x = 0; x < numTests; x++) {
            try {
                synchronized (c) {
                    c.moveOnBoard(5, 4, 4, 3);
                    startTime = System.nanoTime();
                    c.crappyAi();
                    total += (System.nanoTime() - startTime) / 1000000;
                }
                System.out.println("x");
                synchronized (c) {
                    c.moveOnBoard(5, 6, 4, 5);
                    startTime = System.nanoTime();
                    c.crappyAi();
                    total += (System.nanoTime() - startTime) / 1000000;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        System.out.println("Total: " + (total/1000000)/numTests);
    }
}