import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class PlayField extends JFrame {
	
	Player player;
	private int id;
	private CardList hand;
	Map<Integer, Integer> otherPlayers;
	private String username;
	
	//gameplay fields
	PlayableHand lastPlayedHand = null;
	private int lastPlayer = 0;
	private int turn;
	
	//UI
	JLabel label;
	JPanel viewArea;
	JPanel playArea;
	JPanel lastPlayedPanel;
	JPanel[] otherPanels;
	CardPanel[] cardPanels;
	JPanel personalPanel;
	JPanel leftPanel;
	JPanel bottomLeft;
	JPanel personalArea;
	
	// IO streams
	ObjectOutputStream objectToServer = null;
	
	//UI
	JTextField textField = null;
	JTextArea textArea = null;
	Socket socket = null;

	
	private BlockingQueue<Message> messageQueue;
	
	public PlayField(Player player, BlockingQueue<Message> messageQueue, ObjectOutputStream outputStream, String username) {
		this.player = player;
		this.messageQueue = messageQueue;
		this.objectToServer = outputStream;
		this.username = username;

		//add label for window
		label = formatLabel("Big Two Game Board", 36);
		
		//console for text area
		textArea = new JTextArea(30,30);
		textArea.setEditable(false);
		textArea.append("Connected to Game Server\n");
		textArea.append("Waiting for game to start...\n");
		
		this.setLayout(new BorderLayout());
		this.add(label, BorderLayout.NORTH);
		this.add(textArea, BorderLayout.CENTER);
		setSize(400, 400);

		//start thread for updating UI
		UIUpdate ui = new UIUpdate();
		Thread uiThread = new Thread(ui);
		uiThread.start();

		Message message = new Message("SET_USERNAME", username);
		try {
			objectToServer.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//creates blue, centered JLabel of said string and size
	public static JLabel formatLabel(String string, int fontSize) {
		JLabel label = new JLabel(string);
		Font font = new Font("Arial", Font.BOLD, fontSize);
		label.setFont(font);
		label.setForeground(Color.BLUE);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		return label;
	}

	private void startUI(int n){
		//destroy existing panel
		getContentPane().removeAll();
		repaint();
		
		this.setLayout(new GridLayout(1,2));
		
		/* LEFT PANEL */
		
        otherPanels = new JPanel[n-1];
		//panel for left side of game board
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(4,1));
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : otherPlayers.entrySet()) {
        	JPanel cardsPanel = createBlankLayout("horizontal", entry.getValue()); // different player's cards
        	JPanel panel = formatPlayer(cardsPanel, "Player: " + entry.getKey()); // label for other player
        	otherPanels[i] = panel;
        	leftPanel.add(panel); // add to panel
        	i+=1;
        }
		//setup for bottom left panel, add label for Big Two
        bottomLeft = new JPanel();
        bottomLeft.setLayout(new BorderLayout());
        bottomLeft.add(label, BorderLayout.CENTER);
        leftPanel.add(bottomLeft);
        this.add(leftPanel);
        
		//right panel includes personal hand, what's been played, and console
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new GridLayout(2,1));
		
		//general region in top right storing cards played and console
		viewArea = new JPanel();
		viewArea.setLayout(new GridLayout(1,2)); //left for cards played and right for console
		viewArea.setBorder(new LineBorder(Color.BLACK));
		playArea = new JPanel();
		playArea.setLayout(new GridLayout(2,1)); //top for label and bottom for cards played
		//label for top of play area
		JLabel lplabel = formatLabel("Last Played Hand", 24);
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(lplabel, BorderLayout.SOUTH);
		playArea.add(labelPanel);
		//add blank panel for cards played for now
		lastPlayedPanel = new JPanel();	
		playArea.add(lastPlayedPanel);

		//console
		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane pane = new JScrollPane(textArea);

		viewArea.add(playArea);
		viewArea.add(pane);
		rightPanel.add(viewArea);
		
		//region in bottom right that stores personal hand 
		personalArea = new JPanel();
		personalArea.setLayout(new GridLayout(2,1)); //top stores buttons, bottom stores cards

        JPanel buttons = createPlayerControls();
		//wrap buttons to make them centered
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(2,1));
		buttonsPanel.add(buttons);
		JLabel personalLabel = formatLabel("Your Hand", 36);
		personalLabel.setHorizontalAlignment(SwingConstants.CENTER);
		buttonsPanel.add(personalLabel);
		
        JPanel personalCards = createPersonalPanel();
        personalPanel = formatPlayer(personalCards, "Player: " + this.username); //label for ourself

        personalArea.add(buttonsPanel);
        personalArea.add(personalPanel);
        rightPanel.add(personalArea);
        this.add(rightPanel);

        pack();
	}
	
	//formats a JPanel of cards with a label, and adds a border to it
	private JPanel formatPlayer(JPanel cards, String label) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(cards, BorderLayout.WEST);
		JLabel l = new JLabel(label);
		panel.add(l, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return panel;
	}
	
	//panel for storing played cards
	private JPanel createPlayedPanel(CardList playedCards) 
	{
		Point overlap = new Point(20,0);
		OverlapLayout layout = new OverlapLayout(overlap); //custom layout for overlapping cards
		JPanel panel = new JPanel(layout);
		//for all cards in CardList, format properly and add to layout
		for (int i=0; i < playedCards.size(); ++i) {
			Card card = playedCards.getCard(i);
			CardPanel cardp = new CardPanel(card);
			cardp.setBackground(Color.WHITE);
			cardp.setBorder(new LineBorder(Color.BLACK));
			panel.add(cardp);
		}
 
        return panel;
	}
	
	//creates JPanel of blank cards (representing other players)
	private JPanel createBlankLayout(String orientation, int nCards) 
	{
		Point overlap;
		//store vertically or horizontally
		if (orientation == "vertical") {overlap = new Point(0,20);}
		else {overlap = new Point(20,0);}
		
		OverlapLayout layout = new OverlapLayout(overlap); //custom layout for overlapping cards
		JPanel panel = new JPanel(layout);
		//for some nCards, create n blank panels, format properly, and add to layout
		for (int i=0; i < nCards; ++i) {
			CardPanel card = new CardPanel();
			card.setBackground(Color.WHITE);
			card.setBorder(new LineBorder(Color.BLACK));
			panel.add(card);
		}
 
        return panel;
	}
	//creates panel for personal cards
	private JPanel createPersonalPanel()
	{
		//setup for overlap layout (horizontal layout)
		Point overlap = new Point(20,0);
		OverlapLayout layout = new OverlapLayout(overlap);
		Insets popupInsets = new Insets(20,0,0,0);
		layout.setPopupInsets(popupInsets);
		
		JPanel panel = new JPanel(layout);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10) );

        this.hand.sort();
        cardPanels = new CardPanel[hand.size()]; 

        for (int i = 0; i < hand.size(); i++) {
			//fetch card and store in panels
        	CardPanel card =  new CardPanel(hand.getCard(i));
        	cardPanels[i] = card;
			//format each card
			card.setBackground( Color.WHITE );
			card.setBorder(new LineBorder(Color.BLACK));
			card.addMouseListener(
					(new MouseAdapter() {
		                @Override
		                public void mousePressed(MouseEvent e) { //listener for clicks
		            		Component c = e.getComponent();
		            		Boolean constraint = layout.getConstraints(c);
		            		CardPanel panel = (CardPanel) c;
							//pop card up and set flag for selected in card panel to true
		            		if (constraint == null || constraint == OverlapLayout.POP_DOWN) {
		            			layout.addLayoutComponent(c, OverlapLayout.POP_UP);
		            			panel.setSelected(true);
		            		}
		            		else {//pop card down and set flag for selected in card panel to false
		            			layout.addLayoutComponent(c, OverlapLayout.POP_DOWN);
		            			panel.setSelected(false);
		            		}
		            		((JComponent)c).revalidate();
		                }
		            })
		);
			
		panel.add(card);
        }
        
        return panel;
	}
	
    private JPanel createPlayerControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
		//buttons for play, suggest, and pass
        JButton playButton = new JButton("Play");
        JButton suggestButton = new JButton("Suggest");
        JButton passButton = new JButton("Pass");
        JPanel buttonsPanel = new JPanel();
        
	    //give button listener for play button
	    class PlayListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				playSelectedCards();
			}
	    }
	    //give button listener for suggest button
	    class SuggestListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				//generate all possible hands
				ArrayList<PlayableHand> hands = PlayableHand.generatePlayableHand(hand, lastPlayedHand);
				if (hands.size() == 0) {
					textArea.append("No Suggestions\n");
				}
				for (int i = 0; i < hands.size(); ++i) {
					textArea.append("Suggestion:\n"+ hands.get(i).toString() + "\n");
				}
			}
	    }
	    //give button listener for pass button
	    class PassListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				Message pass = new Message("PASS", null);
				try {
					if (lastPlayer == id || lastPlayer == 0) { //if our turn, or freebie
						textArea.append("You can't pass on your free turn!\n"); 
						throw new Exception();
					}
					if (turn != id) { //if out of turn
						textArea.append("You can't pass out of turn!\n");
						throw new Exception();
					}
					objectToServer.writeObject(pass); //send to server
				} catch (Exception e1) {
					//e1.printStackTrace();
				}
			}
	    }
	    
	    //bind listener
	    ActionListener play = new PlayListener();
	    playButton.addActionListener(play);
	    
	    //bind listener
	    ActionListener suggest = new SuggestListener();
	    suggestButton.addActionListener(suggest);
	    
	    //bind listener
	    ActionListener pass = new PassListener();
	    passButton.addActionListener(pass);
	    
        buttonsPanel.add(playButton);
        buttonsPanel.add(suggestButton);
        buttonsPanel.add(passButton);
        panel.add(buttonsPanel, BorderLayout.CENTER);

        return panel;
    }
    
	public boolean playSelectedCards() {
		
		System.out.println("Turn:" + this.turn +" Player ID" + id);
		//check if we're playing out of turn
    	if (this.turn != id) {
    		textArea.append("You can't play when it's not your turn!\n");
    		return false;
    	}

		//fetch all cards in panels that are highlighted
    	ArrayList<Card> selectedCards = new ArrayList<Card>();
    	for (CardPanel panel : this.cardPanels) {
    		if (panel.isSelected()) {
    			selectedCards.add(panel.getCard());
    		}
    	}
    	
    	//first rule, the player that has 3 of diamonds must play it in any combo during their first turn
    	CardList cards = new CardList(selectedCards);
    	if (lastPlayer == 0 && !cards.hasCard(new Card("Diamonds", 1))) {
    		textArea.append("You must play the 3 of Diamonds on your first turn!\n");
    		return false;
    	}


    	PlayableHand hand = CardList.matchPlayableHand(cards);
    	
    	//see if this valid and beats what's on the table
    	if (hand != null) {
    		//Check if this hand beats current hand on deck & also turn order
    		if (hand.beats(lastPlayedHand) || lastPlayer == this.id) {
        		try {
					// play is valid, inform ourselves to update UI
					Message newMessage = new Message("PLAY", this.id, hand);
					synchronized (messageQueue) {
						messageQueue.add(newMessage);
					}
					//send play to server
					objectToServer.writeObject(newMessage);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		return true;
    		}
    		return false;
    	}
    	else {
    		return false;
    	}
	}
	
	/************* 
	 * UI Update Class
	*************/
    
	public class UIUpdate implements Runnable {

	    public UIUpdate() {}

	    @Override
	    public void run() {
	        while (true) {
	        	try { // continuously read in messages and process them
	                Message message = messageQueue.take();
	                SwingUtilities.invokeLater(() -> processMessage(message));
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	        }
	    }

		//updates play area with correct hand
	    private void updatePlayArea(CardList playedHand) {
		    playArea.remove(lastPlayedPanel);
		    lastPlayedPanel = createPlayedPanel(playedHand);
		    playArea.add(lastPlayedPanel, 1);
	    }

	    //resets all player's borders to black 
	    private void resetColors() {
	    	personalPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	    	for (JPanel otherPanel : otherPanels) {
	    		otherPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	    	}
	    }
	    
	    private void processMessage(Message message) {
	    	String command = message.getMessageType();
	    	//System.out.println(command);
			try {
				if(command.equals("DEAL")){
					//read in dealt hand and update, also reset game status
					CardList dealtHand = (CardList) message.getData();
					hand = dealtHand;
					lastPlayedHand = null;
					lastPlayer = 0;
				}
				else if(command.equals("SETID")){
					//read in assigned id and update 
					int assignedID = (int) message.getData();
					System.out.println("Setting ID to " + assignedID);
					id = assignedID;
				}
				else if(command.equals("START")) {
					//create mapping of playerID to numCards for all other players
					int n = 52 / hand.size(); //number of players
					otherPlayers = new LinkedHashMap<Integer, Integer>();
					for (int i = 1; i < n ; ++i) {
						int otherid = id + i;
						if (otherid > n) otherid %= n;
						otherPlayers.put(otherid, hand.size());
					}
					startUI(n); //start board 
					textArea.append("Player: " + id + "\n");
				}
				else if(command.equals("OTHER_PLAYED")){
					//fetch played hand by player and assign it to the last played hand
					CardList playedHand = (CardList) message.getData();
					lastPlayedHand = (PlayableHand) playedHand;
					
					//find which player played this relative to our UI
					int playedID = message.from();
					lastPlayer = playedID;
			        int index = 0;
			        for (Map.Entry<Integer, Integer> entry : otherPlayers.entrySet()) {
			            if (entry.getKey().equals(playedID)) {
			                break;
			            }
			            index++;
			        }

			        //update this player's cards with new panel
			        JPanel otherPlayer = otherPanels[index];
			        leftPanel.remove(otherPlayer); // remove old player panel
			        otherPlayers.compute(playedID, (key, value) -> value - playedHand.size()); //update mapping of id to num cards leftr
			        otherPlayer = createBlankLayout("horizontal", otherPlayers.get(playedID));
			        otherPlayer = formatPlayer(otherPlayer, "Player: " + playedID);
			        leftPanel.add(otherPlayer, index); // add updated panel in correct place
			        otherPanels[index] = otherPlayer;

			        updatePlayArea(playedHand);
			        
				    revalidate();
				    repaint();
			        
			    }
				else if(command.equals("PLAY")){
					//fetch played hand and remove all cards
					CardList playedHand = (CardList) message.getData();
					hand.removeCards(playedHand);
					
					//update last played to ourselves
					lastPlayedHand = (PlayableHand) playedHand;
					lastPlayer = id;
					
					//update our personal panel to reflect new cards
					SwingUtilities.invokeLater(() -> {
						personalArea.remove(personalPanel);
						JPanel personalCards = createPersonalPanel();
						personalPanel = formatPlayer(personalCards, "Player: " + username);
					    personalArea.add(personalPanel, 1);
					    revalidate();
					    repaint();
					});
					
					updatePlayArea(playedHand);
					
					//if we're out of cards announce that the game is over
					if (hand.size() == 0) {
						Message goMessage = new Message("GAME_OVER", null);
						try {
							objectToServer.writeObject(goMessage);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					
				}
				else if(command.equals("TURN")){
					int currentTurn = (int) message.getData();
					turn = currentTurn; //update turn 
					resetColors(); 
			        if (turn == id) { //if current turn is ourselves, highlight our playfield
			        	personalPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
			        }
			        else { // otherwise, find the correct player's turn and update their panel
				        int index = 0;
				        for (Map.Entry<Integer, Integer> entry : otherPlayers.entrySet()) {
				            if (entry.getKey().equals(turn)) break;
				            index++;
				        }
				        JPanel otherPanel = otherPanels[index];
				        otherPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
			        }
				    revalidate();
				    repaint();
				}
				else if(command.equals("GAME_OVER")){ // if game over, announce winner
					int winner = (int) message.getData();
					textArea.append("Winner of game: Player " + winner + "\n");
				}
				else if (command.equals("EXIT")) {
					//if server exits, close window and socket
					System.out.println("Exiting");
					player.addToTextArea("Disconnected from server\n");
					//close socket
					if (socket!=null)  {
						socket.close();
					}
					player.resetSocket();
					//close window
					SwingUtilities.invokeLater(() -> {
						dispose();
					});
				}
				else {
					System.out.println("no match for " + command);
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
}
