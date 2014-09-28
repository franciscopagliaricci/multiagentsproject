package agents;

import java.awt.Rectangle;

public abstract class Creature extends RectangularObject {

	private int health;

	protected Creature(final int health, final Rectangle size, final int maxSpeed) {
		super(size, maxSpeed);
		this.health = health;
	}
	
	public void receiveShot() {
		health--;
	}

	public boolean isDead() {
		return health < 0;
	}

}
