import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.*;
import javax.swing.border.Border;


public class Player extends JFrame{

	private static final long serialVersionUID = -5787944850608160688L;

	// IO streams
	ObjectOutputStream objectToServer = null;
    ObjectInputStream objectFromServer = null;
    DataOutputStream commandToServer = null;
    DataInputStream commandFromServer = null;
    
	//UI
	JTextArea instructions;
	JTextArea console;
	Socket socket = null;

	private String username = null;
	Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
	private BlockingQueue<Message> messageQueue;
	
	public Player() {
		super();

		this.setLayout(new BorderLayout());
		
		//label for top of frame
		JLabel label = new JLabel("Big Two Client");
        Font font = new Font("Arial", Font.BOLD, 36);
        label.setFont(font);
        label.setForeground(Color.BLUE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
		this.add(label, BorderLayout.NORTH);

		//setting up bottom frame with text area and card graphics
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 3));

		//card panels
		Card card = new Card("Spades", 13);
		Card card2 = new Card("Hearts", 13);
		CardPanel cardPanel1 = new CardPanel(card);
		CardPanel cardPanel2 = new CardPanel(card2);

		//text area for console
		console = new JTextArea();
		console.setEditable(false);
		console.setBorder(blackBorder);
		console.append("Welcome to Big Two!\n\n");		
		console.append("Please consult the help menu\nfor extra info\n\n");
		JScrollPane scroll = new JScrollPane(console);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		panel.add(cardPanel1);
		panel.add(scroll);
		panel.add(cardPanel2);

		this.add(panel, BorderLayout.CENTER);

		// Construct menu for connection 
	    JMenuBar menuBar = new JMenuBar();     
	    setJMenuBar(menuBar);
	    menuBar.add(createOptionsMenu());
		menuBar.add(createPlayerMenu());
		setSize(600, 400);
		
	}

	/****************************
	 * UI Paneling
	*****************************/
	
	public void addToTextArea(String text) {
		console.append(text);
	}

	//drop down menu for options
	public JMenu createOptionsMenu() {
	    JMenu menu = new JMenu("Options");
	    menu.add(createConnectItem());
	    menu.add(createExitItem());
	    return menu;
	}

	//drop down menu for player options
	public JMenu createPlayerMenu() {
	    JMenu menu = new JMenu("Player");
	    menu.add(createLoginItem());
		menu.add(createHelpItem());
		menu.add(createHistoryItem());
	    return menu;
	}

	private JMenuItem createHistoryItem() {
		JMenuItem history = new JMenuItem("History");
		//listener for help, displays help
	    class MenuItemListener implements ActionListener{
	         public void actionPerformed(ActionEvent event)
	         {
				//create new history window
				JFrame history = new History(username);
				history.setVisible(true);
				history.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				history.pack();
	         }
	    } 
		history.addActionListener(new MenuItemListener());
	    return history;
	}

	//menu item to display help
	private JMenuItem createHelpItem() {
		JMenuItem help = new JMenuItem("Help");
		//listener for help, displays help
	    class MenuItemListener implements ActionListener{
	         public void actionPerformed(ActionEvent event)
	         {
				//create new window
				JFrame helpFrame = new JFrame();
				helpFrame.setLayout(new BorderLayout());
				
				//label for top of frame
				JLabel label = new JLabel("Help");
		        Font font = new Font("Arial", Font.BOLD, 36);
		        label.setFont(font);
		        label.setForeground(Color.BLUE);
		        label.setHorizontalAlignment(SwingConstants.CENTER);

				helpFrame.add(label, BorderLayout.NORTH);

				//info text
				JTextArea helpText = new JTextArea();
				helpText.setBorder(blackBorder);

				helpText.setEditable(false);

				helpText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				
				String text = "Connecting to server:\n";
				text += "You must log in before connecting to the server.\nPlease sign up/log in using the drop down menu\n";
				text += "Once logged in, you can connect to the server using the \"connect\" option\n";
				text += "Once connected, a new window will open and the game board will appear.\n";
				text += "You can now play the game!\n\n";
				helpFrame.add(helpText, BorderLayout.CENTER);
				helpText.setText(text);

				//link to big two wiki
				JLabel link = new JLabel("<html><a href=\"https://en.wikipedia.org/wiki/Big_two\">Big Two Wiki</a></html>");
				
				//mouse listener to handle click events
				link.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() > 0) {
							try {
								Desktop.getDesktop().browse(new URI("https://en.wikipedia.org/wiki/Big_two"));
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (URISyntaxException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				});

				helpFrame.add(link, BorderLayout.SOUTH);

				helpFrame.setVisible(true);
				helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				helpFrame.pack();
	         }
	    } 
	    //bind listener to open item
	    ActionListener helpListener = new MenuItemListener();
	    help.addActionListener(helpListener);
	    return help;
	}

	//menu item to log in
	private JMenuItem createLoginItem() {
		JMenuItem login = new JMenuItem("Sign Up / Login");
		//listener for login, opens window for user login
	    class MenuItemListener implements ActionListener{
	         public void actionPerformed(ActionEvent event)
	         {
	        	 new UserLogin(new LoginCallback() {
					public void success(String username) {
						console.append("Logged in as " + username + "\n");
						Player.this.username = username;
					}
	        	 });
	         }
	    } 
	    //bind listener to open item
	    ActionListener loginListener = new MenuItemListener();
	    login.addActionListener(loginListener);
	    return login;
	}
	
	//callback for login
	public interface LoginCallback {
	    void success(String username);
	}

	//menu item to connect to server
	private JMenuItem createConnectItem() {
		JMenuItem connect = new JMenuItem("Connect");
		//listener for connect, connects to server
	    class MenuItemListener implements ActionListener{
	         public void actionPerformed(ActionEvent event)
	         {
				 
	     		try { //check if logged in
					if (username == null) {
						console.append("Please log in first\n");
						return;
					}
					else if (socket != null) {
						console.append("Already connected to server\n");
						return;
					}
					else {
						console.append("Connecting to server...\n");
						socket = new Socket("localhost", 9898);
	    				//create IO streams
						commandToServer = new DataOutputStream(socket.getOutputStream());
						objectToServer = new ObjectOutputStream(commandToServer);

						commandFromServer = new DataInputStream(socket.getInputStream());
						objectFromServer = new ObjectInputStream(commandFromServer);
						
						//create message queue and reader
						messageQueue = new LinkedBlockingQueue<>();
						MessageReader reader = new MessageReader(objectFromServer);
						Thread readerThread = new Thread(reader);
						readerThread.start();
						
						//open game board window
						PlayField board = new PlayField(Player.this, messageQueue, objectToServer, username);
						board.setVisible(true);
						board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

						console.append("Connected to server\n");
					}
	    		} catch (IOException e) {
	    			console.append("Error connecting to server\n");
					console.append("Is the server running or full?\n\n");
					resetSocket();
	    		}
	         }
	    } 
	    //bind listener to open item
	    ActionListener connectListener = new MenuItemListener();
	    connect.addActionListener(connectListener);
	    return connect;
	}
	
	//exit app
	private JMenuItem createExitItem() {
		JMenuItem exit = new JMenuItem("Exit");
		//listener for exit item, calls exit()
	    class MenuItemListener implements ActionListener{
	         public void actionPerformed(ActionEvent event)
	         {
	        	try { 
	        		if (socket != null) {
	        			socket.close();
	        		}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            System.exit(0); //exit app
	         }
	    }      
	    //bind listener to exit item
	    ActionListener exitListener = new MenuItemListener();
	    exit.addActionListener(exitListener);
	    return exit;
	}
	
	/****************************
	 * Networking
	*****************************/
	//thread to read messages from server
	public class MessageReader implements Runnable {

	    private ObjectInputStream inputStream;
	    
	    public MessageReader(ObjectInputStream inputStream) {
	        this.inputStream = inputStream;
	    }

	    @Override
	    public void run() {
	        try {
	            while (true) { //read messages from server
	                Message message = (Message) inputStream.readObject();
	                messageQueue.add(message);
	            }
	        } catch (IOException | ClassNotFoundException e) {
	            System.out.println("Error reading from server");
	            System.out.println("resetting socket");
	            resetSocket();
	        }
	    }
	};

	public void resetSocket() {
		socket = null;
	}
	
	public static void main(String[] args) {
		Player player = new Player();
		player.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		player.setVisible(true);
	}
}




