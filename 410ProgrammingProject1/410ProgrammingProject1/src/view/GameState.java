package view;

import java.io.IOException;
import java.net.InetAddress;

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
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

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
	
	private Player p1, p2, p3;
	
	private Image back, background;
	
	private boolean inBounds;
	
	public static Music wolf;
	
	@Override
	public void init(GameContainer gc, StateBasedGame arg1) throws SlickException {
		
		//wolf = new Music("/res/WOLF.wav");
		
		this.selectedCard = 0;
		
		this.mouseX = 0;
		this.mouseY = 0;
		
		this.playX = 0;
		this.playY = 0;
		
		this.inBounds = false;
		
		gc.setSoundVolume(50f);
		
		//cardFwip = new Sound("/res/cardSlide.wav");
		
		try {
			//25.57.178.138
			p1 = new Player(0, InetAddress.getByName("localhost"), PORT_NUM);
			/*p2 = new Player(1, InetAddress.getByName("127.0.0.1"), PORT_NUM);
			p3 = new Player(2, InetAddress.getByName("127.0.0.1"), PORT_NUM);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		generateHand();
		loadBack();
		
		offsetY = gc.getScreenHeight() - p1.getHand().get(0).getCard().getHeight();
		offsetX = gc.getScreenWidth() - p1.getHand().get(0).getCard().getWidth();
		cardWidth = offsetX/17;
		cardHeight = p1.getHand().get(0).getCard().getHeight();
	}


	@Override
	public void update(GameContainer gc, StateBasedGame arg1, int d) throws SlickException {
		//System.out.println(mouseY + "/"+cardHeight);
		if(gc.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON) && mouseY < cardHeight) {
			if(selectedCard > -1 && p1.getHand().size() > 0) {
				try {
					p1.playCard(selectedCard);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				p1.removeCard(p1.getHand().get(selectedCard));
			}
		}
		
		if(gc.getInput().isKeyDown(Keyboard.KEY_ESCAPE)) {
			gc.exit();
		}
		
		mouseX = Mouse.getX();
		mouseY = Mouse.getY();
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame arg1, Graphics g) throws SlickException {
		g.setBackground(Color.black);
		renderImages(gc,g);
	}
	
	public void generateHand() throws SlickException {
		for(int i = 1; i <= 4; i++) {
			for(int x = 2; x <= 14; x++) {
				p1.addCard(x, i);
			}
		}
	}
	
	public void loadBack() throws SlickException {
		this.back = new Image("/images/back.png/");
		this.background = new Image("/images/background.jpg/");
	}
	
	public void renderImages(GameContainer gc, Graphics g){
		background.draw(0,0,2.5f);
		for(int i = 0; i < p1.getHand().size(); i++) {
			/*g.setColor(Color.pink);
			g.drawString((i * cardWidth) + "", (int) (i * cardWidth), (int) (offsetY - cardHeight / 2));*/
			if((mouseX > (i * cardWidth) && mouseX < ((i + 1) * cardWidth)) && (mouseY > (0) && (mouseY < (cardHeight)))) {
				back.draw(i * cardWidth, offsetY - cardHeight / 2, new Color(230,230,230));
				p1.getHand().get(i).drawFace(i * cardWidth, offsetY - cardHeight / 2);
				selectedCard = i;
			} else {
				back.draw(i * cardWidth, offsetY);
				p1.getHand().get(i).drawFace(i * cardWidth, offsetY);
			}
			
			
			//g.drawString(p1.getHand().get(i).getCard().getResourceReference(), i*this.cardWidth+20,(int) (this.offsetY-(20*i)));
		}
	}
	
	public void drawServerData(GameContainer gc, Graphics g) {
		
	}
	
	public static void playSong() {
		wolf.loop(1.0f, .05f);
	}
	
	@Override
	public int getID() {
		return control.StateBasedRunner.GAME;
	}

}
