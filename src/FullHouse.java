import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FullHouse extends PlayableHand {
	
	public FullHouse(ArrayList<Card> cards) {
		super(cards);
	}

	@Override
	public boolean isValid() {
		return isValid(this);
	}
	
	@Override
	public Card top() {
		//top card is the card that appears 3 times
		if (this.size() == 0) {
			return null;
		}

		Map<Integer, Integer> counts = new HashMap<>();
		//create mapping of card value to counts
		for(int i = 0; i < this.size(); ++i) {
			int val = this.getCard(i).getValue();
			counts.put(val, counts.getOrDefault(val, 0) + 1);
		}

		//return card of value that appears 3 times
		for (int val : counts.keySet()) {
			if (counts.get(val) == 3) {
				return new Card("Spades", val);
			}
		}
		return null;
	}

	public static boolean isValid(CardList combo) {
		//flags for if our combo has a triple and double
		boolean hasTriple = false;
		boolean hasDouble = false;
		
		//create mapping of card value to counts
		Map<Integer, Integer> counts = new HashMap<>();
		//make sure this combo is of size 5
		if (combo.size() != 5) {
			return false;
		}
		
		//create mapping of card value to counts
		for(int i = 0; i < combo.size(); ++i) {
			int val = combo.getCard(i).getValue();
			counts.put(val, counts.getOrDefault(val, 0) + 1);
		}
		
		//go through map and check if there is a triple and a double present
        for (int count : counts.values()) {
            if (count == 3) {
            	hasTriple = true;
            } else if (count == 2) {
            	hasDouble = true;
            }
        }
		
		return hasTriple && hasDouble;

	}


}
