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

public class HumanDisplay extends JFrame implements MouseListener, IDisplay{
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
	public int[][][] boardLines;
	public int[][] boardSquares;
	public int[] score;
	public JLabel[] scoreLabels;
	public JLabel turnLabel;
	public DrawPanel displayPanel;
	public ClientNetwork clientNetwork;
	
	
	
	public HumanDisplay(int boardSizeIn, int thicknessIn, int playersIn) {
		super("Dots And Lines Game");
		ClientNetwork clientNetwork = new ClientNetwork(this);
		
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
		
	}

	@Override
	public void mouseClicked(MouseEvent click) {
		int x = click.getX();
		int y = click.getY();
		DrawPanel panel = (DrawPanel) click.getSource();
		
		if( (int) x % space <= thickness){
			clientNetwork.sendLine(player, Y_AXIS, x, y);
		} else if ( (int) y % space <= thickness) {
			clientNetwork.sendLine(player, X_AXIS, x, y);
		}
		for (int i = 0; i < players; i++) {
			scoreLabels[i].setText(Integer.toString(clientNetwork.getScore(i)));
		}		
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
		int thicknessIN;
		if (args.length == 1) {
			thicknessIN = Integer.parseInt(args[0]);
		} else {
			thicknessIN = 16;
		}
		if (thicknessIN > 64){
			JOptionPane.showMessageDialog(null, "comand-line argument(s) invalid", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(ERROR);
		}
		HumanDisplay frame = new HumanDisplay(10, thicknessIN, 2);
	}

	@Override
	public void connected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameStarting(int numberOfPlayers) {
		boardLines = new int[2][][];
		boardLines [X_AXIS] = new int[boardSize][boardSize +2];
		boardLines [Y_AXIS] = new int[boardSize +2][boardSize];
		boardSquares = new int[boardSize][boardSize];
		score = new int[players];
		player = 0;
		this.setVisible(true);
	}

	@Override
	public void turn(int player) {
		this.player = player;
	}

	@Override
	public void move(int player, int axis, int x, int y) {
		boardLines[axis][x][y] = player;
	}

	@Override
	public void square(int player, int x, int y) {
		boardSquares[x][y] = player;
	}

	@Override
	public void gameOver(int winner) {
		displayPanel.repaint();
		JOptionPane.showMessageDialog(this, "player " + (winner) + " won the game!");
	}
}

class DrawPanel extends JPanel {
	
	private static final int X_AXIS = 0;
	private static final int Y_AXIS = 1;
	public static int space;
	public static int thickness;
	public static int lineLength;
	HumanDisplay parent;
	
	public DrawPanel(HumanDisplay parent){
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
			parent.scoreLabels[i].setText(Integer.toString(parent.clientNetwork.getScore(i)));
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
				comp2D.setColor( HumanDisplay.colors[parent.boardLines [X_AXIS][x][y] ] );
				comp2D.fillRect(x * space + thickness, y * space, lineLength, thickness);
			}
		}
		// Checks all lines on the X axis then draws the lines on the Y axis.
		for (int x = 0; x < parent.boardLines [Y_AXIS].length; x++) {
			for (int y = 0; y < parent.boardLines [Y_AXIS][x].length; y++) {
				comp2D.setColor( HumanDisplay.colors[parent.boardLines [Y_AXIS][x][y] ] );
				comp2D.fillRect(x * space, y * space + thickness, thickness, lineLength);
			}
		}
		// Checks all squares then displays the owner.
		for (int x = 0; x < parent.boardSquares.length; x++) {
			for (int y = 0; y < parent.boardSquares[x].length; y++) {
				comp2D.setColor( HumanDisplay.colors[parent.boardSquares[x][y] ] );
				comp2D.fillRect(x * space + thickness + (thickness / 2), y * space + thickness + (thickness / 2), thickness * 2, thickness * 2);
			}
		}
		comp2D.setColor(Color.BLACK);
		
	}	
}