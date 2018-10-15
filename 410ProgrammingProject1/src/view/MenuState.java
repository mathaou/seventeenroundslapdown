package view;

import java.awt.Font;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

public class MenuState extends BasicGameState{
	
	public static Music messenger;
	public Image cover;
	
	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		messenger = new Music("/res/music/messenger.ogg");
		cover = new Image("/images/cover.png/");
	}
	
	public static void playMusic() {
		messenger.loop(1.0f, .5f);;
	}
	
	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int d) throws SlickException {
		
		if(gc.getInput().isKeyDown(Keyboard.KEY_ESCAPE)) {
			GameState.getP1().disconnect();
			gc.exit();
		}
		
		if(gc.getInput().isKeyDown(Keyboard.KEY_SPACE)) {
			messenger.stop();
			sbg.enterState(control.StateBasedRunner.GAME);
			GameState.playSong();
		}
		
		if(gc.getInput().isKeyPressed(Input.KEY_R)) {
			sbg.enterState(control.StateBasedRunner.RULE);
		}
		
	}
	
	public void drawCentered(TrueTypeFont ttfH, String s, GameContainer c, Graphics g, int offsetX, int offsetY) {
		int textWidth = ttfH.getWidth(s);
		g.drawString(s, c.getWidth()/2f - textWidth/2f + offsetX, 
                c.getHeight()/2f - ttfH.getLineHeight()/2f + offsetY);
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		g.drawImage(cover.getScaledCopy(gc.getWidth(), gc.getHeight()), 0, 0);
		
		g.setBackground(Color.black);
		g.setColor(Color.white);
		
		g.setFont(new TrueTypeFont(new Font("Arial", Font.BOLD, 25), true));
		g.drawString("Press [ S p a c e ] to Fight For Your Fate", 150 , gc.getHeight()/2 + 30);
		
		g.drawString("Press [ R ] for rules...", gc.getWidth() - 400, 50);
		g.drawString("[ E S C ] to quit...", gc.getWidth() - 400, 100);
	}

	@Override
	public int getID() {
		return control.StateBasedRunner.MENU;
	}

}
