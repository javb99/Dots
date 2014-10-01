package client;

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

public class HumanDisplay extends JFrame implements MouseListener, IDisplay {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// class variables
	public static final int Y_AXIS = 1;
	public static final int X_AXIS = 0;
	
	public int space;
	public int thickness;
	public int boardSize;
	public int players;
	public static final Color[] colors = new Color[] {Color.white, Color.red, Color.blue, Color.green, Color.orange, Color.pink, Color.cyan};
	
	// instance variables
	public JLabel[] scoreLabels;
	public JLabel turnLabel;
	public DrawPanel displayPanel;
	public ClientNetwork clientNetwork;
	public Player computerPlayer;
	
	public HumanDisplay(int boardSizeIn, int thicknessIn, int playersIn, int portIn, String computerNameIn) {
		super("Dots And Lines Game");
		
		clientNetwork = new ClientNetwork(this, portIn);
		
		if ( computerNameIn.equals("James")) {
			computerPlayer = new JamesComputer(clientNetwork);
		} else if (computerNameIn.equals("Joe")) {
			computerPlayer = new JoesComputer(clientNetwork);
		}
		
		this.thickness = thicknessIn;
		this.space = thickness * 4;
		
		displayPanel = new DrawPanel(this);
		JPanel scorePanel = new JPanel();
		turnLabel = new JLabel();
		scorePanel.add(turnLabel);
		
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
		
		if(computerPlayer == null)
		{
			int x = click.getX();
			int y = click.getY();
			int X = x / space;
			int Y = y / space;
			
			if( (int) x % space <= thickness){
				clientNetwork.sendLine(Y_AXIS, X, Y);
				//System.out.println("clicked on the Y");
			} else if ( (int) y % space <= thickness) {
				clientNetwork.sendLine(X_AXIS, X, Y);
				//System.out.println("clicked on the X");
			}	
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
		int thicknessIN = 16;
		int portIN = 65001;
		String computerNameIN = "human";
		if (args.length >= 2) {
			thicknessIN = Integer.parseInt(args[0]);
			portIN = Integer.parseInt(args[1]);
		}
		
		if (args.length >= 3) {
			computerNameIN = args[2];
		}
		if (thicknessIN > 64 && portIN > 1024 && portIN < 65535 ){
			JOptionPane.showMessageDialog(null, "comand-line argument(s) invalid", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(ERROR);
		}
		new HumanDisplay(10, thicknessIN, 2, portIN, computerNameIN);
	}

	@Override
	public void connected() {
		System.out.println("connection called");
	}
	
	@Override
	public void spectator(int numberOfPlayers, int boardSize) {
		JOptionPane.showMessageDialog(this, "You are spectating");
		computerPlayer = null;
	}
	
	@Override
	public void gameStarting(int numberOfPlayers, int boardSize, int myID) {
		// non gui
		this.boardSize = boardSize;
		System.out.println("my id: " + myID);
		// gui
		setSize( (boardSize * space) + thickness * 2, (boardSize * space) + thickness + space );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		displayPanel = new DrawPanel(this);
		JPanel scorePanel = new JPanel();
		turnLabel = new JLabel();
		scorePanel.add(turnLabel);
		scoreLabels = new JLabel[numberOfPlayers];
		for (int i = 0; i < scoreLabels.length; i++) {
			scoreLabels[i] = new JLabel(Integer.toString(clientNetwork.getScore(i)));
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
		
		this.setVisible(true);
	}

	@Override
	public void gameOver(int winner) {
		displayPanel.repaint();
		if (computerPlayer == null) {
			JOptionPane.showMessageDialog(this, "player " + (winner) + " won the game!");
		}
	}
	
	@Override
	public void sessionOver(int[] scores) {
		displayPanel.repaint();
		StringBuilder builder = new StringBuilder();
		for (int player = 0; player < scores.length; player++) {
			builder.append("player " + (player +1) + " won " + scores[player] + " games.\n");
		}
		JOptionPane.showMessageDialog(this, builder.toString());
		System.exit(0);
	}
	
	@Override
	public void turn(int player) {
		turnLabel.setForeground(colors[player]);
		turnLabel.setText(player + "'s turn.");
		if(computerPlayer != null) {
			computerPlayer.turn(player);
		}
	}

	@Override
	public void move(int player, int axis, int x, int y) {
		if(computerPlayer != null) {
			computerPlayer.move(player, axis, x, y);
		}
		displayPanel.repaint();
	}

	@Override
	public void square(int player, int x, int y) {
		if(computerPlayer != null) {
			computerPlayer.square(player, x, y);
		}
		displayPanel.repaint();
	}
}

class DrawPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
			//System.out.println(parent.clientNetwork.getScore(i));
		}
		// Draws dots.
		for (int x = 0; x < parent.boardSize+1; x++) {
			for (int y = 0; y < parent.boardSize+1; y++) {
				comp2D.fillRect(x * space, y * space, thickness, thickness);
			}
		}
		
		int[][][] boardLinesCopy = parent.clientNetwork.getBoardLinesCopy();
		int[][] boardSquaresCopy = parent.clientNetwork.getBoardSquaresCopy();
		
		// Checks all lines on the X axis then draws the lines on the X axis.
		for (int x = 0; x < boardLinesCopy[X_AXIS].length; x++) {
			for (int y = 0; y < boardLinesCopy[X_AXIS][x].length; y++) {
				comp2D.setColor( HumanDisplay.colors[parent.clientNetwork.getOwnerLine(X_AXIS, x, y)] );
				comp2D.fillRect(x * space + thickness, y * space, lineLength, thickness);
			}
		}
		// Checks all lines on the Y axis then draws the lines on the Y axis.
		for (int x = 0; x < boardLinesCopy[Y_AXIS].length; x++) {
			for (int y = 0; y < boardLinesCopy[Y_AXIS][x].length; y++) {
				comp2D.setColor( HumanDisplay.colors[parent.clientNetwork.getOwnerLine(Y_AXIS, x, y)] );
				comp2D.fillRect(x * space, y * space + thickness, thickness, lineLength);
			}
		}
		// Checks all squares then displays the owner.
		for (int x = 0; x < boardSquaresCopy.length; x++) {
			for (int y = 0; y < boardSquaresCopy[x].length; y++) {
				comp2D.setColor( HumanDisplay.colors[parent.clientNetwork.getOwnerSquare(x, y)] );
				comp2D.fillRect(x * space + thickness + (thickness / 2), y * space + thickness + (thickness / 2), thickness * 2, thickness * 2);
			}
		}
		comp2D.setColor(Color.BLACK);
		
	}	
}
