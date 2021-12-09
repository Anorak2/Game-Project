package com.Main.Games;

import com.Main.MenuController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.util.ArrayList;


public class BlackjackController extends MenuController {
    private static class Card implements Comparable<Card>{
        String suite;
        int num;

        public Card(String suite, int num) {
            this.suite = suite;
            this.num = num;
        }
        public int compareTo(Card other){
            if(other.getSuite().equals(suite) && other.getNum() == num)
                return 0;
            else if(num > other.getNum())
                return -1;
            return 1;
        }

        public String getSuite() {
            return suite;
        }
        public int getNum() {
            return num;
        }
    }


    @FXML
    Button DrawButton;
    @FXML
    Button StandButton;
    @FXML
    Text WinnerText;
    @FXML
    Rectangle PlayerCard1, PlayerCard2, PlayerCard3, PlayerCard4, PlayerCard5, PlayerCard6, PlayerCard7;
    @FXML
    Text PlayerNum1, PlayerNum2, PlayerNum3, PlayerNum4, PlayerNum5, PlayerNum6, PlayerNum7;
    @FXML
    Rectangle DealerCard1, DealerCard2, DealerCard3, DealerCard4, DealerCard5, DealerCard6, DealerCard7;
    @FXML
    Text DealerText1, DealerText2, DealerText3, DealerText4, DealerText5, DealerText6, DealerText7;
    @FXML
    Text PlayerBottomNum, DealerBottomNum;
    @FXML
    AnchorPane AnchorPane;

    ArrayList<Card> deck = new ArrayList<>();
    ArrayList<Card> player = new ArrayList<>();
    ArrayList<Card> dealer = new ArrayList<>();
    Boolean Stand = false;


    public void initialize() {
        //create deck
        deck.clear();
        player.clear();
        dealer.clear();
        hideAllCards();
        DrawButton.setVisible(true);
        StandButton.setVisible(true);
        Stand = false;
        PlayerBottomNum.setX(0);
        DealerBottomNum.setX(0);
        for (int x = 2; x < 15; x++)
            deck.add(new Card("Spade", x));
        for (int x = 2; x < 15; x++)
            deck.add(new Card("Club", x));
        for (int x = 2; x < 15; x++)
            deck.add(new Card("Heart", x));
        for (int x = 2; x < 15; x++)
            deck.add(new Card("Diamond", x));
        DrawDealer();
        WinnerText.setText("");
        DrawPlayer();
        DrawPlayer();
    }

    public void DrawPlayer(){
        if(!Stand){
            Card temp = drawRandomCard();
            player.add(temp);
            displayPlayerCard();
        }
        if(getTotal(player) > 21)
            winner("Dealer");
        else if (getTotal(player) == 21)
            winner("Player");
    }
    public void StandPlayer() {
        Stand = true;
        DrawButton.setVisible(false);
        StandButton.setVisible(false);
        while(true) {
            DrawDealer();
            if (getTotal(dealer) > 21 || getTotal(dealer) == getTotal(player) || getTotal(dealer) > getTotal(player))
                break;
        }
        if(getTotal(dealer) > 21)
            winner("Player");
        else if(getTotal(dealer) > getTotal(player))
            winner("Dealer");
        else if(getTotal(dealer) == getTotal(player)){
            if(getTotal(dealer) > 16)
                winner("Tie");
            else{
                while (true){
                    DrawDealer();
                    if(getTotal(dealer) > 21){
                        winner("Player");
                        break;
                    }
                    else if(getTotal(dealer) > getTotal(player)){
                        winner("Dealer");
                        break;
                    }
                }
            }
        }
        else
            winner("Player");
    }
    private void displayPlayerCard(){
        Rectangle[] PlayerCard = {PlayerCard1, PlayerCard2, PlayerCard3, PlayerCard4, PlayerCard5, PlayerCard6, PlayerCard7};
        Text[] PlayerNum = {PlayerNum1, PlayerNum2,  PlayerNum3,  PlayerNum4,  PlayerNum5,  PlayerNum6,  PlayerNum7};
        Card temp;
        for(int x = 0; x < player.size(); x++){
            temp = player.get(x);
            PlayerCard[x].setVisible(true);
            PlayerNum[x].setVisible(true);
            PlayerNum[x].setText(returnNum(temp));
            PlayerBottomNum.setText(returnNum(temp));

            colorTheNums(x, true);
        }
        PlayerBottomNum.setVisible(true);
        PlayerBottomNum.setX(25 * (player.size()-1));
    }
    private ImageView PlayerBot;
    private ImageView DealerBot;
    private void colorTheNums(int x, Boolean isPlayer){
        try {
            if (isPlayer) {
                FileInputStream inputstream;

                if (player.get(x).getSuite().equals("Diamond"))
                    inputstream = new FileInputStream("src/main/resources/fxml/images/Diamond.png");
                else if ( player.get(x).getSuite().equals("Heart"))
                    inputstream = new FileInputStream("src/main/resources/fxml/images/Heart.png");
                else if (player.get(x).getSuite().equals("Club"))
                    inputstream = new FileInputStream("src/main/resources/fxml/images/Club.png");
                else
                    inputstream = new FileInputStream("src/main/resources/fxml/images/Spade.png");


                Image image = new Image(inputstream);
                ImageView tempImage = new ImageView();
                tempImage.setImage(image);
                tempImage.setFitHeight(20);
                tempImage.setFitWidth(20);
                tempImage.setX(242 + (25 * (player.size()-1)));
                tempImage.setY(215);
                PlayerBot = tempImage;

                ObservableList<Node> childrens = AnchorPane.getChildren();
                for (Node node : childrens) {
                    if (node instanceof ImageView && !node.equals(DealerBot))
                        node.setVisible(false);
                }
                AnchorPane.getChildren().add(tempImage);

            } else {
                FileInputStream inputstream;

                if (dealer.get(x).getSuite().equals("Diamond"))
                    inputstream = new FileInputStream("src/main/resources/fxml/images/Diamond.png");
                else if (dealer.get(x).getSuite().equals("Heart"))
                    inputstream = new FileInputStream("src/main/resources/fxml/images/Heart.png");
                else if (dealer.get(x).getSuite().equals("Club"))
                    inputstream = new FileInputStream("src/main/resources/fxml/images/Club.png");
                else
                    inputstream = new FileInputStream("src/main/resources/fxml/images/Spade.png");

                Image image = new Image(inputstream);
                ImageView tempImage = new ImageView();
                tempImage.setImage(image);
                tempImage.setFitHeight(20);
                tempImage.setFitWidth(20);
                tempImage.setX(242 + (25 * (dealer.size()-1)));
                tempImage.setY(80);
                DealerBot = tempImage;

                ObservableList<Node> childrens = AnchorPane.getChildren();
                for (Node node : childrens) {
                    if (node instanceof ImageView && !node.equals(PlayerBot))
                        node.setVisible(false);
                }
                AnchorPane.getChildren().add(tempImage);

            }


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private void DrawDealer() {
        Card temp = drawRandomCard();
        dealer.add(temp);
        displayDealerCard();
    }
    private void displayDealerCard(){
        Rectangle[] DealerCard = {DealerCard1, DealerCard2, DealerCard3, DealerCard4, DealerCard5, DealerCard6, DealerCard7};
        Text[] DealerText = {DealerText1, DealerText2,  DealerText3,  DealerText4,  DealerText5,  DealerText6,  DealerText7};

        for(int x = 0; x < dealer.size(); x++){
            DealerCard[x].setVisible(true);
            DealerText[x].setVisible(true);
            DealerText[x].setText(returnNum(dealer.get(x)));
            DealerBottomNum.setText(returnNum(dealer.get(x)));

            colorTheNums(x, false);
        }
        DealerBottomNum.setVisible(true);
        DealerBottomNum.setX(25 * (dealer.size()-1));
    }

    private void winner(String x){
        if(x.equals("Dealer")){
            DrawButton.setVisible(false);
            StandButton.setVisible(false);
            WinnerText.setText("You Lose");
        }
        else if(x.equals("Player")){
            DrawButton.setVisible(false);
            StandButton.setVisible(false);
            WinnerText.setText("You Win!");
        }
        else {
            DrawButton.setVisible(false);
            StandButton.setVisible(false);
            WinnerText.setText("It's a Tie!");
        }
    }
    private String returnNum(Card card){
        if(card.getNum() == 14)
            return "A";
        else if(card.getNum() == 13)
            return "K";
        else if(card.getNum() == 12)
            return "Q";
        else if(card.getNum() == 11)
            return "J";
        else
           return "" + card.getNum();
    }
    protected Card drawRandomCard(){
        int x  = (int) (Math.random() * deck.size());
        Card temp = deck.get(x);
        deck.remove(x);
        return temp;
    }
    private int getTotal(ArrayList<Card> hand){
        int total = 0;
        //add up face cards and regular numbers
        for (Card card : hand) {
            if (card.getNum() > 10 && card.getNum() != 14)
                total += 10;
            else if (card.getNum() != 14)
                total += card.getNum();
        }
        //add up Aces
        for (Card card : hand) {
            if (card.getNum() == 14) {
                if (total + 11 > 21)
                    total += 1;
                else
                    total += 11;
            }
        }
        return total;
    }
    private void hideAllCards(){
        PlayerCard1.setVisible(false);
        PlayerCard2.setVisible(false);
        PlayerCard3.setVisible(false);
        PlayerCard4.setVisible(false);
        PlayerCard5.setVisible(false);
        PlayerCard6.setVisible(false);
        PlayerCard7.setVisible(false);

        PlayerNum1.setVisible(false);
        PlayerNum2.setVisible(false);
        PlayerNum3.setVisible(false);
        PlayerNum4.setVisible(false);
        PlayerNum5.setVisible(false);
        PlayerNum6.setVisible(false);
        PlayerNum7.setVisible(false);

        DealerCard1.setVisible(false);
        DealerCard2.setVisible(false);
        DealerCard3.setVisible(false);
        DealerCard4.setVisible(false);
        DealerCard5.setVisible(false);
        DealerCard6.setVisible(false);
        DealerCard7.setVisible(false);

        DealerText1.setVisible(false);
        DealerText2.setVisible(false);
        DealerText3.setVisible(false);
        DealerText4.setVisible(false);
        DealerText5.setVisible(false);
        DealerText6.setVisible(false);
        DealerText7.setVisible(false);
    }
}