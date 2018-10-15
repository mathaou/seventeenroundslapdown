package control;

import java.lang.reflect.Field;
import java.net.UnknownHostException;

import org.newdawn.slick.AppGameContainer;

/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

public class CardGameRunner {
	
	public static void main(String[]args) throws UnknownHostException, Exception {
		
		//make the game
		AppGameContainer game = new AppGameContainer(new StateBasedRunner("Seventeen Round Slap Down"));
		game.setDisplayMode(1920, 1080, false);
		game.setMaximumLogicUpdateInterval(60);
		game.setShowFPS(false);
		game.setAlwaysRender(true);
		
		game.start();
	}
}
