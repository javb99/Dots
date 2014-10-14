package common;

import java.awt.Point;

public class Move {
	public Point point;
	public int axis;
	public int score;

	public Move(int axis, int x, int y, int score)
	{
		this(axis, new Point(x, y), score);
	}

	public Move(int axis, Point point, int score) {
		this.axis = axis;
		this.point = point;
		this.score = score;
	}

	public Move(int axis, int x, int y)
	{
		this(axis, new Point(x, y), 0);
	}

	public Move(int axis, Point point) {
		this(axis, point, 0);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Move) {
			Move move = (Move) object;
			return move.axis == axis && move.point.equals(point) && move.score == score;
		} else {
			return false;
		}

	}
}
