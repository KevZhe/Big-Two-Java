import java.util.ArrayList;
import java.util.Arrays;


public class Straight extends PlayableHand {
	
	public Straight(ArrayList<Card> cards) {
		super(cards);
	}

	@Override
	public boolean isValid() {
		return isValid(this);
	}
	
	public static boolean isValid(CardList combo) {
		combo.sort();
		//check if all cards are in line and increasing in value by 1
		//special case, cannot have 2 in straight
		if (combo.size() == 5 && combo.getCard(4).getValue() != 13) {
			for(int i = 0; i < combo.size() - 1; ++i) {
				if ((combo.getCard(i+1).getValue() - combo.getCard(i).getValue()) != 1) {
					return false;
				}
			}
			return true;
		}
		return false;
	}


}
