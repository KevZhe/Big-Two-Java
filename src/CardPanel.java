import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class CardPanel extends JPanel {
    private Card card;
    private String imagePath; //file path for the background image
    boolean selected;
    private static final int TRANSLATION_AMOUNT = 10;
    
    public CardPanel(Card card) {
        this.card = card;
        this.imagePath = "cards/"+ constructFileName(card);
        //set up the panel appearance 
        setPreferredSize(new Dimension(100, 150)); 
        setOpaque(false);

    }
    
    //blank card panel
    public CardPanel() {
        this.imagePath = "cards/"+ "blank.png";
        // Set up the panel appearance 
        setPreferredSize(new Dimension(100, 150)); 
        setOpaque(false);
    }
    
    //getter and setter for selected
    public boolean isSelected() {
    	return this.selected;
    }

    public void setSelected(boolean flag) {
    	this.selected = flag;
    }
    
    //getter for card
    public Card getCard() {
    	return this.card;
    }

    //repaints the card panel
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Image img = new ImageIcon(imagePath).getImage();
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }
    
    //fetches the correct file name for the card
    private String constructFileName(Card card) {
 
        Map<Integer, String> nameMap = Card.cardValues;

        int value = card.getValue();
        String name = nameMap.get(value);
        String suit = card.getSuit();

        return name.toLowerCase() + "_of_" + suit.toLowerCase() + ".png";
    }
}
