import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ComputerPlayer implements Runnable {

		//fields
		private CardList hand = null;
		
		private PlayableHand lastPlayedHand = null;
		private int lastPlayedBy;
		
		private int id;
		private boolean exit = false;
		
		GameServer server;
		
		//constructor
		public ComputerPlayer(GameServer server, int id) {
			this.server = server;
			this.id = id;
		}

		//returns a playable hand, or null if no playable hands
		public PlayableHand play(PlayableHand toBeat) {
			ArrayList<PlayableHand> hands = PlayableHand.generatePlayableHand(hand, toBeat);
			if (hands.size() == 0) {
				return null;
			}
			else {
				CardList toRemove = (CardList) hands.get(0);
				hand.removeCards(toRemove);
				return hands.get(0);
			}
		}

		//sets hand and resets computer player status
		public void setHand(CardList hand) {
			System.out.println("Computer" + id + "reset");
			this.exit = false;
			this.hand = hand;
			lastPlayedHand = null;
			lastPlayedBy = 0;
		}

		//indicates that this thread should be on idle
		public void exit() {
			exit = true;
		}
		
		public int getID() {
			return id;
		}

		public void setID(int id) {
			this.id = id;
		}
		
		//updates last played hand and last played by 
		public void otherPlayed(PlayableHand otherHand, int otherID) {
			lastPlayedHand = otherHand;
			lastPlayedBy = otherID;
		}

	    public synchronized void startTurn() {
			//wait until it is this thread's turn
			while (server.getTurn() != this.id) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	   }
		
		public void run () {
			while (true) {
				synchronized (this) {
					startTurn();
					//check if this thread has been terminated
					if (exit) {
						while (true) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (!exit) break;
						}
					}
					startTurn(); // if this thread is coming out of sleep, make sure that its still their turn (new game started)
					
					//current turn
					if (hand != null) {
						//generate all playable hands
						ArrayList<PlayableHand> hands;
						//check if last played is this computer player, if so then we can play anything
						if (lastPlayedBy == this.id) {
							hands = PlayableHand.generatePlayableHand(hand, null);
						}
						else {
							hands = PlayableHand.generatePlayableHand(hand, lastPlayedHand);
						}
						//randomly pass or play
						int passOrNot = Math.random() < 0.5 ? 0 : 1;
						//if last played is this computer then cannot pass
						if (lastPlayedBy == this.id) {
							passOrNot = 0;
						}
						//if possible to play, or freebie, and is playing
						if (hands.size() != 0 || lastPlayedBy == this.id && passOrNot == 0) {
							
							//randomly select a playable hand
							int randomidx = ThreadLocalRandom.current().nextInt(0, hands.size());
							PlayableHand toPlay = hands.get(randomidx);
	
							//delay for a second 
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							System.out.println("Computer " + this.id + " played " + toPlay);

							//play this hand
							lastPlayedBy = id;
							server.broadcastPlay(this.id, toPlay);
							server.nextTurn();
							//remove cards
							CardList toRemove = (CardList) toPlay;
							hand.removeCards(toRemove);	
							//check if game is over
							if (hand.size() == 0) {
								//game over
								server.sendToAll("GAME_OVER", id);
								server.updateResults(0);
							}
						} //no playable hands or randomly pass
						else {
							server.nextTurn();
						}
					}
				}
			}
		}
	}