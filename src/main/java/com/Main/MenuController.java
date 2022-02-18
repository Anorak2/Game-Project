package com.Main;

import com.Main.Games.MinesweeperController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.Objects;

public class MenuController {
    private Stage stage;
    public Scene scene;
    private Parent root;

    public void swapToMenu(ActionEvent event) throws IOException{
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/menu.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void swapToBlackjack(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/blackjack.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void swapToCheckers(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/checkers.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void swapToMineSweeper(ActionEvent event) throws IOException {
        //Really cool way to have a menu on Mac, should pursue at a later date

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxml/minesweeper.fxml")));
        root = loader.load();

        MinesweeperController controller = loader.getController();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);

        //It's a bit of a mess because it detects if keys are pressed (that kinda rhymes)
        scene.setOnKeyPressed(keyEvent -> {
            if((keyEvent.getCode()+"").equals("Q") || (keyEvent.getCode()+"").equals("P"))
                 controller.setKeyHeld(true);
        });
        scene.setOnKeyReleased(keyEvent -> {
            if((keyEvent.getCode()+"").equals("Q") || (keyEvent.getCode()+"").equals("P"))
                controller.setKeyHeld(false);
        });

        stage.setScene(scene);
        stage.show();
    }
}