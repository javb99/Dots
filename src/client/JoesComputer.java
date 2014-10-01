package client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import utilities.Constants;
import common.JoesMove;
import common.Move;

public class JoesComputer extends ComputerPlayer{
	
	private int boardSize;
	private int[][][] boardLines;
	private int[][] boardSquares;
	
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
			JoesMove line = finishSquareMove();
			//System.out.println("The hops: " + line.hops.toString());
			playLine(line);
		}
	}
	/**
	 * @param square: the square to check from.
	 * @returns the number of line of the square specified that are owned.
	 */
	private int getCount(Point square) {
		int count = 0;
		ArrayList<Move> lines = getLines(square);
		for (Iterator<Move> lineIterator = lines.iterator(); lineIterator.hasNext();) {
			Move line = lineIterator.next();
			if (getOwnerLine(line) > 0) {
				++count;
			}
		}
		return count;
	}
	
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
	
	private void playLine(Move line) {
		boardController.playLine(line.axis, line.point.x, line.point.y);
	}
	
	private int getOwnerLine(Move line) {
		return boardController.getOwnerLine(line.axis, line.point.x, line.point.y);
	}
	
	private void createBoardCopy() {
		boardLines = boardController.getBoardLinesCopy();
		boardSquares = boardController.getBoardSquaresCopy();
	}
	
	private JoesMove finishSquareMove() {
		for(int x = 0; x < boardSize; ++x) {
		    for(int y = 0; y < boardSize; ++y) {
				Point square = new Point(x, y);
				if (boardController.getOwnerSquare(x, y) == 0) { // square is not owned.
					ArrayList<Move> lines = getLines(square);
					
					JoesMove finishLine = new JoesMove(0,0,0, "error"); // default value will never be used if set up correctly.
					int count = 0;
					for (Iterator<Move> lineIterator = lines.iterator(); lineIterator.hasNext();) {
						JoesMove line = new JoesMove(lineIterator.next(), "Square");
						
						if (getOwnerLine(line) > 0) { // line is owned.
							++count;
						} else if (getOwnerLine(line) == 0) {
							finishLine = line;
						}
						
						if (count == 3) { // because there are three lines already taken and the square is not owned this square can be finished.
							if (getOwnerLine(finishLine) == 0) {
								return finishLine;
							}
						}
					}
				}
		    }
		}
		return dontMakeStupidMove().addHop("Square");
	}

	private JoesMove dontMakeStupidMove() {
		ArrayList<Move> whitelist = new ArrayList<Move>();
		ArrayList<Move> blacklist = new ArrayList<Move>();
		
		for(int axis=Constants.X_AXIS; axis<=Constants.Y_AXIS; ++axis) {
			int xSize = axis == Constants.Y_AXIS ? boardSize+1 : boardSize;
			for(int x=0; x<xSize; ++x) {
				int ySize = axis == Constants.X_AXIS ? boardSize+1 : boardSize;
				for(int y=0; y<ySize; ++y) {
					Move line = new Move(axis, x, y);
					int owner = boardController.getOwnerLine(axis, x, y);
					if(owner == 0) { // line is not owned
						
						ArrayList<Point> squares = getSquares(new Move(axis, x, y));
						for (Iterator<Point> squareIterator = squares.iterator(); squareIterator.hasNext();) {
							Point square = squareIterator.next();
							if (getCount(square) == 2) {
								blacklist.add(line);
							}
						}
						if (!blacklist.contains(line)) {
							whitelist.add(line);
						}
					}
				}
			}
		}
		
		if (whitelist.isEmpty() && !blacklist.isEmpty()) {
			//System.out.println("blacklist.");
			return makeRandomMove(blacklist).addHop("Stupid");
		} else {
			//System.out.println("whitelist.");
			return makeRandomMove(whitelist).addHop("Stupid");
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
	
	private JoesMove makeRandomMove(ArrayList<Move> list) {
		Random random = new Random();
		//System.out.println("list size." + list.size());
		return new JoesMove(list.get(random.nextInt(list.size())), "Random");
	}
	
	private JoesMove makeRandomMove() {
		Random random = new Random();
		int axis;
		int x;
		int y;
		while (true) {
			axis = random.nextInt(2);
			x = random.nextInt(axis == Constants.Y_AXIS ? boardSize+1 : boardSize);
			y = random.nextInt(axis == Constants.X_AXIS ? boardSize+1 : boardSize);
			if (boardController.getOwnerLine(axis, x, y) == 0) {
				return new JoesMove(axis, x, y, "Random");
			}
		}
		
	}

	@Override
	public void move(int player, int axis, int x, int y) {
		this.boardSize = boardController.getBoardSize();
		createBoardCopy();
		latestMove = new Move(axis, x, y, player);
		

	}

	@Override
	public void square(int player, int x, int y) {  }
}