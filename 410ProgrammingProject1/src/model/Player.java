package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.LWJGLException;
import org.newdawn.slick.SlickException;

import view.GameState;
import view.ResultState;

/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

//client and player
public class Player{
	
	//hand
	public ArrayList<Card> hand;
	//cards played as they come in
	public int[] playedCards;
	
	public int top, playerIndex, round = 1, currentPlayer = 0, win = 0, voteNew;
	
	private Socket socket;
	
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	
	private final int SERVER_PORT;
	
	private String displayBytes = "";
	
	ExecutorService es;
	
	boolean gameStart = true, roundTurn = false;
	
	int[] ps = new int[3];
	int[] pp = new int[3];
	
	public Player(int playerIndex, InetAddress serverAddress, int serverPort) throws Exception{
		
		this.playerIndex = playerIndex;
		this.hand = new ArrayList<Card>();
		this.playedCards = new int[] {-1,-1,-1};
		
		this.top = 17;
		
		this.SERVER_PORT = serverPort;
		
		connect(serverAddress, serverPort);
		
		es = Executors.newFixedThreadPool(3);
		
		//receive thread
		es.execute(new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						displayBytes += (char) inputStream.readByte();
						if(displayBytes.endsWith("}") && !es.isShutdown()) {
							filterInput(displayBytes);
						}
					} catch (IOException e) {
						//error message upon connection interruption
						JOptionPane error = new JOptionPane("CONNECTION INTERRUPT", JOptionPane.ERROR_MESSAGE);
						JDialog dialog = error.createDialog("Failure");
						dialog.setAlwaysOnTop(true);
						dialog.setVisible(true);
						e.printStackTrace();
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						
						System.exit(0);
					} catch (SlickException e) {
						e.printStackTrace();
					} catch (LWJGLException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						//e.printStackTrace();
					}
				}
			}
		});
		
	}

	public void filterInput(String input) throws InterruptedException, SlickException, LWJGLException {
		String s = "";
		try {
			JSONObject sHand = new JSONObject(input);
			s = sHand.getString("msg_type");
			switch(s) {
			
			/*
			 * {	
				    "msg_type": "player_score",
				    "player_index": 1, // Player 2
				    "wins": 3,
				    "points": 34
				}
			 */
			
			// Update score
			
			case "player_score":
				ps[sHand.getInt("player_index")] = sHand.getInt("wins");
				pp[sHand.getInt("player_index")] = sHand.getInt("points");
				break;
				
			/*
			 * {
				    "msg_type": "client_move",
				    "player_index": 1, // Player 2 plays a card
				    "card": 132, // 8 of Spades
				    "round": 3, // Round number that the play is for
				    "next_round": 3, // Round after play
				    "next_turn": 2, // Index of next player to go
				    // Result of play
				    // -1 if no action, otherwise player index of round winner
				    "result_type": -1
				}
			 */
				
			// each card played	
				
			case "client_move":
					GameState.slapdown = false;	
					//update played cards
					playedCards[sHand.getInt("player_index")] = sHand.getInt("card");
					//update currentPlayer
					if(sHand.getInt("next_turn") != currentPlayer) {
						currentPlayer = sHand.getInt("next_turn");
					}
					
					//get winner
					if(sHand.getInt("result_type") > -1)
						setWin(sHand.getInt("result_type"));
					
					//complicated ish to get start of new turn
					if(sHand.getInt("round") == sHand.getInt("next_round") && round != 16) {
						roundTurn = true;
					} else {
						if(roundTurn && sHand.getInt("next_round") > sHand.getInt("round")) {
							Thread.sleep(1000);
							for(int i = 0; i < playedCards.length; i++) {
								playedCards[i] = -1;
							}
							setRound(sHand.getInt("round"));
							roundTurn = false;
						}
					}
					
					//slapdown bonus trigger
					if(sHand.getBoolean("slapdown")) {
						GameState.slapdown = true;
						GameState.slapDown.play(1.0f, 2.2f);
					}
					break;
					
			/*
			 * {
				    "msg_type": "game_end",
				    // # of players tied for first
				    "winner_count": 1,
				    // Scoreboard sorted in descending order of wins, then points
				    "scoreboard": [
				        {
				            "id": 2,
				            "wins": 7,
				            "points": 85
				        },
				        {
				            "id": 1,
				            "wins": 7,
				            "points": 78
				        },
				        {
				            "id": 3,
				            "wins": 3,
				            "points": 30
				        }
				    ],
				    // # of ms until new game
				    "delay_ms": 15000
				}
			 */
					
			case "game_end":
				JSONArray winner = sHand.getJSONArray("scoreboard");
				
				//get score board and reset some things for new game
				if(winner.getJSONObject(0).getInt("wins") == winner.getJSONObject(1).getInt("wins") && winner.getJSONObject(0).getInt("points") == winner.getJSONObject(1).getInt("points")) {
					ResultState.setWinningPlayer(winner.getJSONObject(0).getInt("id") + winner.getJSONObject(1).getInt("id"));
				} else {
					ResultState.setWinningPlayer(winner.getJSONObject(0).getInt("id"));
				}
				ResultState.timer = 15;
				GameState.gameEnd = true;
				win = 0;
				round = 1;
				break;
				
			/*
			 * {
				    "msg_type": "client_hand",
				    "player_id": 0,
				    "cards": [ ... ]
				}
			 */
				
			case "client_info":
				//get player index from server
				playerIndex = sHand.getInt("player_id");
				
				JSONArray pHand = sHand.getJSONArray("cards");
				
				//get hand and account for if the collection exists or not otherwise add each card as it comes
				for(int i = 0; i <pHand.length(); i++) {
					if(GameState.gameEnd || this.hand.size() == pHand.length()) {
						this.hand.set(i, new Card(pHand.getInt(i) >> 4, pHand.getInt(i) & 0x0F));
					} else {
						addCard(pHand.getInt(i) >> 4, pHand.getInt(i) & 0x0F);
					}
				}
				break;
				
			/*
			 * {
				    "msg_type": "game_state",
				    "round": 12, // 1-based
				    // playing = game in progress
				    // ended = game has ended
				    "game_state": "playing",
				    "players": {
				        "0": {
				            "hand_size": 12,
				            "points": 3
				        },
				        "1": {
				            "hand_size": 12,
				            "points": 8
				        }
				    },
				    "turn": 0,
				    // Current cards on the table. 0 = no card for player
				    // Indices correspond to player indices
				    "table": [0, 0, 0]
				}
			 */
				
			case "game_state":
				
				//start of game
				
				GameState.slapdown = false;
				voteNew = 0;
				round = 1;
				currentPlayer = 0;
				try {
					for(int i = 0; i <sHand.getJSONArray("cards").length(); i++) {
						if(GameState.gameEnd || this.hand.size() == sHand.getJSONArray("cards").length()) {
							this.hand.set(i, new Card(sHand.getJSONArray("cards").getInt(i) >> 4, sHand.getJSONArray("cards").getInt(i) & 0x0F));
						} else {
							addCard(sHand.getJSONArray("cards").getInt(i) >> 4, sHand.getJSONArray("cards").getInt(i) & 0x0F);
						}
					}
					win = 0;
					GameState.gameEnd = false;
					GameState.inBounds = false;
					displayBytes = "";
				} catch (JSONException e) {
					//e.printStackTrace();
				}
				break;
				
			/*
			 * {
				    "msg_type": "new_game_votes",
				    "vote_state": [true, false, true] // P1 and P3 have voted; P2 has not
				}
			 */
				
			case "new_game_votes":
				
				//tally votes
				
				int temp = 0;
				for(int i = 0; i < sHand.getJSONArray("vote_state").length(); i++) {
					temp = temp + (sHand.getJSONArray("vote_state").getBoolean(i) ? 1 : 0);
				}
				this.voteNew = temp;
				break;
				
			/*
			 * {
				    "msg_type": "client_reject",
				    "reject_reason": "server_full"
				}	
			 */
				
			case "client_reject":
				
				//client reject
				
				System.exit(0);
				break;
			}
			displayBytes = "";
		} catch (JSONException e) {
			//e.printStackTrace();
		}
	}

	/*
	 *  | Play card method |
	 */
	
	public void playCard(int selectedCard) throws IOException, ClassNotFoundException, InterruptedException {
		es.execute(new Thread() {
			@Override
			public void run() {
				try {
					outputStream.write(formatCardAsJSONObject(GameState.selectedCard).getBytes("UTF-8"));
					outputStream.flush();
					GameState.cardFwip.play((float) Math.random()*.2f + .8f,.0005f);
					hand.remove(hand.size()-1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void voteYes() {
		es.execute(new Thread() {
			@Override
			public void run() {
				try {
					outputStream.write("{\n\t\"msg_type\": \"vote_new_game\"\n}".getBytes("UTF-8"));
					outputStream.flush();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public boolean connect(InetAddress server, int port) {
		try {
            this.socket = new Socket(server, port);
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.inputStream = new DataInputStream(socket.getInputStream());
        } catch (UnknownHostException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        return true;
	}
	
	public void disconnect() {
		try {
			es.shutdownNow();
			socket.close();
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String formatCardAsJSONObject(int selectedCard) {
		return "{\n\t\"msg_type\": \"play_card\",\n\t\"card_index\": "+selectedCard+",\n\t\"card_id\": "+formatCardID(hand.get(selectedCard).getRank(), hand.get(selectedCard).getSuit())+"\n}\n";
	}
	
	public int formatCardID(int rank, int suit) {
		int num = 0;
		String binary = intToBinary(rank, 4) + "" + intToBinary(suit, 4);
		num = Integer.parseInt(binary, 2);
		
		return num;
	}
	
	public static String intToBinary (int n, int numOfBits) {
		   String binary = "";
		   for(int i = 0; i < numOfBits; ++i, n/=2) {
		      switch (n % 2) {
		         case 0:
		            binary = "0" + binary;
		            break;
		         case 1:
		            binary = "1" + binary;
		            break;
		      }
		   }
		   return binary;
	}
	
	public String buildFileName(int rank, int suit) {
		String output = "";
		switch(rank) {
			case 2: 
				output += "2";                                                    
				break;
			case 3:
				output += "3";
				break;
			case 4:
				output += "4";
				break;
			case 5:
				output += "5";
				break;
			case 6:
				output += "6";
				break;
			case 7:
				output += "7";
				break;
			case 8:
				output += "8";
				break;
			case 9:
				output += "9";
				break;
			case 10:
				output += "10";
				break;
			case 11:
				output += "jack";
				break;
			case 12:
				output += "queen";
				break;
			case 13:
				output += "king";
				break;
			case 14:
				output += "ace";
				break;
		}
		
		output+="_of_";
		
		switch(suit) {
			case 1:
				output += "clubs.png";
				break;
			case 2:
				output += "diamonds.png";
				break;
			case 3:
				output += "hearts.png";
				break;
			case 4: 
				output += "spades.png";
				break;
		}
		
		return output;
		
	}
	
	public void addCard(int rank, int suit) throws SlickException, LWJGLException {
		if(this.hand.size() != top) {
			this.hand.add(new Card(rank, suit));
		}
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	
	public ArrayList<Card> getHand(){
		return this.hand;
	}
	
	public int[] getPlayedCards(){
		return this.playedCards;
	}
	
	public int getIndex() {
		return this.playerIndex;
	}
	
	public int getVote() {
		return voteNew;
	}
	
	public int[] getPs() {
		return ps;
	}

	public void setPs(int[] ps) {
		this.ps = ps;
	}
	
	
	public int[] getPoints() {
		return pp;
	}

	public boolean isRoundTurn() {
		return roundTurn;
	}

	public void setRoundTurn(boolean roundTurn) {
		this.roundTurn = roundTurn;
	}

	public int getWin() {
		return win;
	}

	public void setWin(int win) {
		this.win = win;
	}

	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(int currentPlayer) {
		this.currentPlayer = currentPlayer;
	}
	
	public int[] getPp() {
		return pp;
	}

	public void setPp(int[] pp) {
		this.pp = pp;
	}
	
	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}
	
}
