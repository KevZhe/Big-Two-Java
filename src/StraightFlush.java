import java.util.ArrayList;
import java.util.Arrays;


public class StraightFlush extends PlayableHand {
	
	public StraightFlush(ArrayList<Card> cards) {
		super(cards);
	}

	@Override
	public boolean isValid() {
		return isValid(this);
	}
	
	public static boolean isValid(CardList combo) {
		combo.sort();
		//check if this is both valid as a flush and a straight
        return Flush.isValid(combo) && Straight.isValid(combo);
	}
}
