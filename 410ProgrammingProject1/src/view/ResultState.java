package view;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class ResultState extends BasicGameState{

	static int winningPlayer = -1;
	
	public Font hind;
	
	public TrueTypeFont ttfH;
	
	int countDown = 0;
	public static int timer;
	
	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		try {
			hind = Font.createFont(Font.TRUETYPE_FONT, new File("res/font/limelight/Limelight.ttf/"));
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		hind = hind.deriveFont(Font.BOLD, 40);
		ttfH = new TrueTypeFont(hind, true);
	}

	@Override
	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2) throws SlickException {
		arg2.setFont(ttfH);
		arg2.drawString("P"+(winningPlayer + 1) +" WINS, " + timer + " seconds left...", arg0.getWidth()/2, arg0.getHeight()/2);
		
		for(int i = 0; i < GameState.getP1().getPs().length; i++) {
			arg2.drawString("P"+ (i + 1), arg0.getWidth()/2 + (300 * i), arg0.getHeight()/2 + 40);
			arg2.drawString("Wins: "+GameState.getP1().getPs()[i], arg0.getWidth()/2 + (150 * i), arg0.getHeight()/2 + 80);
			arg2.drawString("Points: "+GameState.getP1().getPp()[i], arg0.getWidth()/2 + (150 * i), arg0.getHeight()/2 + 120);
		}
		
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
		countDown += arg2;
		if(countDown > 1200) {
			countDown = 0;
			if(timer > 0) {
				timer--;
			}
		}
		if(timer <= 0 && GameState.getP1().getRound() == 1) {
			for(int i = 0; i < GameState.getP1().getPs().length; i++) {
				GameState.getP1().getPs()[i] = 0;
				GameState.getP1().getPlayedCards()[i] = -1;
				GameState.getP1().getPp()[i] = 0;
			}
			
			GameState.gameEnd = false;
			arg1.enterState(control.StateBasedRunner.GAME);
		}
	}

	@Override
	public int getID() {
		return control.StateBasedRunner.RESULT;
	}
	
	public static void setWinningPlayer(int i) {
		winningPlayer = i;
	}

}
