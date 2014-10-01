package common;

import java.awt.Point;

public class JoesMove extends Move {

	public StringBuilder hops;
	
	public JoesMove(Move line, String creator) {
		this(line.axis, line.point, creator);
	}
	
	public JoesMove(int axis, int x, int y, int score) {
		super(axis, new Point(x, y), score);
	}

	public JoesMove(int axis, Point point, int score) {
		super(axis, point, score);
	}
	
	public JoesMove(int axis, Point point, String creator) {
		super(axis, point, -1);
		this.hops = new StringBuilder(creator);
	}

	public JoesMove(int axis, int x, int y, String creator) {
		super(axis, new Point(x, y), -1);
		this.hops = new StringBuilder(creator);
	}
	
	public JoesMove(int axis, Point point) {
		super(axis, point, -1);
	}

	public JoesMove(int axis, int x, int y) {
		super(axis, x, y, -1);
	}
	
	public JoesMove addHop(String hop) {
		this.hops.append(hop);
		return this;
	}

}
