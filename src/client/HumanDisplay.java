package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import utilities.Constants;

// change turns and scoring...........................................

public class HumanDisplay extends JPanel implements Runnable, MouseListener, IDisplay {
	/**
	 *
	 */

	private static final long serialVersionUID = 1L;
	public int boardSize;

	private Thread runner;

	public JLabel[] scoreLabels;
	public JLabel turnLabel;
	public JPanel scorePanel;
	public DrawPanel displayPanel;
	public String cardName;

	public ClientNetwork clientNetwork;
	private boolean allowInput;

	public HumanDisplay(ClientNetwork clientNetwork, String name) {
		this(clientNetwork, name, true);
	}

	public HumanDisplay(ClientNetwork clientNetwork, String name, boolean allowInput) {
		this.allowInput = allowInput;
		this.clientNetwork = clientNetwork;
		cardName = Constants.BOARD_CARD_NAME + " " + name;

		setupDisplay();
		System.out.println("add board card with this name: " + cardName);
		Constants.client.mainPanel.add(this, cardName);


		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	@Override
	public void run() {	}

	public void setupDisplay() {
		// board card
		displayPanel = new DrawPanel(this);
		scorePanel = new JPanel();
		turnLabel = new JLabel();
		scorePanel.add(turnLabel);

		BorderLayout border = new BorderLayout();
		this.setLayout(border);
		this.add(scorePanel, BorderLayout.NORTH);
		this.add(displayPanel, BorderLayout.CENTER);
		displayPanel.addMouseListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent click) {
		if(allowInput) {
			int x = click.getX();
			int y = click.getY();
			int X = x / Constants.SPACE;
			int Y = y / Constants.SPACE;

			if( x % Constants.SPACE <= Constants.THICKNESS && y % Constants.SPACE > Constants.THICKNESS){
				clientNetwork.sendLine(Constants.Y_AXIS, X, Y);
			} else if ( y % Constants.SPACE <= Constants.THICKNESS && x % Constants.SPACE > Constants.THICKNESS) {
				clientNetwork.sendLine(Constants.X_AXIS, X, Y);
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

	// server interaction.

	@Override
	public void connected(int numberOfPlayers, int boardSize) {
		if (numberOfPlayers > 0 && boardSize > 0) {
			System.out.println("connection called");
			this.boardSize = boardSize;
			if (scorePanel.getComponentCount() > 2) {
				for (JLabel scoreLabel : scoreLabels) {
					scorePanel.remove(scoreLabel);
				}
			}
			scoreLabels = new JLabel[numberOfPlayers];
			for (int i = 0; i < scoreLabels.length; i++) {
				scoreLabels[i] = new JLabel(Integer.toString(clientNetwork.getScore(i)));
				scoreLabels[i].setForeground(Constants.colors[i+1]);
				scoreLabels[i].setVisible(true);
				scorePanel.add(scoreLabels[i]);
			}
		}
	}

	@Override
	public void spectator(int numberOfPlayers, int boardSize) {
		JOptionPane.showMessageDialog(this, "You are spectating");
	}

	@Override
	public void gameStarting(int myID) {
		this.setSize( boardSize * Constants.SPACE + Constants.THICKNESS * 2, boardSize * Constants.SPACE + Constants.THICKNESS + Constants.SPACE);
		displayPanel.repaint();
	}

	@Override
	public void gameOver(int winner) {
		displayPanel.repaint();
		if (allowInput) {
			JOptionPane.showMessageDialog(this, "player " + winner + " won the game!");
		}
	}

	@Override
	public void sessionOver(int[] scores) {
		displayPanel.repaint();
		String playerName;
		StringBuilder builder = new StringBuilder();
		for (int player = 0; player < scores.length; player++) {
			playerName = clientNetwork.getPlayerName(player+1);
			if (player+1 == clientNetwork.getPlayerNumber()) {
				builder.append("You won " + scores[player] + " games.\n");
			} else if (playerName.length() > 0) {
				builder.append(playerName + " won " + scores[player] + " games.\n");
			} else {
				builder.append("Player " + (player +1) + " won " + scores[player] + " games.\n");
			}
		}
		JOptionPane.showMessageDialog(this, builder.toString());
	}

	@Override
	public void turn(int player) {
		turnLabel.setForeground(Constants.colors[player]);
		String playerName = clientNetwork.getPlayerName(player);
		if (player == clientNetwork.getPlayerNumber()) {
			Constants.client.switchCard(cardName);
			turnLabel.setText("Your turn.");
		}else if (playerName.length() > 0) {
			turnLabel.setText(playerName + "'s turn.");
		} else {
			turnLabel.setText(player + "'s turn.");
		}
	}

	@Override
	public void move(int player, int axis, int x, int y) {
		displayPanel.repaint();
		Constants.client.mainPanel.repaint();
	}

	@Override
	public void square(int player, int x, int y) {
		displayPanel.repaint();
	}

}

// board display.

class DrawPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	HumanDisplay parent;

	public DrawPanel(HumanDisplay parent){
		super();
		this.parent = parent;
	}

	@Override
	public void paintComponent(Graphics comp) {
		super.paintComponent(comp);
		Graphics2D comp2D = (Graphics2D) comp;
		comp2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		comp2D.setColor(Color.BLACK);

		// update score labels.
		for (int i = 0; i < parent.clientNetwork.getPlayerCount(); i++) {
			try {
				parent.scoreLabels[i].setText(Integer.toString(parent.clientNetwork.getScore(i +1)));
			} catch (NullPointerException npe) {
				System.out.println("scores not ititalised yet.");
			}
		}

		// Draws dots.
		for (int x = 0; x < parent.clientNetwork.getBoardSize()+1; x++) {
			for (int y = 0; y < parent.clientNetwork.getBoardSize()+1; y++) {
				comp2D.fillRect(x * Constants.SPACE, y * Constants.SPACE, Constants.THICKNESS, Constants.THICKNESS);
			}
		}

		int[][][] boardLinesCopy = parent.clientNetwork.getBoardLinesCopy();
		int[][] boardSquaresCopy = parent.clientNetwork.getBoardSquaresCopy();

		// Checks all lines on the X axis then draws the lines on the X axis.
		for (int x = 0; x < boardLinesCopy[Constants.X_AXIS].length; x++) {
			for (int y = 0; y < boardLinesCopy[Constants.X_AXIS][x].length; y++) {
				comp2D.setColor( Constants.colors[parent.clientNetwork.getOwnerLine(Constants.X_AXIS, x, y)] );
				comp2D.fillRect(x * Constants.SPACE + Constants.THICKNESS, y * Constants.SPACE, Constants.LINELENGTH, Constants.THICKNESS);
			}
		}
		// Checks all lines on the Y axis then draws the lines on the Y axis.
		for (int x = 0; x < boardLinesCopy[Constants.Y_AXIS].length; x++) {
			for (int y = 0; y < boardLinesCopy[Constants.Y_AXIS][x].length; y++) {
				comp2D.setColor( Constants.colors[parent.clientNetwork.getOwnerLine(Constants.Y_AXIS, x, y)] );
				comp2D.fillRect(x * Constants.SPACE, y * Constants.SPACE + Constants.THICKNESS, Constants.THICKNESS, Constants.LINELENGTH);
			}
		}
		// Checks all squares then displays the owner.
		for (int x = 0; x < boardSquaresCopy.length; x++) {
			for (int y = 0; y < boardSquaresCopy[x].length; y++) {
				comp2D.setColor( Constants.colors[parent.clientNetwork.getOwnerSquare(x, y)] );
				comp2D.fillRect(x * Constants.SPACE + Constants.THICKNESS + Constants.THICKNESS / 2, y * Constants.SPACE + Constants.THICKNESS + Constants.THICKNESS / 2, Constants.THICKNESS * 2, Constants.THICKNESS * 2);
			}
		}
		comp2D.setColor(Color.BLACK);

	}
}