package model;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Card {
	
	private int rank, suit;
	private Image cardFace;
	
	public Card(int rank, int suit, String fileName) throws SlickException {
		this.rank = rank;
		this.suit = suit;
		setCardFace(new Image("/images/"+fileName));
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getSuit() {
		return suit;
	}

	public void setSuit(int suit) {
		this.suit = suit;
	}
	
	public Image getCard() {
		return cardFace;
	}
	
	public void setCardFace(Image cardFace) {
		this.cardFace = cardFace;
	}
	
	public void drawFace(int x, int y) {
		this.cardFace.draw(x,y);
	}
}
