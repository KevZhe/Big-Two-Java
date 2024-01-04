import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


//Class that facilitates communication with client
class HandlePlayerClient implements Runnable {
	
	private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
	
	private boolean exit = false;
	private Socket socket; 
	private int clientNum;
	private String username;
	private GameServer server;
	// IO streams
	ObjectOutputStream objectToClient;
	ObjectInputStream objectFromClient;
	DataOutputStream commandToClient;
	DataInputStream commandFromClient;
	
	//constructs the thread
	public HandlePlayerClient(GameServer server, Socket socket, int clientNum) {
		this.server = server;
		this.socket = socket;
		this.clientNum = clientNum;
		//create an input stream to receive data from the client
		try {
			commandToClient = new DataOutputStream(socket.getOutputStream());
			objectToClient = new ObjectOutputStream(commandToClient);
			commandFromClient = new DataInputStream(socket.getInputStream());
			objectFromClient = new ObjectInputStream(commandFromClient);
			
			//send the client their assigned id
			Message newMessage = new Message("SETID", clientNum);
			objectToClient.writeObject(newMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//initialize reader for messages from the client
		MessageReader reader = new MessageReader(objectFromClient);
		Thread readerThread = new Thread(reader);
		readerThread.start();
	}
	
	public boolean exited() {
		return exit;
	}
	public String getUsername() {
		return this.username;
	}	

	//general function that send a message with some data to client
	public void sendToClient(String message, Object data) {
		try {
			System.out.println("Sending message " + message + " to client " + clientNum);
			Message newMessage = new Message(message, data);
			objectToClient.writeObject(newMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//general function that send a message with some data to client including who sent the data
	public void sendToClient(String message, int clientNum, Object data) {
		try {
			Message newMessage = new Message(message, clientNum, data);
			objectToClient.writeObject(newMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//getter/setter for client no
	public int getClientNum() {
		return clientNum;
	}

	public void setClientNum(int clientNum) {
		this.clientNum = clientNum;
		sendToClient("SETID", clientNum);
	}
	
	//run this thread to constantly take messages queue
	public void run() {
		try {
			// Continuously serve the client
			while (socket.isConnected()) {
				try {
					Message message = messageQueue.take();
					processMessage(message);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					break;
				}
			}
		}
		catch(Exception ex) {
		ex.printStackTrace();

		}
	}
	
	public void exit() {
		System.out.println("Client " + clientNum + " disconnected");
		exit = true;
		//remove this thread from our list
		ArrayList<HandlePlayerClient> clients = server.getClients();
		clients.remove(this);
		//free up this client number
		boolean[] taken = server.getTaken();
		taken[clientNum - 1] = false;
		server.setTaken(taken);
		server.moveTaken();
	}
	
	//process message from client
	private void processMessage(Message message) {
		String command = message.getMessageType();
		try {
			if (command.equals("PLAY")) {
				//if client plays hand, send this play back to game server to handle
				PlayableHand playedHand =  (PlayableHand) message.getData();
				server.broadcastPlay(this.clientNum, playedHand);
				server.nextTurn();
			}
			else if (command.equals("PASS")) {
				//if client passes, update turn order
				server.nextTurn();
			}
			else if (command.equals("GAME_OVER")) {
				//client is out of cards, game is over
				server.sendToAll("GAME_OVER", this.clientNum);
				server.updateResults(clientNum);
			}
			else if (command.equals("SET_USERNAME")) {
				//client is setting their username
				String username = (String) message.getData();
				this.username = username;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class MessageReader implements Runnable {

		private ObjectInputStream inputStream;
		
		public MessageReader(ObjectInputStream inputStream) {
			this.inputStream = inputStream;
		}
		//run this thread to constantly put messages into queue
		@Override
		public void run() {
			try {
				while (true) {
					Message message = (Message) inputStream.readObject();
					messageQueue.add(message);
				}
			} catch (IOException | ClassNotFoundException e) {
				server.addToTextArea("Client " + clientNum + " disconnected\n");
				exit();
			}
		}
	};
}
