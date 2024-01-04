import java.util.ArrayList;
import java.util.Arrays;


public class Flush extends PlayableHand {
	
	public Flush(ArrayList<Card> cards) {
		super(cards);
	}

	@Override
	public boolean isValid() {
		return isValid(this);
	}
	
	public static boolean isValid(CardList combo) {
		//check if all cards are same suit and length is correct
		if (combo.size() == 5) {
			for(int i = 0; i < combo.size() - 1; ++i) {
				if (!combo.getCard(i+1).getSuit().equals(combo.getCard(i).getSuit())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
