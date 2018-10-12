package control;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import view.GameState;
import view.MenuState;
import view.ResultState;

public class StateBasedRunner extends StateBasedGame{

	public static final int MENU = 0;
	public static final int GAME = 1;
	public static final int RESULT = 2;
	
	public StateBasedRunner(String name) {
		super(name);
	}

	@Override
	public void initStatesList(GameContainer gc) throws SlickException {
		addState(new MenuState());
		addState(new GameState());
		addState(new ResultState());
	}

}
