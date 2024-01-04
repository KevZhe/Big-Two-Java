import java.util.ArrayList;
import java.util.Arrays;

public abstract class PlayableHand extends CardList{
	//combo ordering for comparison
	private String[] comboOrder = {"Straight", "Flush", "FullHouse", "Quad", "StraightFlush"};
	
	public PlayableHand(ArrayList<Card> cards) {
		super(cards);
	}
	
	//returns the top card of the hand
	public Card top() {
		if (this.size() == 0) {
			return null;
		}
		this.sort();
		return this.getCard(this.size() -1);
	}
	
	public boolean beats(PlayableHand otherHand) {
		
		//anything we play here beats null hand
		if (otherHand == null) {
			return true;
		}
		
		//if hand sizes are different no comparison is needed
		if (this.size() != otherHand.size()) {
			return false;
		}
		
		
		//if both hands are combos (size 5), then need special comparison
		if(this.size() == 5) {
			//find ranking in combo ordering 
			String thisComboName = this.getClass().getSimpleName();
			String otherComboName = otherHand.getClass().getSimpleName();
	        int tIndex = Arrays.asList(comboOrder).indexOf(thisComboName);
	        int oIndex = Arrays.asList(comboOrder).indexOf(otherComboName);
	        //compare
	        if (tIndex > oIndex) {
	        	return true;
	        }
	        else if (tIndex < oIndex) {
	        	return false;
	        }
		}
		
		//otherwise we are dealing with hands of the same type, just need to compare top cards
		Card c1 = this.top();
		Card c2 = otherHand.top();
		return (c1.compareTo(c2) == 1 ? true : false);
		
	};
	//generate all possible playable hands from a given set of cards
	public static ArrayList<PlayableHand> generatePlayableHand(CardList cards, PlayableHand toBeat) {
		
		ArrayList<PlayableHand> combos = new ArrayList<PlayableHand>();
		//if we are not trying to beat anything, then just play first card
		if (toBeat == null) {
			cards.sort();
			allPlayableHands(cards, new ArrayList<Card>(), combos, 0);
			/*
			Card firstCard = cards.getCard(0);
			PlayableHand hand = new Single(new ArrayList<Card>(Arrays.asList(firstCard)));
			combos.add(hand);
			*/
			return combos;
		}
		helper(cards, toBeat, new ArrayList<Card>(), combos, 0);
		
		return combos;
	}

	//recursive helper to generate all possible playable hands
	private static void helper(CardList cards, PlayableHand toBeat, ArrayList<Card> currentCombo, ArrayList<PlayableHand> result, int start) {
		//if we have a valid combo 
		if (currentCombo.size() == toBeat.size()) {
			//check if it beats the hand we are trying to beat and is a playable hand
			CardList combo = new CardList(new ArrayList<Card>(currentCombo));
			PlayableHand hand = CardList.matchPlayableHand(combo);
			//if so add it to the result
			if (hand != null && hand.beats(toBeat)) {
				result.add(hand);
			}
			return;
		}
		//enumerate all possible combos
		for (int i = start; i < cards.size(); i++) {
			currentCombo.add(cards.getCard(i));
			helper(cards, toBeat, currentCombo, result, i + 1);
			currentCombo.remove(currentCombo.size() - 1);
		}
	}

	private static void allPlayableHands(CardList cards, ArrayList<Card> currentCombo, ArrayList<PlayableHand> result, int start) {

		if (currentCombo.size() > 5) {
			return;
		}

		//create new combo from current list and try to match a hand
		CardList combo = new CardList(new ArrayList<Card>(currentCombo));
		PlayableHand hand = CardList.matchPlayableHand(combo);

		//if match add it to the result
		if (hand != null) {
			result.add(hand);
		}

		//enumerate all possible combos
		for (int i = start; i < cards.size(); i++) {
			currentCombo.add(cards.getCard(i));
			allPlayableHands(cards, currentCombo, result, i + 1);
			currentCombo.remove(currentCombo.size() - 1);
		}
	}

	public abstract boolean isValid();
	
	
}
