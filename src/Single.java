import java.util.ArrayList;
import java.util.Arrays;


public class Single extends PlayableHand {
	
	public Single(ArrayList<Card> cards) {
		super(cards);
	}
	
	
	public static boolean beats(CardList card1, CardList card2) {
		Card c1 = card1.getCard(0);
		Card c2 = card2.getCard(0);

		//first check rank
		if (c1.getValue() > c2.getValue()) {
			return true;
		}
		else if (c1.getValue() < c2.getValue()) {
			return false;
		}// if rank same then check suit
		else {
			if (c1.getSuitValue() > c2.getSuitValue()) {
				return true;
			}
			else if (c1.getSuitValue() < c2.getSuitValue()) {
				return false;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean isValid() {
		return isValid(this);
	}

	public static boolean isValid(CardList combo) {
		return combo.size() == 1;
	}

}
