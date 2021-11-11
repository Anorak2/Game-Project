package com.Main.Games;

import com.Main.MenuController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
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
    Rectangle PlayerCard1;
    @FXML
    Rectangle PlayerCard2;
    @FXML
    Rectangle PlayerCard3;
    @FXML
    Rectangle PlayerCard4;
    @FXML
    Rectangle PlayerCard5;
    @FXML
    Rectangle PlayerCard6;
    @FXML
    Rectangle PlayerCard7;
    @FXML
    Text PlayerNum1;
    @FXML
    Text PlayerNum2;
    @FXML
    Text PlayerNum3;
    @FXML
    Text PlayerNum4;
    @FXML
    Text PlayerNum5;
    @FXML
    Text PlayerNum6;
    @FXML
    Text PlayerNum7;
    @FXML
    Rectangle DealerCard1;
    @FXML
    Rectangle DealerCard2;
    @FXML
    Rectangle DealerCard3;
    @FXML
    Rectangle DealerCard4;
    @FXML
    Rectangle DealerCard5;
    @FXML
    Rectangle DealerCard6;
    @FXML
    Rectangle DealerCard7;
    @FXML
    Text DealerText1;
    @FXML
    Text DealerText2;
    @FXML
    Text DealerText3;
    @FXML
    Text DealerText4;
    @FXML
    Text DealerText5;
    @FXML
    Text DealerText6;
    @FXML
    Text DealerText7;
    @FXML
    Text PlayerBottomNum;
    @FXML
    Text DealerBottomNum;
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
    public void displayPlayerCard(){
        if(player.size() >= 1) {
            PlayerCard1.setVisible(true);
            PlayerNum1.setVisible(true);
            PlayerNum1.setText(returnNum(player.get(0)));
            PlayerBottomNum.setVisible(true);
        }
        if(player.size() >= 2) {
            PlayerCard2.setVisible(true);
            PlayerNum2.setVisible(true);
            PlayerNum2.setText(returnNum(player.get(1)));
        }
        if(player.size() >= 3) {
            PlayerCard3.setVisible(true);
            PlayerNum3.setVisible(true);
            PlayerNum3.setText(returnNum(player.get(2)));
        }
        if(player.size() >= 4) {
            PlayerCard4.setVisible(true);
            PlayerNum4.setVisible(true);
            PlayerNum4.setText(returnNum(player.get(3)));
        }
        if(player.size() >= 5) {
            PlayerCard5.setVisible(true);
            PlayerNum5.setVisible(true);
            PlayerNum5.setText(returnNum(player.get(4)));
        }
        if(player.size() >= 6) {
            PlayerCard6.setVisible(true);
            PlayerNum6.setVisible(true);
            PlayerNum6.setText(returnNum(player.get(5)));
        }
        if(player.size() >= 7) {
            PlayerCard7.setVisible(true);
            PlayerNum7.setVisible(true);
            PlayerNum7.setText(returnNum(player.get(6)));
        }
        PlayerBottomNum.setText(returnNum(player.get(player.size()-1)));
        PlayerBottomNum.setX(25 * (player.size()-1));
    }

    public void DrawDealer() {
        Card temp = drawRandomCard();
        dealer.add(temp);
        displayDealerCard();
    }
    public void displayDealerCard(){
        if(dealer.size() >= 1) {
            DealerCard1.setVisible(true);
            DealerText1.setVisible(true);
            DealerText1.setText(returnNum(dealer.get(0)));
        }
        if(dealer.size() >= 2) {
            DealerCard2.setVisible(true);
            DealerText2.setVisible(true);
            DealerText2.setText(returnNum(dealer.get(1)));
        }
        if(dealer.size() >= 3) {
            DealerCard3.setVisible(true);
            DealerText3.setVisible(true);
            DealerText3.setText(returnNum(dealer.get(2)));
        }
        if(dealer.size() >= 4) {
            DealerCard4.setVisible(true);
            DealerText4.setVisible(true);
            DealerText4.setText(returnNum(dealer.get(3)));
        }
        if(dealer.size() >= 5) {
            DealerCard5.setVisible(true);
            DealerText5.setVisible(true);
            DealerText5.setText(returnNum(dealer.get(4)));
        }
        if(dealer.size() >= 6) {
            DealerCard6.setVisible(true);
            DealerText6.setVisible(true);
            DealerText6.setText(returnNum(dealer.get(5)));
        }
        if(dealer.size() >= 7) {
            DealerCard7.setVisible(true);
            DealerText7.setVisible(true);
            DealerText7.setText(returnNum(dealer.get(6)));
        }
        DealerBottomNum.setText(returnNum(dealer.get(dealer.size()-1)));
        DealerBottomNum.setX(25 * (dealer.size()-1));
    }

    public void winner(String x){
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
    public String returnNum(Card card){
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
    public Card drawRandomCard(){
        int x  = (int) (Math.random() *deck.size());
        Card temp = deck.get(x);
        deck.remove(x);
        return temp;
    }
    public int getTotal(ArrayList<Card> hand){
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
    public void hideAllCards(){
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

