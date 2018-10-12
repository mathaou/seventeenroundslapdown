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

	public static Sound cardFwip, slapDown;
	
	int offsetY, offsetX, cardWidth, cardHeight, timer = 0;
	
	int mouseX, mouseY;
	
	public static int selectedCard;
	
	public static Player player;
	
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
		
		offsetY = gc.getScreenHeight() - 323;
		offsetX = gc.getScreenWidth() - 222;
		cardWidth = offsetX/17;
		cardHeight = 323;
		
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
		
		selectedCard = 0;
		
		this.mouseX = 0;
		this.mouseY = 0;
		
		inBounds = false;
		
		gc.setSoundVolume(40f);
		
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
			player = new Player(0, InetAddress.getByName(ip), PORT_NUM);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		wolf = new Music("res/music/wolf.ogg");
		cardFwip = new Sound("res/music/fwip.wav");
		slapDown = new Sound("res/music/slapdown.ogg");
		
		MenuState.playMusic();
		
		oCards = new HashMap<Integer, Image>();
		cardFaces = new ArrayList<Image>();
		
		crownIcon = new Image("/images/Icon_crown.png");
		
		loadBack();
		
		gc.setFullscreen(true);
		
	}


	@Override
	public void update(GameContainer gc, StateBasedGame arg1, int d) throws SlickException {
		mouseX = Mouse.getX();
		mouseY = Mouse.getY();
		
		if(gc.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON) && mouseY < cardHeight + 50 && player.getCurrentPlayer() == player.getIndex()) {
			if(inBounds) {
				if(selectedCard > -1 && player.getHand().size() > 0) {
					try {
						player.playCard(selectedCard);
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
		
		if(player.getCurrentPlayer() == player.getIndex()) {
			inBounds = true;
		} else {
			inBounds = false;
		}
		
		if(gameEnd) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			arg1.enterState(control.StateBasedRunner.RESULT);
			player.setRound(1);
			wolf.pause();
			gameEnd = false;
		}
		
		if(gc.getInput().isKeyDown(Keyboard.KEY_ESCAPE)) {
			gc.exit();
		}
		
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame arg1, Graphics g) throws SlickException {
		g.setBackground(Color.black);
		renderImages(gc,g);
	}
	
	public void renderImages(GameContainer gc, Graphics g){
		background.draw(0,0,2.5f);
		for(int i = 0; i < player.getHand().size(); i++) {
			try {
				drawFace(i, oCards.get(player.formatCardID(player.getHand().get(i).getRank(), player.getHand().get(i).getSuit())));
				if((mouseX > (i * cardWidth) + (cardWidth/2) && mouseX < ((i + 1) * cardWidth) + (cardWidth/2)) && (mouseY > (0) && (mouseY < (cardHeight + 50))))
					selectedCard = i;
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		
		g.setColor(Color.white);
		g.setFont(ttfL);
		g.drawString("PLAYER "+(player.getIndex() + 1), 50, 50);
		
		if(player.getRound() == 1 && player.getHand().size() == 17) {
			g.drawString("Round: "+ (player.getRound()) + "", gc.getWidth()-200, 50f);
		} else { 
			g.drawString("Round: "+ (player.getRound() + 1) + "", gc.getWidth()-200, 50f);
		}
		
		g.setFont(ttfH);
		
		for(int i = 0; i < player.getPs().length; i++) {
			g.drawString( "P"+ (i + 1), (gc.getWidth()/5) * (i + 1) + (cardWidth/2), gc.getHeight() - 600);
			g.drawString("Wins: "+player.getPs()[i], (gc.getWidth()/5) * (i + 1) + (cardWidth/2), gc.getHeight() - 550);
			g.drawString("Score: "+player.getPoints()[i], (gc.getWidth()/5) * (i + 1) + (cardWidth/2), gc.getHeight() - 500);
		}
		
		crownIcon.draw((gc.getWidth()/5) * (player.getWin() + 1) + (cardWidth/2), 100, .4f, Color.yellow);
		
		for(int i = 0; i < player.getPlayedCards().length; i++) {
			if(player.playedCards[i] > -1) {
				back.draw((gc.getWidth()/5) * (i + 1) + (cardWidth/2), 150);
				oCards.get(player.getPlayedCards()[i]).draw((gc.getWidth()/5) * (i + 1) + (cardWidth/2), 150);
			} else {
				if(i == player.getCurrentPlayer()) {
					g.setColor(Color.orange);
				} else {
					g.setColor(Color.black);
				}
				g.fillRoundRect((gc.getWidth()/5) * (i + 1) + (cardWidth/2), 150, 222, cardHeight, 2);
			}
		}
	}

	public void drawFace(int index, Image i) {
		float up = (float) ((1f / (
				Math.abs(mouseX - (index * cardWidth)) * .04f + 1f
				)) * 75);
			if(selectedCard == index) {
				back.draw((index * cardWidth) + (cardWidth/2), offsetY - up, new Color(255,255,255 - (20 * up))); 
				i.draw((index * cardWidth) + (cardWidth/2), offsetY - up);
			} else {
				back.draw((index * cardWidth) + (cardWidth/2), offsetY - up); 
				i.draw((index * cardWidth) + (cardWidth/2), offsetY - up);
			}
	}
	
	public void loadBack() throws SlickException {
		this.back = new Image("/images/back.png/");
		this.background = new Image("/images/background.jpg/");
		for(Card c: player.getHand()) {
			cardFaces.add(new Image(player.buildFileName(c.getRank(), c.getSuit())));
		}
		for(int x = 2; x <=14; x++) {
			for(int y = 1; y <= 4; y++) {
				oCards.put(player.formatCardID(x, y), new Image(player.buildFileName(x, y)));
			}
		}
	}
	
	public static void playSong() {
		wolf.loop(1.0f, .5f);
	}
	
	@Override
	public int getID() {
		return control.StateBasedRunner.GAME;
	}
	
	public static Player getP1() {
		return player;
	}

}
