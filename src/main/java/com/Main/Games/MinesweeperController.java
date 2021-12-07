package com.Main.Games;

import com.Main.MenuController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MinesweeperController extends MenuController implements Initializable {
    @FXML
    Rectangle mainBox;
    @FXML
    GridPane MainGridpane;

    private final int GridSize = 24;
    private final int pixelSize = 600;
    private int numBombs = (int) ((GridSize * GridSize) /4.85);

    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainBox.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                click(true, event.getSceneX(), event.getSceneY());
                System.out.println("left clicked");
            }
            else if (event.getButton().equals(MouseButton.SECONDARY)) {
                click(false, event.getSceneX(), event.getSceneY());
                System.out.println("right clicked");
            }
        });

        MainGridpane.setGridLinesVisible(true);
        setBoard();
    }

    public void click(Boolean isLeftClick, double x, double y){
        int xGrid = (int) x/GridSize;
        int yGrid = (int) y/GridSize;

        if(isLeftClick){
            //logic
            Node temp = getNodeByRowColumnIndex(xGrid, yGrid);
            if(temp != null)
                System.out.println(temp.accessibleRoleProperty());
        }
        else {
            Node temp = getNodeByRowColumnIndex(xGrid, yGrid);
        }
    }

    public void showAllAround(){

    }
    public Node getNodeByRowColumnIndex (final int row, final int column) {
        Node result = null;
        ObservableList<Node> childrens = MainGridpane.getChildren();

        for (Node node : childrens) {
            if(GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                    result = node;
                    break;
                }
            }
        }

        return result;
    }

    public void setBoard(){
        int num = 0;
        try {
            for(int row = 0; row < GridSize; row++) {
                for(int col = 0; col < GridSize; col++) {
                    FileInputStream inputstream = new FileInputStream("/Users/3064683/Desktop/mine-icon.png");
                    Image image = new Image(inputstream);

                    ImageView tempImage = new ImageView();
                    tempImage.setImage(image);
                    tempImage.setFitHeight(pixelSize/GridSize);
                    tempImage.setFitWidth(pixelSize/GridSize);

                    if(num < numBombs && Math.random() < .206){
                        MainGridpane.add(tempImage, row, col);
                    }
                    //GridPane.setConstraints((Node) image, 2, 0);
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
