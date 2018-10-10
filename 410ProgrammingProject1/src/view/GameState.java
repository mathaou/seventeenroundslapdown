package view;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import model.Card;
import model.Player;

public class GameState extends BasicGameState{

	private final static int PORT_NUM = 6789;

	Sound cardFwip;
	
	int offsetY;
	int offsetX;
	int cardWidth, cardHeight;
	
	int timer = 0;
	
	int mouseX, mouseY;
	
	int playX, playY;
	
	public static int selectedCard;
	
	public static Player p1;
	
	private Image back, background;
	
	public static boolean inBounds, gameEnd = false;
	
	public static Music wolf;
	
	public ArrayList<Image> cardFaces;
	public HashMap<Integer, Image> oCards;
	
	public Image crownIcon;
	
	public Font hind, lora;
	
	public TrueTypeFont ttfH, ttfL;
	
	@Override
	public void init(GameContainer gc, StateBasedGame arg1) throws SlickException {
		
		//wolf = new Music("res/WOLF.wav");
		
		try {
			hind = Font.createFont(Font.TRUETYPE_FONT, new File("res/font/hind/hind-bold.ttf"));
			lora = Font.createFont(Font.TRUETYPE_FONT, new File("res/font/lora/Lora-Bold.ttf"));
		} catch (FontFormatException | IOException e1) {
			e1.printStackTrace();
		}
		
		hind = hind.deriveFont(Font.BOLD, 30);
		lora = lora.deriveFont(Font.BOLD, 30);
		
		ttfH = new TrueTypeFont(hind, true);
		ttfL = new TrueTypeFont(lora, true);
		
		oCards = new HashMap<Integer, Image>();
		
		this.selectedCard = 0;
		
		this.mouseX = 0;
		this.mouseY = 0;
		
		this.playX = 0;
		this.playY = 0;
		
		this.inBounds = false;
		
		gc.setSoundVolume(50f);
		
		try {
			boolean loop = false;
			String ip = "";
			String IPADDRESS_PATTERN = 
					"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
			while(!loop) {
				ip = JOptionPane.showInputDialog("Enter your IP address...");
				if(Pattern.compile(IPADDRESS_PATTERN).matcher(ip).matches()) {
					loop = true;
				} else {
					JOptionPane.showMessageDialog(null, "Address formatted incorrectly. Try again.", "Address formatted incorrectly. Try again.", JOptionPane.ERROR_MESSAGE);
				}
			}
			p1 = new Player(0, InetAddress.getByName(ip), PORT_NUM);
			gc.setFullscreen(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		cardFaces = new ArrayList<Image>();
		
		crownIcon = new Image("/images/Icon_crown.png");
		
		loadBack();
		
		offsetY = gc.getScreenHeight() - 323;
		offsetX = gc.getScreenWidth() - 222;
		cardWidth = offsetX/17;
		cardHeight = 323;
	}


	@Override
	public void update(GameContainer gc, StateBasedGame arg1, int d) throws SlickException {
		if(gc.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON) && mouseY < cardHeight + 50 && p1.getCurrentPlayer() == p1.getIndex()) {
			if(inBounds) {
				if(selectedCard > -1 && p1.getHand().size() > 0) {
					try {
						p1.playCard(selectedCard);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if(p1.getCurrentPlayer() == p1.getIndex()) {
			inBounds = true;
		} else {
			inBounds = false;
		}
		
		if(gc.getInput().isKeyDown(Keyboard.KEY_ESCAPE)) {
			gc.exit();
		}
		
		mouseX = Mouse.getX();
		mouseY = Mouse.getY();
		
		if(gameEnd) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			arg1.enterState(control.StateBasedRunner.RESULT);
			p1.setRound(1);
			gameEnd = false;
		}
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame arg1, Graphics g) throws SlickException {
		g.setBackground(Color.black);
		renderImages(gc,g);
	}
	
	public void loadBack() throws SlickException {
		this.back = new Image("/images/back.png/");
		this.background = new Image("/images/background.jpg/");
		for(Card c: p1.getHand()) {
			cardFaces.add(new Image(p1.buildFileName(c.getRank(), c.getSuit())));
		}
		for(int x = 2; x <=14; x++) {
			for(int y = 1; y <= 4; y++) {
				oCards.put(formatCardID(x, y), new Image(buildFileName(x, y)));
			}
		}
	}
	
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
	
	public void renderImages(GameContainer gc, Graphics g){
		background.draw(0,0,2.5f);
		for(int i = 0; i < p1.getHand().size(); i++) {
			/*g.setColor(Color.pink);
			g.drawString((i * cardWidth) + "", (int) (i * cardWidth), (int) (offsetY - cardHeight / 2));*/
			try {
				if((mouseX > (i * cardWidth) && mouseX < ((i + 1) * cardWidth)) && (mouseY > (0) && (mouseY < (cardHeight + 50)))) {
					//back.draw(i * cardWidth, offsetY - cardHeight / 2, new Color(230,230,230));
					drawFace(0, i, oCards.get(formatCardID(p1.getHand().get(i).getRank(), p1.getHand().get(i).getSuit())));
					selectedCard = i;
					//System.out.println(selectedCard);
				} else {
					//back.draw(i * cardWidth, offsetY);
					drawFace(0, i, oCards.get(formatCardID(p1.getHand().get(i).getRank(), p1.getHand().get(i).getSuit())));
				}
			} catch (IndexOutOfBoundsException e) {
				
			}
			
			
			//g.drawString(p1.getHand().get(i).getCard().getResourceReference(), i*this.cardWidth+20,(int) (this.offsetY-(20*i)));
		}
		
		g.setColor(Color.white);
		
		g.setFont(ttfL);
		
		g.drawString("PLAYER "+(p1.getIndex() + 1), 50, 50);
		
		g.setFont(ttfH);
		
		if(p1.getRound() == 1 && p1.getHand().size() == 17) {
			g.drawString("Round: "+ (p1.getRound()) + "", gc.getWidth()-200, 50f);
		} else { 
			g.drawString("Round: "+ (p1.getRound() + 1) + "", gc.getWidth()-200, 50f);
		}
		
		for(int i = 0; i < p1.getPs().length; i++) {
			g.drawString( "P"+ (i + 1), (400* (i + 1)), gc.getHeight() - 600);
			g.drawString("Wins: "+p1.getPs()[i], (400* (i + 1) + 100), gc.getHeight() - 600);
			g.drawString("Score: "+p1.getPoints()[i], (400* (i + 1) + (195 / 2)), gc.getHeight() - 570);
		}
		
		crownIcon.draw(400 * (p1.getWin() + 1), 100, .4f, Color.yellow);
		
		for(int i = 0; i < p1.getPlayedCards().length; i++) {
			if(p1.playedCards[i] > -1) {
				back.draw(400 * (i + 1), 150);
				oCards.get(p1.getPlayedCards()[i]).draw(400 * (i + 1), 150);
			} else {
				g.setColor(Color.black);
				g.fillRoundRect(400 * (i + 1), 150, 222, 323, 2);
			}
		}
	}
	
	public static Player getP1() {
		return p1;
	}

	public void drawFace(int s, int index, Image i) {
		float up = (float) ((1f / (
				Math.abs(mouseX - (index * cardWidth)) * .04f + 1f
				)) * 75);
		switch(s) {
		case 0: 
			if(selectedCard == index) {
				back.draw(index * cardWidth, offsetY - up, new Color(255,255,255 - (20 * up))); i.draw(index * cardWidth, offsetY - up); break;
			} else {
				back.draw(index * cardWidth, offsetY - up); i.draw(index * cardWidth, offsetY - up); break;
			}
		}
	}
	
	public static void playSong() {
		wolf.loop(1.0f, .05f);
	}
	
	@Override
	public int getID() {
		return control.StateBasedRunner.GAME;
	}

}
