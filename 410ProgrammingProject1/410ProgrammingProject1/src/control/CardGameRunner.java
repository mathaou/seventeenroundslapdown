package control;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import model.Player;

public class CardGameRunner {
	
	public static void main(String[]args) throws UnknownHostException, Exception {
		
		AppGameContainer game = new AppGameContainer(new StateBasedRunner("Seventeen Round Slap Down"));
		game.setDisplayMode(1920, 1080, false);
		game.setMaximumLogicUpdateInterval(60);
		game.setShowFPS(false);
		game.setAlwaysRender(true);
		
		game.start();
	}
}
