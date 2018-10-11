package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.LWJGLException;
import org.newdawn.slick.SlickException;

import view.GameState;
import view.ResultState;

public class Player{
	
	public ArrayList<Card> hand;
	public int[] playedCards;
	
	public int top;
	
	private int playerIndex;
	
	private Socket socket;
	
	private Scanner scan;
	
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	
	private final int SERVER_PORT;
	
	private String displayBytes = "", temp;
	
	Thread connectThread, recieveThread;
	
	ExecutorService es;
	
	boolean gameStart = true, roundTurn = false;
	
	int round = 1;
	int currentPlayer = 0;
	int win = 0;
	int[] ps = new int[3];
	int[] pp = new int[3];
	
	int voteNew = 0;
	
	public int getVote() {
		return voteNew;
	}
	
	public int[] getPs() {
		return ps;
	}

	public void setPs(int[] ps) {
		this.ps = ps;
	}

	public Player(int playerIndex, InetAddress serverAddress, int serverPort) throws Exception{
		
		this.playerIndex = playerIndex;
		this.hand = new ArrayList<Card>();
		this.playedCards = new int[] {-1,-1,-1};
		
		this.top = 17;
		
		scan = new Scanner(System.in);
		
		this.SERVER_PORT = serverPort;
		
		connect(serverAddress, serverPort);
		
		es = Executors.newFixedThreadPool(2);
		
		this.recieveThread = new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						displayBytes += (char) inputStream.readByte();
						//System.out.println(displayBytes);
						if(displayBytes.endsWith("}")) {
							filterInput(displayBytes);
						}
					} catch (IOException e) {
					} catch (SlickException e) {
						e.printStackTrace();
					} catch (LWJGLException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		recieveThread.start();
		
	}
	
	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public void filterInput(String input) throws InterruptedException, SlickException, LWJGLException {
		String s = "";
		try {
			JSONObject sHand = new JSONObject(input);
			s = sHand.getString("msg_type");
			switch(s) {
			case "player_score":
				ps[sHand.getInt("player_index")] = sHand.getInt("wins");
				pp[sHand.getInt("player_index")] = sHand.getInt("points");
				break;
			case "client_move":
					playedCards[sHand.getInt("player_index")] = sHand.getInt("card");
					if(sHand.getInt("next_turn") != currentPlayer) {
						currentPlayer = sHand.getInt("next_turn");
					}
					if(sHand.getInt("result_type") > -1)
						setWin(sHand.getInt("result_type"));
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
					
					break;
			case "game_end":
				JSONArray winner = sHand.getJSONArray("scoreboard");
				ResultState.setWinningPlayer(winner.getJSONObject(0).getInt("id"));
				ResultState.timer = 15;
				GameState.gameEnd = true;
				win = 0;
				round = 1;
				break;
			case "client_info":
				playerIndex = sHand.getInt("player_id");
				JSONArray pHand = sHand.getJSONArray("cards");
				for(int i = 0; i <sHand.getJSONArray("cards").length(); i++) {
					if(GameState.gameEnd || this.hand.size() == sHand.getJSONArray("cards").length()) {
						this.hand.set(i, new Card(sHand.getJSONArray("cards").getInt(i) >> 4, sHand.getJSONArray("cards").getInt(i) & 0x0F));
					} else {
						addCard(sHand.getJSONArray("cards").getInt(i) >> 4, sHand.getJSONArray("cards").getInt(i) & 0x0F);
					}
				}
				break;
			case "game_state":
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
					
				}
				
				break;
			case "new_game_votes":
				int temp = 0;
				for(int i = 0; i < sHand.getJSONArray("vote_state").length(); i++) {
					if(sHand.getJSONArray("vote_state").getBoolean(i)) {
						temp++;
					}
				}
				this.voteNew = temp;
				System.out.println("VOTE: "+voteNew);
				break;
			}
			displayBytes = "";
		} catch (JSONException e) {
			
		}
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

	public void playCard(int selectedCard) throws IOException, ClassNotFoundException, InterruptedException {
		/*outputStream.write(formatCardAsJSONObject(GameState.selectedCard).getBytes("UTF-8"));
		outputStream.flush();*/
		es.execute(new Thread() {
			@Override
			public void run() {
				try {
					outputStream.write(formatCardAsJSONObject(GameState.selectedCard).getBytes("UTF-8"));
					outputStream.flush();
					GameState.cardFwip.play((float) Math.random()*.2f + .8f,.05f);
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
			inputStream.close();
			outputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		String fileName = buildFileName(rank, suit);
		if(this.hand.size() != top) {
			this.hand.add(new Card(rank, suit));
		}
	}
	
//	public void removeCard(Card cardToRemove) {
//		if(this.hand.contains(cardToRemove)) {
//			this.hand.remove(cardToRemove);
//		}
//	}
	
	public ArrayList<Card> getHand(){
		return this.hand;
	}
	
	
	public String formatCardAsJSONObject(int selectedCard) {
		return "{\n\t\"msg_type\": \"play_card\",\n\t\"card_index\": "+selectedCard+",\n\t\"card_id\": "+formatCardID(hand.get(selectedCard).getRank(), hand.get(selectedCard).getSuit())+"\n}\n";
		//return String.format("{\n\t\"msgtype\": \"play_card\",\n\t\"card_index\": %d,\n\t\"card_id\": %d\n}", selectedCard, formatCardID(hand.get(selectedCard).getRank(), hand.get(selectedCard).getSuit()));
	}
	
	
	//generates binary to send
	public int formatCardID(int rank, int suit) {
		int num = 0;
		
		String binary = intToBinary(rank, 4) + "" + intToBinary(suit, 4);
		
		num = Integer.parseInt(binary, 2);
		
		return num;
	}
	
	//converter
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
	
	public int[] getPlayedCards(){
		return this.playedCards;
	}
	
	public int getIndex() {
		return this.playerIndex;
	}
	
}
