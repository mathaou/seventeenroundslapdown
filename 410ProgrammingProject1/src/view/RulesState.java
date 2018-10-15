package view;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

public class RulesState extends BasicGameState{

	String[] rules = new String[27];
	
	Scanner scan;
	
	int lineNum;
	
	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		try {
			scan = new Scanner(new File("res/srsdm.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while(scan.hasNextLine()) {
			rules[lineNum] = scan.nextLine();
			lineNum++;
		}
		
		lineNum = 0;
	}

	@Override
	public void render(GameContainer gc, StateBasedGame arg1, Graphics g) throws SlickException {
		g.setFont(new TrueTypeFont(new Font("Arial", Font.BOLD, 25), true));
		g.drawString("Press [ R ] to main menu...", gc.getWidth() - 400, 50);
		
		for(int i = 0; i < rules.length; i++) {
			if(rules[i].indexOf(":") > -1) {
				g.setColor(Color.red);
			} else {
				g.setColor(Color.white);
			}
			g.drawString(rules[i], 0, i * 30);
		}
		
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
		if(arg0.getInput().isKeyPressed(Input.KEY_R)) {
			arg1.enterState(control.StateBasedRunner.MENU);
		}
	}

	@Override
	public int getID() {
		return control.StateBasedRunner.RULE;
	}

}
