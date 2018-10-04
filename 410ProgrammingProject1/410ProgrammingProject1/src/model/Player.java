package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.newdawn.slick.SlickException;

import view.GameState;

public class Player{
	
	public ArrayList<Card> hand;
	public ArrayList<Card> playedCards;
	
	public int top;
	
	private int playerIndex;
	
	private Socket socket;
	
	private Scanner scan;
	
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	
	private final int SERVER_PORT;
	
	private String displayBytes, temp;
	
	Thread connectThread, recieveThread;
	
	ExecutorService es;
	
	public Player(int playerIndex, InetAddress serverAddress, int serverPort) throws Exception{
		
		this.playerIndex = playerIndex;
		this.hand = new ArrayList<Card>();
		this.playedCards = new ArrayList<Card>();
		
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
							if(inputStream.readUTF() != null) {
								try {
									inputStream.readUTF();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}
		};
		
		es.submit(recieveThread);
		
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
					System.out.println("Sent...");
				} catch (IOException e) {
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
	
	public void addCard(int rank, int suit) throws SlickException {
		String fileName = buildFileName(rank, suit);
		if(this.hand.size() != top) {
			this.hand.add(new Card(rank, suit, fileName));
		}
	}
	
	public void removeCard(Card cardToRemove) {
		if(this.hand.contains(cardToRemove)) {
			this.hand.remove(cardToRemove);
		}
	}
	
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
	
	public int getIndex() {
		return this.playerIndex;
	}
	
}
