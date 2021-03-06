package gui;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Set;

import agents.RectangularObject;

public interface Paintable {

	/**
	 * @return the list of points used by the painting.
	 */
	Set<Point> paint(Graphics g);
	
	RectangularObject getBackendModel();
}
