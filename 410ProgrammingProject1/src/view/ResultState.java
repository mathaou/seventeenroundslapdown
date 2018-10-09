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
	
	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		try {
			hind = Font.createFont(Font.TRUETYPE_FONT, new File("res/font/limelight/Limelight.ttf/"));
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		hind = hind.deriveFont(Font.BOLD, 20);
		ttfH = new TrueTypeFont(hind, true);
	}

	@Override
	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2) throws SlickException {
		arg2.setFont(ttfH);
		if(winningPlayer > -1) {
			arg2.drawString("P"+winningPlayer +" WINS", arg0.getWidth()/2, arg0.getHeight()/2);
		}
		
		for(int i = 0; i < GameState.getP1().getPp().length; i++) {
			
		}
		
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
		if(arg0.getInput().isKeyPressed(Input.KEY_SPACE)) {
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
