package view;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeOutTransition;

public class MenuState extends BasicGameState{
	
	public int timer, loopControl, fontIndex, fontCounter, width, height;
	
	public TrueTypeFont[] fontList;
	
	public String chars;
	
	String fonts[];
	
	Music messenger;
	
	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		this.timer = 0;
		this.loopControl = 0;
		this.fontIndex = 0;
		this.fontCounter = 0;
		
		this.width = 0;
		this.height = 0;
		
		this.chars = "Seventeen Round Slap Down";
		
		this.fonts
		        = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();		
		
		//this.messenger = new Music("/res/912.wav");
		
		//messenger.loop(1.0f, 1.0f);
		
		loopControl = fonts.length;
		
		if(loopControl % 5 == 0) {
			fontIndex += 5;
		} else if(loopControl % 4 == 0) {
			fontIndex += 4;
		} else if(loopControl % 3 == 0) {
			fontIndex += 3;
		} else if(loopControl % 2 == 0) {
			fontIndex += 2;
		} else {
			fontIndex++;
		}
		
		this.fontList = new TrueTypeFont[fontIndex];
		
		for(int i = 0; i < fontList.length; i++) {
			fontList[i] = new TrueTypeFont(new Font(fonts[i], Font.PLAIN, 24), true);
		}
		
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int d) throws SlickException {
		
		timer+=d;
		
		if(timer >= 2000) {
			for(int i = 0; i < fontList.length; i ++) {
				fontList[i] = new TrueTypeFont(new Font(fonts[fontCounter + i], Font.PLAIN, 24), true);
			}
			fontCounter+=fontIndex;
			timer = 0;
		}
		
		if(fontCounter > fonts.length-30) {
			fontCounter = 0;
		}
		
		if(gc.getInput().isKeyDown(Keyboard.KEY_ESCAPE)) {
			gc.exit();
		}
		if(gc.getInput().isKeyDown(Keyboard.KEY_SPACE)) {
			//messenger.stop();
			sbg.enterState(control.StateBasedRunner.GAME);
			//GameState.playSong();
		}
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		g.setBackground(Color.black);
		g.setColor(Color.pink);
		drawCentered(chars, g, gc.getWidth()/2, gc.getHeight()/2);
		g.drawString("Press [S p a c e] to Fight For Your Fate", 100 , gc.getHeight() - 100);
	}

	@Override
	public int getID() {
		return control.StateBasedRunner.MENU;
	}
	
	public void drawCentered(String chars, Graphics g, int x, int y){
		for(int i = 0; i < fontList.length; i ++) {
			width = fontList[i].getWidth(chars);
			height = fontList[i].getHeight(chars);
			fontList[i].drawString(x, y + (i * 50), chars);
			
		}
    }

}
