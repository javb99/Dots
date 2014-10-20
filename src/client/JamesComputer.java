package client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import utilities.Constants;

import common.Move;

public class JamesComputer extends ComputerPlayer {

	private ArrayList<Move> allMoves = null;

	JamesComputer(BoardController controller, String name) {
		super(controller, name);
	}

	private ArrayList<Move> allMoves()
	{
		if(allMoves == null) {
			int boardSize = boardController.getBoardSize();

			allMoves = new ArrayList<Move>();

			for(int axis=Constants.X_AXIS; axis<=Constants.Y_AXIS; ++axis) {
				int xSize = axis == Constants.Y_AXIS ? boardSize+1 : boardSize;
				for(int x=0; x<xSize; ++x) {
					int ySize = axis == Constants.X_AXIS ? boardSize+1 : boardSize;
					for(int y=0; y<ySize; ++y) {
						allMoves.add(new Move(axis, x, y, 0));
					}
				}
			}
		}

		return allMoves;
	}

	private int squareLineCount(int squareX, int squareY) {

		int squareLineCount = 0;

		for(int axis=Constants.X_AXIS; axis<=Constants.Y_AXIS; ++axis) {
			int xSize = axis == Constants.Y_AXIS ? squareX+1 : squareX;
			for(int x=squareX; x<xSize; ++x) {
				int ySize = axis == Constants.X_AXIS ? squareY+1 : squareY;
				for(int y=squareY; y<ySize; ++y) {
					int owner = boardController.getOwnerLine(axis, x, y);
					if(owner != 0) {
						squareLineCount++;
					}
				}
			}
		}

		return squareLineCount;
	}

	private boolean playValid(int axis, int x, int y) {

		if(x<0) {
			return false;
		}
		if(y<0) {
			return false;
		}

		int boardSize = boardController.getBoardSize();
		int xSize = axis == Constants.Y_AXIS ? boardSize+1 : boardSize;
		int ySize = axis == Constants.X_AXIS ? boardSize+1 : boardSize;

		if(x>=xSize) {
			return false;
		}
		if(y>=ySize) {
			return false;
		}

		return true;
	}

	private ArrayList<Move> movesForSquare(Point point) {
		ArrayList<Move> moves = new ArrayList<Move>();

		moves.add(new Move(Constants.X_AXIS, point.x, point.y, 0));
		moves.add(new Move(Constants.X_AXIS, point.x, point.y+1, 0));
		moves.add(new Move(Constants.Y_AXIS, point.x, point.y, 0));
		moves.add(new Move(Constants.Y_AXIS, point.x+1, point.y, 0));

		return moves;
	}

	private ArrayList<Point> squaresForMove(Move move) {
		ArrayList<Point> squares = new ArrayList<Point>();
		int boardSize = boardController.getBoardSize();

		if(move.axis == Constants.Y_AXIS) {
			if(move.point.x > 0) {
				squares.add(new Point(move.point.x-1, move.point.y));
			}
			if(move.point.x < boardSize-1) {
				squares.add(new Point(move.point.x, move.point.y));
			}
		} else {
			if(move.point.y > 0) {
				squares.add(new Point(move.point.x, move.point.y-1));
			}
			if(move.point.y < boardSize-1) {
				squares.add(new Point(move.point.x, move.point.y));
			}
		}

		return squares;
	}

	private int rateMove(Move move) {

		int score = (int)(Math.random() * 1000);

		// Check if we will complete the square to either side of the line.

		ArrayList<Point> squares = squaresForMove(move);
		for(Point square : squares) {
			int linesAlreadyCompleted = 0;

			ArrayList<Move> moves = movesForSquare(square);
			for(Move currentMove : moves) {
				if(boardController.getOwnerLine(currentMove.axis, currentMove.point.x, currentMove.point.y) != 0) {
					linesAlreadyCompleted++;
				}
			}
			if(linesAlreadyCompleted == 3) {
				score += 100000;
			}
			else if(linesAlreadyCompleted == 2)
			{
				score -= 10000;
			}
		}

		return score;
	}

	@Override
	public void turn(int player) {

		if(player == boardController.getPlayerNumber()) {
			int boardSize = boardController.getBoardSize();

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Comparator<Move> comparator = new Comparator<Move>()
					{
				@Override
				public int compare(Move move1, Move move2)
				{
					return Integer.compare(move2.score, move1.score);
				}
					};

					PriorityQueue<Move> bestMoves = new PriorityQueue<Move>(boardSize^2, comparator);

					for(Move move : allMoves()) {
						int owner = boardController.getOwnerLine(move.axis, move.point.x, move.point.y);
						if(owner == 0) {
							Move currentMove = new Move(move.axis, move.point.x, move.point.y, 0);
							currentMove.score = rateMove(currentMove);
							bestMoves.add(currentMove);
						}
					}

					Move bestMove = bestMoves.poll();
					if(bestMove != null) {
						System.out.println("Best Play is:" + bestMove.axis + " " + bestMove.point.x + " " + bestMove.point.y);
						boardController.playLine(bestMove.axis, bestMove.point.x, bestMove.point.y);
					}
		}
	}

	@Override
	public void square(int player, int x, int y) {
		// TODO Auto-generated method stub

	}
}

