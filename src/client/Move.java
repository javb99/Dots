package client;

import java.awt.Point;

public class Move {
	public Point point;
	public int axis;
	public int score;
	
	Move(int axis, int x, int y, int score)
	{
		this.axis = axis;
		this.point = new Point(x,y);
		this.score = score;
	}
}
