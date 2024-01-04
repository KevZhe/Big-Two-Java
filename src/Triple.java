import java.util.ArrayList;
import java.util.Arrays;


public class Triple extends PlayableHand {
	
	public Triple(ArrayList<Card> cards) {
		super(cards);
	}
	

	@Override
	public boolean isValid() {
		return isValid(this);
	}

	public static boolean isValid(CardList combo) {
		//check if all cards are of the same value
		if (combo.size() == 3) {
			for(int i = 0; i < combo.size() - 1; ++i) {
				if (!combo.getCard(i).equals(combo.getCard(i+1))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
