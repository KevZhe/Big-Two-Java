import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private final String[] SUITS = {"Clubs", "Diamonds", "Hearts", "Spades" };
    private final String[]  RANKS = {"3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace", "2"};
    private ArrayList<Card> deck;
    
	public Deck() {
		initializeDeck();
	}
	
	public void initializeDeck() {
		deck = new ArrayList<Card>();
		//initialize our deck with the 52 different Cards
		for(int i = 1; i <= 13; ++i) {
			for (String suit : SUITS) {
				deck.add(new Card(suit, i));
			}
		}
	}
	
	//shuffles the deck
	public void shuffleDeck() {
		Collections.shuffle(deck);
	}
	
	//repr
	public String toString() {
		String res = "";
		for (Card c : deck) {
			res += c.toString() + "\n";
		}
		return res;
	}
	
	public int getLength() {
		return deck.size();
	}
	
	//splits the hands into n hands
    public ArrayList<CardList> deal(int numPlayers) {
        ArrayList<CardList> hands = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            CardList hand = new CardList();
            int numCards = 52 / numPlayers;
            for (int j = 0; j < numCards; j++) {
                hand.addCard(deck.remove(0));
            }
            hands.add(hand);
        }
       
        return hands;
    }
	public static void main(String[] args) {
		Deck deck = new Deck(); 
		deck.shuffleDeck();
		System.out.println(deck.getLength());
	 }
	 
}
