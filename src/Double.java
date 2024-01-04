import java.util.ArrayList;
import java.util.Arrays;


public class Double extends PlayableHand {
	
	public Double(ArrayList<Card> cards) {
		super(cards);
	}
	
	@Override
	public boolean isValid() {
		return isValid(this);
		
	}

	public static boolean isValid(CardList combo) {
		//check if our pair are the same rank 
		if (combo.size() == 2) {
			return combo.getCard(0).equals(combo.getCard(1));
		}
		return false;
		
	}
	
}
