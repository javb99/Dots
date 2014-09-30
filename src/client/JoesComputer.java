package client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import utilities.Constants;
import common.Move;

public class JoesComputer extends ComputerPlayer{
	
	private int boardSize;
	
	Move latestMove;
	
	JoesComputer(BoardController controller) {
		super(controller);
	}

	public void turn(int player) {
		if(player == boardController.getPlayerNumber()) {
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// If here then it is my turn.
			Point square = canFinishSquare();
			System.out.println("square to finish: " + square);
			if (square != null) {
				FinishSquare(square);
			} /**else if (canMakeShadowMove()) {
				makeShadowMove();
			}**/ else {
				makeRandomMove();
			}
			
				
				
			
		}
	}
	
	@SuppressWarnings("unused")
	private ArrayList<Point> getSquares(Move line) {
		ArrayList<Point> list = new ArrayList<Point>();
		int axis = line.axis;
		int x = line.point.x;
		int y = line.point.y;
		
		if (axis == Constants.X_AXIS) {
			// if line is on the NORTH
			if (y < boardSize) {
				list.add(new Point(x, y));
			}
			// if line is on the SOUTH
			if (y > 0) {
				list.add(new Point(x, y-1));
			}
		}
		if (axis == Constants.Y_AXIS) {
			// if checking from left
			if (x < boardSize) {
				list.add(new Point(x, y));
			}
			// if checking from right
			if (x > 0) {
				list.add(new Point(x-1, y));
			}
		}
		return list;
	}
	
	private ArrayList<Move> getLines(Point square) {
		ArrayList<Move> list = new ArrayList<Move>();
		list.add(new Move(Constants.X_AXIS, square.x, square.y, boardController.getOwnerLine(Constants.X_AXIS, square.x, square.y))); // NORTH
		list.add(new Move(Constants.X_AXIS, square.x, square.y +1, boardController.getOwnerLine(Constants.X_AXIS, square.x, square.y +1))); // SOUTH
		list.add(new Move(Constants.Y_AXIS, square.x, square.y, boardController.getOwnerLine(Constants.Y_AXIS, square.x, square.y))); // EAST
		list.add(new Move(Constants.Y_AXIS, square.x +1, square.y, boardController.getOwnerLine(Constants.Y_AXIS, square.x +1, square.y))); // WEST
		return list;
	}
	
	private Point canFinishSquare() {
		for(int x = 0; x < boardSize; ++x) {
		    for(int y = 0; y < boardSize; ++y) {
				Point square = new Point(x, y);
				ArrayList<Move> lines = getLines(square);
				
				int count = 0;
				for (Iterator<Move> lineIterator = lines.iterator(); lineIterator.hasNext();) {
					Move line = lineIterator.next();
					if (line.score > 0) {
						++count;
					}
				}
				if (count == 3) {
					return square;
				}					
		    }
		}
		return null;
	}
	
	private void FinishSquare(Point square) {
		System.out.println("square to finish: " + square);
		ArrayList<Move> lines = getLines(square);
		for (Iterator<Move> lineIterator = lines.iterator(); lineIterator.hasNext();) {
			Move line = lineIterator.next();
			if (line.score == 0) {
				boardController.playLine(line.axis, line.point.x, line.point.y);
			}
		}
	}
	
	private boolean canMakeShadowMove() {              // edges crashing.
		if (latestMove == null) {
			return false;
		}
		try {
			if (latestMove.axis == Constants.X_AXIS) {
				return boardController.getOwnerLine(Constants.Y_AXIS, latestMove.point.x, latestMove.point.y) < 1;
			} else {
				return boardController.getOwnerLine(Constants.X_AXIS, latestMove.point.x, latestMove.point.y) < 1;
			}
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}
	
	private void makeShadowMove() {
		if (latestMove.axis == Constants.X_AXIS) {
			boardController.playLine(Constants.Y_AXIS, latestMove.point.x, latestMove.point.y);
		} else {
			boardController.playLine(Constants.X_AXIS, latestMove.point.x, latestMove.point.y);
		}
	}
	
	private void makeRandomMove() {
		Random random = new Random();
		int axis;
		int x;
		int y;
		while (true) {
			axis = random.nextInt(2);
			x = random.nextInt(axis == Constants.Y_AXIS ? boardSize+1 : boardSize);
			y = random.nextInt(axis == Constants.X_AXIS ? boardSize+1 : boardSize);
			if (boardController.getOwnerLine(axis, x, y) == 0) {
				boardController.playLine(axis, x, y);
				return;
			}
		}
		
	}
	
	private void makeIndexedMove() {
		for(int axis = Constants.X_AXIS; axis <= Constants.Y_AXIS; ++axis) {
			int xSize = axis == Constants.Y_AXIS ? boardSize+1 : boardSize;
			for(int x = 0; x < xSize; ++x) {
				int ySize = axis == Constants.X_AXIS ? boardSize+1 : boardSize;
			    for(int y = 0; y < ySize; ++y) {
			    	if (boardController.getOwnerLine(axis, x, y) == 0) {
						boardController.playLine(axis, x, y);
						return;
					}
			    }
			}
		}
	}

	@Override
	public void move(int player, int axis, int x, int y) {
		 latestMove = new Move(axis, x, y, player);
		 this.boardSize = boardController.getBoardSize();
	}

	@Override
	public void square(int player, int x, int y) {  }
}