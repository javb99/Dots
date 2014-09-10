package main;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.colorchooser.ColorSelectionModel;

// change turns and scoring...........................................

public class DotsAndLines extends JFrame implements MouseListener{
	// class variables
	public static final int Y_AXIS = 1;
	public static final int X_AXIS = 0;
	
	public static int space;
	public static int thickness;
	public static int boardSize;
	public static int players;
	public static final Color[] colors = new Color[] {Color.white, Color.red, Color.blue, Color.green, Color.orange, Color.pink, Color.cyan};
	
	// instance variables
	int player;
	int movesLeft = 1;
	public int[] score;
	public JLabel[] scoreLabels;
	public JLabel turnLabel;
	public DrawPanel displayPanel;
	
	public int[][] boardx;
	public int[][] boardy;
	public int[][] board;
	
	public DotsAndLines() {
		super("Dots And Lines Game");
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
		panel.add(scorePanel, box.NORTH);
		panel.add(displayPanel, box.CENTER);
		add(panel);
		displayPanel.addMouseListener(this);
		setVisible(true);
		setupGame();
	}
	
	private void setupGame() {
		boardx = new int[boardSize][boardSize +2];
		boardy = new int[boardSize +2][boardSize];
		board = new int[boardSize][boardSize];
		score = new int[players];
		player = 0;
		switchPlayer();
	}
	
	public void checkSquare(int type, int x, int y, int player) {
		if (type == X_AXIS) {
			// if checking from top
			if (y < boardSize &&  boardx[x][y] > 0 && boardx[x][y +1] > 0 && boardy[x][y] > 0 && boardy[x +1][y] > 0) {
				board[x] [y] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from bottom
			if (y > 0 && boardx[x][y] > 0 && boardx[x][y -1] > 0 && boardy[x][y -1] > 0 && boardy[x +1][y -1] > 0) {
				board[x] [y-1] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
		}
		if (type == Y_AXIS) {
			// if checking from left
			if (x < boardSize && boardy[x][y] > 0 && boardy[x +1][y] > 0 && boardx[x][y] > 0 && boardx[x][y +1] > 0) {
				board[x] [y] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from right
			if (x > 0 && boardy[x][y] > 0 && boardy[x -1][y] > 0 && boardx[x -1][y] > 0 && boardx[x -1][y +1] > 0) {
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
		//System.out.println("the X space was held by : " + boardx[(int) x / space][(int) y / space] + " the Y space was held by : " + boardy[(int) x / space][(int) y / space]);
		System.out.println("X mod space: " + (int) x % space);
		
		if( (int) x % space <= thickness){
			// checks if spot is taken
			if (boardy[(int) x / space] [(int) y / space] == 0) {
				// clicked on the West.
				boardy[(int) x / space] [(int) y / space] = player;
				checkSquare(Y_AXIS, (int) x / space, (int) y / space, player);
				movesLeft -= 1;
				if (movesLeft < 1) {
					switchPlayer();
					movesLeft = 1;
				}
			}
		} else if ( (int) y % space <= thickness) {
			// checks if spot is taken
			if (boardx[(int) x / space] [(int) y / space] == 0) {
				// clicked on the North
				boardx[(int) x / space] [(int) y / space] = player;
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
		if (args.length == 3) {
			boardSize = Integer.parseInt(args[0]);
			thickness = Integer.parseInt(args[1]);
			players = Integer.parseInt(args[2]);
			
		} else {
			boardSize = 10;
			thickness = 16;
			players = 2;
		}
		if (players >= colors.length || boardSize > 15){
			JOptionPane.showMessageDialog(null, "comand-line argument(s) invalid", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(ERROR);
		}
		space = thickness * 4;
		
		DotsAndLines frame = new DotsAndLines();
	}
}

class DrawPanel extends JPanel {
	
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
		for (int x = 0; x < parent.boardx.length; x++) {
			for (int y = 0; y < parent.boardx[x].length; y++) {
				comp2D.setColor( parent.colors[parent.boardx[x][y] ] );
				comp2D.fillRect(x * space + thickness, y * space, lineLength, thickness);
			}
		}
		
		// Checks all lines on the Y axis then draws the lines on the Y axis.
		for (int x = 0; x < parent.boardy.length; x++) {
			for (int y = 0; y < parent.boardy[x].length; y++) {
				comp2D.setColor(parent.colors[parent.boardy[x][y]]);
				comp2D.fillRect(x * space, y * space + thickness, thickness, lineLength);
			}
		}
		
		// Checks all squares then displays the owner.
		for (int x = 0; x < parent.board.length; x++) {
			for (int y = 0; y < parent.board[x].length; y++) {
				comp2D.setColor( parent.colors[parent.board[x][y] ] );
				comp2D.fillRect(x * space + thickness + (thickness / 2), y * space + thickness + (thickness / 2), thickness * 2, thickness * 2);
			}
		}
		comp2D.setColor(Color.BLACK);
		
	}	
}