import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GameServer extends JFrame implements Runnable {

	// text area to display connection status
	JTextArea ta;

    //store all active clients
	private ArrayList<HandlePlayerClient> clients = new ArrayList<>();
	
	private int turn;
	int numPlayers = 4;

	private ArrayList<ComputerPlayer> computerPlayers = new ArrayList<ComputerPlayer>();

	//keep track of what client nums 1-4 are taken
	private boolean[] taken = new boolean[4];

	//socket for server
	private ServerSocket serverSocket;

	public GameServer() {
		super("Game Server");
		this.setLayout(new BorderLayout());

		JLabel label = PlayField.formatLabel("Big Two Server", 36);
		this.add(label, BorderLayout.NORTH);
		
		//add new text area for server
		ta = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(ta);
		ta.setEditable(false);
		
		// create options for game
	    JPanel button = createStartButton();
		JPanel playerOptions= createNumPlayers();
		JPanel panel = new JPanel();

		this.add(scrollPane, BorderLayout.CENTER);
		panel.add(button);
		panel.add(playerOptions);
		this.add(panel, BorderLayout.SOUTH);
	    
		//add menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Options");
		menu.add(createExitItem());
		menu.add(ridComputers());
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
		
	    setSize(600, 600);
		//disconnect when window closed
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				exitWindow();
			}
		});
	    //start new thread for this server
	    Thread t = new Thread(this);
	    t.start();
	}
	
	//getters & setters
	public boolean[] getTaken() {
		return taken;
	}
	public void setTaken(boolean[] taken) {
		this.taken = taken;
	}

	public ArrayList<HandlePlayerClient> getClients() {
		return clients;
	}
	
	public int getTurn() {
		return this.turn;
	}
	
	//adds string to text area
	public void addToTextArea(String s) {
		ta.append(s);
	}
	
	//adjusts client numbers when a client exits
	public void moveTaken() {
		//move all taken clients down one
		for (int i = 0; i < taken.length - 1; ++i) {
			if(!taken[i]) {
				taken[i] = taken[i+1];
				taken[i+1] = false;
				//reassign client numbers
				for (HandlePlayerClient client : clients) {
					if (client.getClientNum() == i + 2) {
						client.setClientNum(i + 1);
					}
				}
				//reassign computer numbers
				for (ComputerPlayer computer : computerPlayers) {
					if (computer.getID() == i + 2) {
						computer.setID(i + 1);
					}
				}
			}
		}
	}

	public void exitWindow() {
		//send exit message to all clients
		for (HandlePlayerClient client : clients) {
			client.sendToClient("EXIT", null);
		}

		//close server socket
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//exit all computer players
		for (ComputerPlayer computer : computerPlayers) {
			computer.exit();
		}
		//close server
		SwingUtilities.invokeLater(() -> {
			dispose();
		});
	}

	//exit item for menu bar
	public JMenuItem createExitItem() {
		JMenuItem exitItem = new JMenuItem("Exit");
		class ExitItemListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				exitWindow();
			}
		}
		ActionListener exit = new ExitItemListener();
		exitItem.addActionListener(exit);
		return exitItem;
	}

	public JMenuItem ridComputers() {
		JMenuItem rid = new JMenuItem("Remove Computers");
		class ClearComputersListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				//remove all computer players
				for (ComputerPlayer computer : computerPlayers) {
					computer.exit();
					int id = computer.getID();
					taken[id - 1] = false;
				}
				computerPlayers.clear();
			}
		}
		//bind listener
		ActionListener listener = new ClearComputersListener();
		rid.addActionListener(listener);
		return rid;
	}

	//start & end game button
	public JPanel createStartButton() {
	    JButton startButton = new JButton("Start New Game");
		JButton endButton = new JButton("End Game");
	    //give button listener for play button
	    class StartListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				//check if we have enough players
				if (clients.size()  == 0) {
					ta.append("No players connected, cannot start game\n");
				}
				else if (clients.size() + computerPlayers.size() == numPlayers) { 
					//valid player count, start new game
					startNewGame(numPlayers); 
				}
				else if (clients.size() + computerPlayers.size() < numPlayers) {
					//not enough players, add computer players
					int numComputers = numPlayers - clients.size() - computerPlayers.size();
					for (int i = 0; i < numComputers; ++i) {
						// find next available id and add computer player
						int id = findNextAvailable();
						taken[id] = true;
						//create new computer player
						ComputerPlayer computer = new ComputerPlayer(GameServer.this, id + 1);
						computerPlayers.add(computer);
						//start new thread for computer player
						Thread t = new Thread(computer);
						t.start();
					}
					startNewGame(numPlayers);
				}
				else {
					ta.append("Too many players to start game\n");
				}
				
			}
	    }
	    
		endButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//check if game is already over
				if (clients.size() + computerPlayers.size() == 0) {
					ta.append("No game to end\n");
					return;
				}
				//end game with player 2 winning, for debugging mainly
				sendToAll("GAME_OVER", 2);
				updateResults(0);
			}
		});

	    //bind listener
	    ActionListener start = new StartListener();
	    startButton.addActionListener(start);

	    JPanel panel = new JPanel();
	    panel.add(startButton);
		panel.add(endButton);

	    return panel;
	}

	public JPanel createNumPlayers() {
		//drop down menu
		JComboBox<Integer> comboBox = new JComboBox<Integer>();

		// add label for button
		JLabel label = new JLabel("Number of Players: ");

		//panel for storing button
		JPanel panel = new JPanel();

		//add options
		comboBox.addItem(2);
		comboBox.addItem(3);
		comboBox.addItem(4);

		//default to 4
		comboBox.setSelectedIndex(2);

		//listener for combo box
		class NumPlayersListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				//get number of players
				int n = (int) comboBox.getSelectedItem();
				System.out.println("n changed to " + n);
				numPlayers = n;
				//check if too many players
				if (clients.size() > numPlayers) {
					ta.append("Too many players\n");
					return;
				}
				//remove computer players if necessary
				if (clients.size() + computerPlayers.size() > numPlayers) {
					int numToRemove = clients.size() + computerPlayers.size() - numPlayers; 
					//remove from end, have threads exit, and free up client numbers
					for (int i = 0; i < numToRemove; ++i) {
						ComputerPlayer computer = computerPlayers.get(computerPlayers.size() - 1);
						computer.exit();
						computerPlayers.remove(computer);
						taken[computer.getID() - 1] = false;
						System.out.println("slot " + (computer.getID() -1) + " freed up");
					}
				}
			}
		}

		//listener for play button
		class ListPlayerListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				//list all players
				ta.append("List of Players: \n");
				for (int i = 0; i < clients.size(); ++i) {
					ta.append("Player " + (i+1) + "\n");
				}
				for (int i = 0; i < computerPlayers.size(); ++i) {
					ta.append("Computer " + (i+clients.size() + 1) + "\n");
				}
			}
		}

		ActionListener numPlayers = new NumPlayersListener();
		comboBox.addActionListener(numPlayers);

		ActionListener list = new ListPlayerListener();
		JButton button = new JButton("List Players");
		button.addActionListener(list);

		panel.add(label);
		panel.add(comboBox);
		panel.add(button);

		return panel;
	}

	//finds next available client number in boolean array
	public int findNextAvailable() {
		//check if not taken
		for (int i = 0; i < taken.length; ++i) {
			if (!taken[i])
				return i;
			}
		return -1;
	}
		
	public void run() {
	  try {
        // Create a server socket at port 9898
	    serverSocket = new ServerSocket(9898, 0, InetAddress.getByName(null));
		//display current time
		ta.append("Game server started at " + new Date() + '\n');
		//listen for a new connection request
		
		//check if we have enough players
	    while (true) {
		  Socket socket = serverSocket.accept();
		  //check if we have enough players
		  if (clients.size() + computerPlayers.size() >= numPlayers) {
			  ta.append("Too many players, cannot accept new connection\n");
			  socket.close();
			  continue;
		  }
		  //find an available player number
		  int clientNo = findNextAvailable();
	      taken[clientNo] = true;

	      ta.append("Starting thread for client " + (clientNo + 1) +
	          " at " + new Date() + '\n');
	
	      //find the client's host name, and IP address
	      InetAddress inetAddress = socket.getInetAddress();
	      ta.append("Player Client " + (clientNo + 1) + "'s host name is "
	          + inetAddress.getHostName() + "\n");
	      ta.append("Player Client " + (clientNo + 1) + "'s IP Address is "
	          + inetAddress.getHostAddress() + "\n");
	      
	      //create new thread to handle client and add to our list
	      HandlePlayerClient client = new HandlePlayerClient(this, socket, clientNo + 1);
	      clients.add(client);
	      
	      //create and start a new thread for the connection
	      new Thread(client).start();
		  
	    }
	  }
      catch(IOException ex) {
        System.err.println(ex);
      }
	}
	
	
	public void startNewGame(int n) {
		
		ta.append("New game started with " + n + " players\n");

		//initialize new deck and shuffle it
        Deck deck = new Deck();
        deck.shuffleDeck();

		//fetch all hands based on number of players
        ArrayList<CardList> hands = deck.deal(n);
        Card threeDiamond = new Card("Diamonds", 1);

        //deal hands to all the player threads
        for (int i = 0; i < clients.size(); ++i) {
        	HandlePlayerClient client = clients.get(i);
        	CardList hand = hands.get(i);
        	//check if hand has 3 of diamonds (who starts)
        	if (hand.hasCard(threeDiamond)) {
				System.out.println("Player " + (i+1) + " has 3 of diamonds");
        		this.turn = i + 1;
        	}
        	client.sendToClient("DEAL", hand);
        	client.sendToClient("START", null);
        }

		//deal hands to all the computer threads
		for (int i = 0; i < computerPlayers.size(); ++i) {
			ComputerPlayer computer = computerPlayers.get(i);
			CardList hand = hands.get(i + clients.size());
			//check if hand has 3 of diamonds (who starts)
        	if (hand.hasCard(threeDiamond)) {
				System.out.println("Computer " + (i+1) + " has 3 of diamonds");
        		this.turn = i + 1 + clients.size();
        	}
			computer.setHand(hand);
		}
		System.out.println("Starting Turn is " + this.turn);
		//send turn to all clients
        sendToAll("TURN", this.turn);  
        
	}
	
	//updates turn order, cycles back if necessary
	public void nextTurn() {
		turn += 1;
		if (turn > (clients.size() + computerPlayers.size())) {
			turn = 1;
		}
		sendToAll("TURN", this.turn);
	}
	
	//sends some message and data to all clients
	public void sendToAll(String message, Object data) {
		for (HandlePlayerClient client : this.clients) {
			client.sendToClient(message, data);
		}
		//if game over, exit all computer players
		if (message == "GAME_OVER") {
			for (ComputerPlayer computer : computerPlayers) {
				computer.exit();
			}
		}
	}
	
	//broadcasts a play from a certain player to all other players
	public void broadcastPlay(int clientNo, PlayableHand playedCards) {
		for (HandlePlayerClient client : this.clients) {
			//if this thread is serving a different client and has not terminated
			if (client.getClientNum() != clientNo && !client.exited()) {
				client.sendToClient("OTHER_PLAYED", clientNo, playedCards);
			}
		}
		
		//update last played for all computer players
		for (ComputerPlayer computer : computerPlayers) {
			computer.otherPlayed(playedCards, clientNo);
		}
		
	}
	
	public void updateResults(int winner) {
		//connect to database
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:javabook.db")) {
			//update wins and losses for all players
			for (HandlePlayerClient client : clients) {
				String username = client.getUsername();
				String query;
				if (client.getClientNum() == winner) {
					//update wins if client is winner
					query = "UPDATE User SET wins = wins + 1 WHERE username = ?";
				}
				else {
					//otherwise, update losses
					query = "UPDATE User SET losses = losses + 1 WHERE username = ?";
				}
				PreparedStatement statement = connection.prepareStatement(query);
				statement.setString(1, username);
				statement.executeUpdate();
			}
			//add game to database
			String query = "INSERT INTO Game (numplayers, players, winner, datetime) VALUES (?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, clients.size());

			//comma separated players
			String players = "";
			for (HandlePlayerClient client : clients) {
				players += client.getUsername() + ",";
			}
			statement.setString(2, players);
			
			//check if computer is winner or player
			if (winner == 0) statement.setString(3, "Computer");
			else statement.setString(3, clients.get(winner - 1).getUsername());
			
			//add date
			statement.setString(4, new Date().toString());
			statement.executeUpdate();

			//close connection
			connection.close();
			System.out.println(statement.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	  
	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.setVisible(true);
	}
}


