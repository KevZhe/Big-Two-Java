import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Card implements Comparable<Card>, Serializable{
	

	private static final long serialVersionUID = -5080014665968565092L;
	// each card has its own suit and value
	private String suit;
	private int value;
	public static Map<Integer, String> cardValues = new HashMap<>();
	//order of suits for comparison
	private String[] suitsOrder = {"Diamonds", "Clubs", "Hearts", "Spades"}; //
    static {
        //Initialize the mapping of card names to values in a static block
    	cardValues.put(13, "2");
        cardValues.put(12, "Ace");
        cardValues.put(11, "King");
        cardValues.put(10,  "Queen");
        cardValues.put(9, "Jack");
        cardValues.put(8, "10");
        cardValues.put(7, "9");
        cardValues.put(6, "8");
        cardValues.put(5, "7");
        cardValues.put(4, "6");
        cardValues.put(3, "5");
        cardValues.put(2, "4");
        cardValues.put(1, "3");
    }	
	
	public Card(int value) {
		this.value = value;
	}
	
	//Constructs a card with a suit and a value
	public Card(String suit, int value) {
		this.suit = suit;
		this.value = value;
	}
	
	//getters
	public String getSuit() {
		return this.suit;	
	}
	
	//get the suit value for comparison
	public int getSuitValue() {
		switch (this.suit) {
			case "Spades":
				return 4;
			case "Hearts":
				return 3;
			case "Clubs":
				return 2;
			case "Diamonds":
				return 1;
		}
		return -1;
		
	}
	
	//get main card value
	public int getValue() {
		return this.value;
	}

	
	//compare card values based on value and suit
	@Override
	public int compareTo(Card o) {
		if (this.value > o.value) {
			return 1;
		} else if (this.value < o.value) {
			return -1;
		}
		else {
	        // ranks are equal, compare suits
	        int idx1 = Arrays.asList(suitsOrder).indexOf(this.suit);
	        int idx2 = Arrays.asList(suitsOrder).indexOf(o.suit);
	        return Integer.compare(idx1, idx2);
	    }
	}
	
	//check if two cards represent the same card
	public boolean equals(Card card) {
		return (this.value == card.getValue());
	}
	
	//repr for card
	public String toString() {
		//return joker
		if (this.suit == null) {
			return cardValues.get(this.value);
		}
		return cardValues.get(this.value) + " of " + this.getSuit(); 
	}
	

}
