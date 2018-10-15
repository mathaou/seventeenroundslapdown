package control;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import view.GameState;
import view.MenuState;
import view.ResultState;
import view.RulesState;

/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

public class StateBasedRunner extends StateBasedGame{

	//ids for different states
	public static final int MENU = 0;
	public static final int GAME = 1;
	public static final int RESULT = 2;
	public static final int RULE = 3;
	
	public StateBasedRunner(String name) {
		super(name);
	}

	@Override
	public void initStatesList(GameContainer gc) throws SlickException {
		addState(new MenuState());
		addState(new RulesState());
		addState(new GameState());
		addState(new ResultState());
	}

}
