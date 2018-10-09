package model;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Card {
	
	private int rank, suit;
	
	public Card(int rank, int suit) throws SlickException {
		this.rank = rank;
		this.suit = suit;
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
}
