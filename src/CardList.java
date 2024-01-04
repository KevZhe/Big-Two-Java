import java.io.Serializable;
import java.util.ArrayList;

public class CardList implements Serializable {
	
	
	private static final long serialVersionUID = 1054582157568806335L;
	public ArrayList<Card> cards;
	
	//constructors
	public CardList() {
		cards = new ArrayList<Card>();
	}
	
	public CardList(ArrayList<Card> cards) {
		this.cards = cards;
	}
	
	//return size of card list
	public int size() {
		return this.cards.size();
	}
	
	//adds a card to the list
	public void addCard(Card card) {
		cards.add(card);
	}
	
	//get card at specific index
	public Card getCard(int idx) {
		if (idx >= 0 && idx < cards.size()) {
			return cards.get(idx);
		}
		else {
			return null;
		}
	}
	
	public void sort() {
		this.cards.sort(null);
	}
	
	public void removeCards(CardList cardsToRemove) {
		for (Card card : cardsToRemove.getCards()) {
			cards.remove(card);
		}
	}
	
	public ArrayList<Card> getCards() {
		return this.cards;
	}
	
	//checks if a given card is in this cardlist
	public boolean hasCard(Card toMatch) {
		for(Card card : this.cards) {
			if (card.compareTo(toMatch) == 0) {
				return true;
			}
		}
		return false;
	}
	
	//checks if a given cardlist is any of the playable hands
	//if so, returns the playable hand
	public static PlayableHand matchPlayableHand(CardList hand) {
		if (hand.size() == 5) {
			if (StraightFlush.isValid(hand)) return new StraightFlush(hand.cards);
			else if (FullHouse.isValid(hand)) return new FullHouse(hand.cards);
			else if (Flush.isValid(hand))  return new Flush(hand.cards); 
			else if (Straight.isValid(hand))  return new Straight(hand.cards); 
			else if (Quad.isValid(hand))  return new Quad(hand.cards); 
		}
		if (Triple.isValid(hand)) return new Triple(hand.cards);
		else if (Double.isValid(hand))  return new Double(hand.cards); 
		else if (Single.isValid(hand))  return new Single(hand.cards); 
		
		return null;
	}
	
	
	public String toString() {
		String res = "";
		for (Card c : cards) {
			res += c.toString() + "\n";
		}
		return res;
	}
	
}
