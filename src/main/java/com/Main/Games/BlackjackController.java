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
    }

    public void DrawPlayer(ActionEvent e){
        if(!Stand){
            Card temp = drawRandomCard();
            player.add(temp);
            displayPlayerCard(temp);
        }
        if(getTotal(player) > 21)
            dealerWins();
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
    public void DrawDealer(){
        Card temp = drawRandomCard();
        dealer.add(temp);
        displayDealerCard(temp);
    }

    public void dealerWins(){
        DrawButton.setVisible(false);
        StandButton.setVisible(false);
    }
    public void playerWins(){

    }

    public Card drawRandomCard(){
        int x  = (int) (Math.random() *deck.size() +1);
        Card temp = deck.get(x);
        deck.remove(x);
        return temp;
    }
    public void displayPlayerCard(Card card){
        String temp = "";
        if(card.getSuite().equals("Spade"))
            temp = " of Spades";
        if(card.getSuite().equals("Club"))
            temp = " of Clubs";
        if(card.getSuite().equals("Heart"))
            temp = " of Hearts";
        if(card.getSuite().equals("Diamond"))
            temp = " of Diamonds";
        PlayerHandText.setText(PlayerHandText.getText() + card.getNum() + temp + ", ");

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
        DealerHandText.setText(DealerHandText.getText() + card.getNum() + temp + ", ");
    }

    public int getTotal(ArrayList<Card> hand){
        int total = 0;
        for(int x = 0; x < hand.size(); x++){
            if(hand.get(x).getNum() > 10 && hand.get(x).getNum() != 14)
                total += 10;
            else if(hand.get(x).getNum() != 14)
                total += hand.get(x).getNum();
            else if(total + 11 > 21)
                total += 1;
            else
                total += 11;
        }
        return total;
    }
}

