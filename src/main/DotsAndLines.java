package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// change turns and scoring...........................................

public class DotsAndLines extends JFrame implements MouseListener{
	// class variables
	public static final int Y_AXIS = 1;
	public static final int X_AXIS = 0;
	
	public int space;
	public int thickness;
	public int boardSize;
	public int players;
	public static final Color[] colors = new Color[] {Color.white, Color.red, Color.blue, Color.green, Color.orange, Color.pink, Color.cyan};
	
	// instance variables
	int player;
	int movesLeft = 1;
	public int[] score;
	public JLabel[] scoreLabels;
	public JLabel turnLabel;
	public DrawPanel displayPanel;
	
	public int[][][] boardLines;

	public int[][] board;
	
	public DotsAndLines(int boardSizeIn, int thicknessIn, int playersIn) {
		super("Dots And Lines Game");
		this.boardSize = boardSizeIn;
		this.thickness = thicknessIn;
		this.players = playersIn;
		this.space = thickness * 4;
		
		setSize( (boardSize * space) + thickness * 2, (boardSize * space) + thickness + space );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		displayPanel = new DrawPanel(this);
		JPanel scorePanel = new JPanel();
		turnLabel = new JLabel();
		scorePanel.add(turnLabel);
		score = new int[players];
		scoreLabels = new JLabel[players];
		for (int i = 0; i < scoreLabels.length; i++) {
			
			scoreLabels[i] = new JLabel(Integer.toString(score[i]));
			scoreLabels[i].setForeground(colors[i+1]);
			scoreLabels[i].setVisible(true);
			scorePanel.add(scoreLabels[i]);
		}
		JPanel panel = new JPanel();
		BorderLayout box = new BorderLayout();
		panel.setLayout(box);
		panel.add(scorePanel, BorderLayout.NORTH);
		panel.add(displayPanel, BorderLayout.CENTER);
		add(panel);
		displayPanel.addMouseListener(this);
		setupGame();
		setVisible(true);
		
	}
	
	private void setupGame() {
		boardLines = new int[2][][];
		boardLines [X_AXIS] = new int[boardSize][boardSize +2];
		boardLines [Y_AXIS] = new int[boardSize +2][boardSize];
		board = new int[boardSize][boardSize];
		score = new int[players];
		player = 0;
		switchPlayer();
	}
	
	public void checkSquare(int type, int x, int y, int player) {
		if (type == X_AXIS) {
			// if checking from top
			if (y < boardSize &&  boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y +1] > 0 && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x +1][y] > 0) {
				board[x] [y] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from bottom
			if (y > 0 && boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y -1] > 0 && boardLines [Y_AXIS][x][y -1] > 0 && boardLines [Y_AXIS][x +1][y -1] > 0) {
				board[x] [y-1] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
		}
		if (type == Y_AXIS) {
			// if checking from left
			if (x < boardSize && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x +1][y] > 0 && boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y +1] > 0) {
				board[x] [y] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from right
			if (x > 0 && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x -1][y] > 0 && boardLines [X_AXIS][x -1][y] > 0 && boardLines [X_AXIS][x -1][y +1] > 0) {
				board[x-1] [y] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
		}
	}
	
	private void switchPlayer() {
		if (player >= players) {
			player = 1;
		} else {
			player += 1;
		}
		turnLabel.setText("player " + player + "'s turn.");
		turnLabel.setForeground(colors[player]);
	}

	@Override
	public void mouseClicked(MouseEvent click) {
		int x = click.getX();
		int y = click.getY();
		DrawPanel panel = (DrawPanel) click.getSource();
		System.out.println("X: " + x + " Y: " + y);
		System.out.println("X space: " + (int) x / space + " Y space: " + (int) y / space);
		//System.out.println("the X space was held by : " + boardLines [X_AXIS][(int) x / space][(int) y / space] + " the Y space was held by : " + boardLines [Y_AXIS][(int) x / space][(int) y / space]);
		System.out.println("X mod space: " + (int) x % space);
		
		if( (int) x % space <= thickness){
			// checks if spot is taken
			if (boardLines [Y_AXIS][(int) x / space] [(int) y / space] == 0) {
				// clicked on the West.
				boardLines [Y_AXIS][(int) x / space] [(int) y / space] = player;
				checkSquare(Y_AXIS, (int) x / space, (int) y / space, player);
				movesLeft -= 1;
				if (movesLeft < 1) {
					switchPlayer();
					movesLeft = 1;
				}
			}
		} else if ( (int) y % space <= thickness) {
			// checks if spot is taken
			if (boardLines [X_AXIS][(int) x / space] [(int) y / space] == 0) {
				// clicked on the North
				boardLines [X_AXIS][(int) x / space] [(int) y / space] = player;
				checkSquare(X_AXIS, (int) x / space, (int) y / space, player);
				movesLeft -= 1;
				if (movesLeft < 1) {
					switchPlayer();
					movesLeft = 1;
				}
			}
		}
		// update score label and check for a winner.
		int totalScore = 0;
		int winningPlayer = 0;
		for (int i = 0; i < players; i++) {
			scoreLabels[i].setText(Integer.toString(score[i]));
			totalScore += score[i];
			if (score[i] > score[winningPlayer]) {
				winningPlayer = i;
			}
		}
		if (totalScore == boardSize * boardSize) {
			displayPanel.repaint();
			JOptionPane.showMessageDialog(this, "player " + (winningPlayer+1) + " won the game!");
			int playAgain = JOptionPane.showOptionDialog(this, "Play Again?", "Play Again", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, 0);
			if (playAgain == JOptionPane.YES_OPTION) {
				setupGame();
			} else {
				System.exit(0);
			}
		}
		this.repaint();
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
	
	public static void main(String[] args) {
		int boardSizeIN;
		int thicknessIN;
		int playersIN;
		
		if (args.length == 3) {
			boardSizeIN = Integer.parseInt(args[0]);
			thicknessIN = Integer.parseInt(args[1]);
			playersIN = Integer.parseInt(args[2]);
			
		} else {
			boardSizeIN = 10;
			thicknessIN = 16;
			playersIN = 2;
		}
		if (playersIN >= colors.length || boardSizeIN > 15){
			JOptionPane.showMessageDialog(null, "comand-line argument(s) invalid", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(ERROR);
		}
		DotsAndLines frame = new DotsAndLines(boardSizeIN,thicknessIN,playersIN);
	}
}

class DrawPanel extends JPanel {
	
	private static final int X_AXIS = 0;
	private static final int Y_AXIS = 1;
	public static int space;
	public static int thickness;
	public static int lineLength;
	DotsAndLines parent;
	
	public DrawPanel(DotsAndLines parent){
		super();
		this.parent = parent;
		space = parent.space;
		thickness = parent.thickness;
		lineLength = space - thickness;
	}
	@Override
	public void paintComponent(Graphics comp) {
		super.paintComponent(comp);
		Graphics2D comp2D = (Graphics2D) comp;
		comp2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		comp2D.setColor(Color.BLACK);
		
		// update score labels.
		for (int i = 0; i < parent.players; i++) {
			parent.scoreLabels[i].setText(Integer.toString(parent.score[i]));
		}
		// Draws dots.
		for (int x = 0; x < parent.boardSize+1; x++) {
			for (int y = 0; y < parent.boardSize+1; y++) {
				comp2D.fillRect(x * space, y * space, thickness, thickness);
			}
		}
		// Checks all lines on the X axis then draws the lines on the X axis.
		for (int x = 0; x < parent.boardLines [X_AXIS].length; x++) {
			for (int y = 0; y < parent.boardLines [X_AXIS][x].length; y++) {
				comp2D.setColor( DotsAndLines.colors[parent.boardLines [X_AXIS][x][y] ] );
				comp2D.fillRect(x * space + thickness, y * space, lineLength, thickness);
			}
		}
		// Checks all lines on the X axis then draws the lines on the Y axis.
		for (int x = 0; x < parent.boardLines [Y_AXIS].length; x++) {
			for (int y = 0; y < parent.boardLines [Y_AXIS][x].length; y++) {
				comp2D.setColor( DotsAndLines.colors[parent.boardLines [Y_AXIS][x][y] ] );
				comp2D.fillRect(x * space, y * space + thickness, thickness, lineLength);
			}
		}
		// Checks all squares then displays the owner.
		for (int x = 0; x < parent.board.length; x++) {
			for (int y = 0; y < parent.board[x].length; y++) {
				comp2D.setColor( DotsAndLines.colors[parent.board[x][y] ] );
				comp2D.fillRect(x * space + thickness + (thickness / 2), y * space + thickness + (thickness / 2), thickness * 2, thickness * 2);
			}
		}
		comp2D.setColor(Color.BLACK);
		
	}	
}