import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Quad extends PlayableHand {
	
	public Quad(ArrayList<Card> cards) {
		super(cards);
	}

	@Override
	public boolean isValid() {
		return isValid(this);
	}
	
	@Override
	public Card top() {
		//top card is the card that appears 4 times
		if (this.size() == 0) {
			return null;
		}

		Map<Integer, Integer> counts = new HashMap<>();
		//create mapping of card value to counts
		for(int i = 0; i < this.size(); ++i) {
			int val = this.getCard(i).getValue();
			counts.put(val, counts.getOrDefault(val, 0) + 1);
		}

		//return card of value that appears 4 times
		for (int val : counts.keySet()) {
			if (counts.get(val) == 4) {
				return new Card("Spades", val);
			}
		}
		return null;
	}
	public static boolean isValid(CardList combo) {
		//flags for if our combo has a quad and a single
		boolean hasQuad = false;
		boolean hasSingle = false;
		
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
		
		//go through map and check if there is a quad and a single present
        for (int count : counts.values()) {
            if (count == 4) {
            	hasQuad = true;
            } else if (count == 1) {
            	hasSingle = true;
            }
        }
		
		return hasQuad && hasSingle;

	}


}
