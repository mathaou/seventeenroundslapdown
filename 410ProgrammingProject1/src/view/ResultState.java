package view;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class ResultState extends BasicGameState{

	public static int winningPlayer = -1, timer;
	
	public Font hind;
	
	public TrueTypeFont ttfH;
	
	int countDown = 0;
	
	public boolean vote = false;
	
	public String[] arr = new String[] {""};
	
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
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
		countDown += arg2;
		if(countDown > 1200) {
			countDown = 0;
			if(timer > 0) {
				timer--;
			}
		}
		
		if(arg0.getInput().isKeyPressed(Keyboard.KEY_V)) {
			if(!vote) {
				GameState.getP1().voteYes();
				vote = true;
			}
		}
		
		if(arg0.getInput().isKeyPressed(Keyboard.KEY_ESCAPE)) {
			GameState.getP1().disconnect();
			System.exit(0);
		}
		
		if((timer <= 0 && GameState.gameEnd == false) || (GameState.getP1().getVote() == 3 && GameState.gameEnd == false)) {
			for(int i = 0; i < GameState.getP1().getPs().length; i++) {
				GameState.getP1().getPs()[i] = 0;
				GameState.getP1().getPlayedCards()[i] = -1;
				GameState.getP1().getPp()[i] = 0;
			}
			vote = false;
			GameState.wolf.play();
			arg1.enterState(control.StateBasedRunner.GAME);
		}
	}
	
	@Override
	public void render(GameContainer c, StateBasedGame sbg, Graphics g) throws SlickException {
		g.setFont(ttfH);

		drawCentered("P"+(winningPlayer + 1) +" WINS, " + timer + " seconds left...", c, g, 0, 0);
		
		for(int i = 0; i < GameState.getP1().getPs().length; i++) {
			drawCentered("P"+ (i + 1), c, g, (i * 300) - 300, 200);
			drawCentered("Wins: "+GameState.getP1().getPs()[i], c, g, (300 * i) - 300, 300);
			drawCentered("Points: "+GameState.getP1().getPp()[i], c, g, (300 * i) - 300, 400);
		}
		
		drawCentered("Press [ V ] to vote to start new game...", c, g, -300, -300);
		g.drawString("Votes: "+GameState.getP1().getVote(), 50, c.getHeight()- 50);
	}

	public void drawCentered(String s, GameContainer c, Graphics g, int offsetX, int offsetY) {
		int textWidth = ttfH.getWidth(s);
		g.drawString(s, c.getWidth()/2f - textWidth/2f + offsetX, 
                c.getHeight()/2f - ttfH.getLineHeight()/2f + offsetY);
	}
	
	@Override
	public int getID() {
		return control.StateBasedRunner.RESULT;
	}
	
	public static void setWinningPlayer(int i) {
		winningPlayer = i;
	}

}
