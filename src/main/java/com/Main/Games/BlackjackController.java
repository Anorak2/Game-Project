package com.Main.Games;

import com.Main.MenuController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class BlackjackController extends MenuController {
    private class Card implements Comparable<Card>{
        String suite = "";
        int num = -1;

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
    Text PlayerHandText;
    @FXML
    Text DealerHandText;
    @FXML
    Text WinnerText;
    ArrayList<Card> deck = new ArrayList<Card>();
    ArrayList<Card> player = new ArrayList<Card>();
    ArrayList<Card> dealer = new ArrayList<Card>();
    Boolean Stand = false;

    public void initialize() {
        //create deck
        for (int x = 2; x < 15; x++)
            deck.add(new Card("Spade", x));
        for (int x = 2; x < 15; x++)
            deck.add(new Card("Club", x));
        for (int x = 2; x < 15; x++)
            deck.add(new Card("Heart", x));
        for (int x = 2; x < 15; x++)
            deck.add(new Card("Diamond", x));
        PlayerHandText.setText("");
        DealerHandText.setText("");
        DrawDealer();
        WinnerText.setText("");
    }

    public void DrawPlayer(ActionEvent e){
        if(!Stand){
            Card temp = drawRandomCard();
            player.add(temp);
            displayPlayerCard(temp);
        }
        if(getTotal(player) > 21)
            dealerWins();
        else if (getTotal(player) == 21)
            playerWins();
    }
    public void StandPlayer(ActionEvent e){
        Stand = true;
        DrawButton.setVisible(false);
        StandButton.setVisible(false);
        while(true) {
            DrawDealer();
            if (getTotal(dealer) > getTotal(player) || getTotal(dealer) > 21)
                break;
        }
        if(getTotal(dealer) > 21)
            playerWins();
        else if(getTotal(dealer) > getTotal(player))
            dealerWins();
        else
            playerWins();
    }
    public void playerWins(){
        DrawButton.setVisible(false);
        StandButton.setVisible(false);
        WinnerText.setText("You Win!");
    }
    public void displayPlayerCard(Card card){
        String temp = "";
        if(card.getSuite().equals("Spade"))
            temp = " of Spades";
        else if(card.getSuite().equals("Club"))
            temp = " of Clubs";
        else if(card.getSuite().equals("Heart"))
            temp = " of Hearts";
        else if(card.getSuite().equals("Diamond"))
            temp = " of Diamonds";
        if(card.getNum() == 14)
            PlayerHandText.setText(PlayerHandText.getText() + "Ace" + temp + ", ");
        else if(card.getNum() == 13)
            PlayerHandText.setText(PlayerHandText.getText() + "King" + temp + ", ");
        else if(card.getNum() == 12)
            PlayerHandText.setText(PlayerHandText.getText() + "Queen" + temp + ", ");
        else if(card.getNum() == 11)
            PlayerHandText.setText(PlayerHandText.getText() + "Jack" + temp + ", ");
        else
            PlayerHandText.setText(PlayerHandText.getText() + card.getNum() + temp + ", ");

    }

    public void DrawDealer(){
        Card temp = drawRandomCard();
        dealer.add(temp);
        displayDealerCard(temp);
    }
    public void dealerWins(){
        DrawButton.setVisible(false);
        StandButton.setVisible(false);
        WinnerText.setText("You Lose");
    }
    public void displayDealerCard(Card card){
        String temp = "";
        if(card.getSuite().equals("Spade"))
            temp = " of Spades";
        if(card.getSuite().equals("Club"))
            temp = " of Clubs";
        if(card.getSuite().equals("Heart"))
            temp = " of Hearts";
        if(card.getSuite().equals("Diamond"))
            temp = " of Diamonds";
        if(card.getNum() == 14)
            DealerHandText.setText(DealerHandText.getText() + "Ace" + temp + ", ");
        else if(card.getNum() == 13)
            DealerHandText.setText(DealerHandText.getText() + "King" + temp + ", ");
        else if(card.getNum() == 12)
            DealerHandText.setText(DealerHandText.getText() + "Queen" + temp + ", ");
        else if(card.getNum() == 11)
            DealerHandText.setText(DealerHandText.getText() + "Jack" + temp + ", ");
        else
            DealerHandText.setText(DealerHandText.getText() + card.getNum() + temp + ", ");
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
        for(int x = 0; x < hand.size(); x++){
            if(hand.get(x).getNum() > 10 && hand.get(x).getNum() != 14)
                total += 10;
            else if(hand.get(x).getNum() != 14)
                total += hand.get(x).getNum();
        }
        //add up Aces
        for(int x = 0; x < hand.size(); x++){
            if(hand.get(x).getNum() == 14){
                if(total + 11 > 21)
                    total += 1;
                else
                    total += 11;
            }
        }
        return total;
    }
}

