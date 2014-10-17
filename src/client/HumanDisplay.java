package client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utilities.Constants;

// change turns and scoring...........................................

public class HumanDisplay extends JFrame implements ActionListener, MouseListener, IDisplay {
	/**
	 *
	 */
	public static void main(String[] args) {
		int thicknessIN = 16;
		int portIN = Constants.PORT;
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

	private static final long serialVersionUID = 1L;
	// class variables
	public static final int Y_AXIS = 1;
	public static final int X_AXIS = 0;

	public int space;
	public int thickness;
	public int boardSize;
	public static final Color[] colors = new Color[] {Color.white, Color.red, Color.blue, Color.green, Color.orange, Color.pink, Color.cyan};

	// instance variables

	//  non gui related.
	public String ip;
	public int port;
	public String computerName;

	//  gui related.
	public CardLayout cardLayout;
	public JPanel mainPanel;
	//   Single player screen related.
	public JPanel singlePlayerScreenPanel;
	//   Multi player screen related.
	public JPanel multiPlayerScreenPanel;
	public JTextField portField;
	public JTextField ipField;
	public JButton connectButton;
	//   Title screen related.
	public JPanel titleScreenPanel;
	public JLabel connectingLabel;
	//   Board screen related.
	public JPanel boardPanel;
	public JLabel[] scoreLabels;
	public JLabel turnLabel;
	public JPanel scorePanel;
	public DrawPanel displayPanel;

	public ClientNetwork clientNetwork;
	public Player computerPlayer;

	public HumanDisplay(int boardSizeIn, int thicknessIn, int playersIn, int port, String computerNameIn) {
		super("Dots And Lines Game client: " + computerNameIn);

		ip = Constants.IP;
		this.port = port;
		computerName = computerNameIn;
		thickness = thicknessIn;
		space = thickness * 4;

		// Sets up the gui.
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setupBoardPanel();
		setupTitleScreenPanel();
		setupSinglePlayerPanel();
		setupMultiPlayerPanel();

		// combination
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);
		mainPanel.add(titleScreenPanel, Constants.TITLE_CARD_NAME);
		mainPanel.add(boardPanel, Constants.BOARD_CARD_NAME);
		mainPanel.add(singlePlayerScreenPanel, Constants.SINGLE_PLAYER_CARD_NAME);
		mainPanel.add(multiPlayerScreenPanel, Constants.MULTI_PLAYER_CARD_NAME);
		switchCard(Constants.TITLE_CARD_NAME);

		add(mainPanel);
		this.setVisible(true);
	}

	public void setupBoardPanel() {
		// board card
		displayPanel = new DrawPanel(this);
		scorePanel = new JPanel();
		turnLabel = new JLabel();
		scorePanel.add(turnLabel);

		boardPanel = new JPanel();
		BorderLayout border = new BorderLayout();
		boardPanel.setLayout(border);
		boardPanel.add(scorePanel, BorderLayout.NORTH);
		boardPanel.add(displayPanel, BorderLayout.CENTER);
		displayPanel.addMouseListener(this);
	}

	public void setupTitleScreenPanel()	{
		// title screen card
		titleScreenPanel = new JPanel();
		JLabel logo = new JLabel(new ImageIcon("assets//TitleScreenLogo.png"));

		JButton singlePlayerButton = new JButton("Single Player");
		singlePlayerButton.setActionCommand(Constants.GO_TO_SINGLE_PLAYER);
		singlePlayerButton.addActionListener(this);

		JButton multiPlayerButton = new JButton("Multi Player");
		multiPlayerButton.setActionCommand(Constants.GO_TO_MULTI_PLAYER);
		multiPlayerButton.addActionListener(this);



		titleScreenPanel.add(logo);
		titleScreenPanel.add(singlePlayerButton);
		titleScreenPanel.add(multiPlayerButton);
	}

	public void setupSinglePlayerPanel() {
		// Single player setup panel
		singlePlayerScreenPanel = new JPanel();

		JLabel computerNameLabel = new JLabel("Computer Name");
		JComboBox<String> nameList = new JComboBox<String>(Constants.validComputerNames);

		JButton titleScreenButton = new JButton("Back");
		titleScreenButton.setActionCommand(Constants.GO_TO_TITLE);
		titleScreenButton.addActionListener(this);

		singlePlayerScreenPanel.add(computerNameLabel);
		singlePlayerScreenPanel.add(nameList);
		singlePlayerScreenPanel.add(titleScreenButton);
	}

	public void setupMultiPlayerPanel() {
		// Multi player setup panel
		multiPlayerScreenPanel = new JPanel();

		// ip input box
		JPanel ipPanel = new JPanel();
		ipPanel.setLayout(new BoxLayout(ipPanel, BoxLayout.X_AXIS));
		JLabel ipLabel = new JLabel("IP:");
		ipField = new JTextField(Constants.IP);
		ipField.setActionCommand(Constants.IP_CHANGE);
		ipField.addActionListener(this);
		ipPanel.add(ipLabel);
		ipPanel.add(ipField);

		// port input box.
		JPanel portPanel = new JPanel();
		portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.X_AXIS));
		JLabel portLabel = new JLabel("PORT:");
		portField = new JTextField(Integer.toString(Constants.PORT));
		portField.setActionCommand(Constants.PORT_CHANGE);
		portField.addActionListener(this);
		portPanel.add(portLabel);
		portPanel.add(portField);

		// connect button.
		connectButton = new JButton("Connect");
		connectButton.setActionCommand(Constants.CONNECT_TO_SERVER);
		connectButton.addActionListener(this);

		// connecting label.
		connectingLabel = new JLabel();
		changeConnectionStatus(null);

		// button to go back to the title screen.
		JButton titleScreenButton = new JButton("Back");
		titleScreenButton.setActionCommand(Constants.GO_TO_TITLE);
		titleScreenButton.addActionListener(this);

		multiPlayerScreenPanel.add(ipPanel);
		multiPlayerScreenPanel.add(portPanel);
		multiPlayerScreenPanel.add(connectButton);
		multiPlayerScreenPanel.add(connectingLabel);
		multiPlayerScreenPanel.add(titleScreenButton);
	}

	public void startButtonPressed() {
		System.out.println("Start Button Pressed.");
		changeConnectionStatus(null);
		clientNetwork = new ClientNetwork(this, ip, port, false);
		if ( computerName.equals("James")) {
			computerPlayer = new JamesComputer(clientNetwork);
		} else if (computerName.equals("Joe")) {
			computerPlayer = new JoesComputer(clientNetwork);
		}
		if (computerPlayer == null) {
			computerName = JOptionPane.showInputDialog(this, "What would you like your name to be?");
		}
		clientNetwork.runner.start();
	}

	public void connectButtonPressed() {
		String ip = ipField.getText();
		System.out.println("testing: " + ip + " to see if it is valid.");
		int port;
		try {
			port = Integer.parseInt(portField.getText());
		} catch (NumberFormatException nfe) {
			return;
		}
		if (port <= 65535 && port > 1024 && checkIPv4(ip)) {
			this.port = port;
			this.ip = ip;
			startButtonPressed();
		}

	}

	public void changeConnectionStatus(String status) {
		if (status != null) {
			connectingLabel.setText(status);
			connectingLabel.setVisible(true);
			connectButton.setVisible(false);
		} else {
			connectingLabel.setVisible(false);
			connectingLabel.setText("Connecting...");
			connectButton.setVisible(true);
		}
	}

	public void switchCard(String cardId) {
		switch (cardId) {

		case Constants.TITLE_CARD_NAME:
			cardLayout.show(mainPanel, Constants.TITLE_CARD_NAME);
			this.setSize(160, 160);
			connectingLabel.setVisible(false);
			break;

		case Constants.BOARD_CARD_NAME:
			cardLayout.show(mainPanel, Constants.BOARD_CARD_NAME);
			break;

		case Constants.MULTI_PLAYER_CARD_NAME:
			cardLayout.show(mainPanel, Constants.MULTI_PLAYER_CARD_NAME);
			this.setSize(160, 160);
			break;

		case Constants.SINGLE_PLAYER_CARD_NAME:
			cardLayout.show(mainPanel, Constants.SINGLE_PLAYER_CARD_NAME);
			this.setSize(160, 160);
			break;
		}


	}

	public static final boolean checkIPv4(final String ip) {
	    boolean isIPv4;
	    try {
	    final InetAddress inet = InetAddress.getByName(ip);
	    isIPv4 = inet.getHostAddress().equals(ip) && inet instanceof Inet4Address;
	    } catch (final UnknownHostException e) {
	    isIPv4 = false;
	    }
	    return isIPv4;
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		switch(action.getActionCommand()) {

		case Constants.CONNECT_TO_SERVER:
			connectButtonPressed();
			break;

		case Constants.GO_TO_TITLE: // goes to the title menu.
			switchCard(Constants.TITLE_CARD_NAME);
			break;

		case Constants.GO_TO_SINGLE_PLAYER: // goes to the single player setup menu.
			switchCard(Constants.SINGLE_PLAYER_CARD_NAME);
			break;

		case Constants.GO_TO_MULTI_PLAYER: // goes to the multi player setup menu.
			switchCard(Constants.MULTI_PLAYER_CARD_NAME);
			break;

		default:
			System.out.println("did not match any registered buttons.");
		}
	}

	@Override
	public void mouseClicked(MouseEvent click) {
		if(computerPlayer == null)
		{
			int x = click.getX();
			int y = click.getY();
			int X = x / space;
			int Y = y / space;

			if( x % space <= thickness && y % space > thickness){
				clientNetwork.sendLine(Y_AXIS, X, Y);
			} else if ( y % space <= thickness && x % space > thickness) {
				clientNetwork.sendLine(X_AXIS, X, Y);
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
			changeConnectionStatus("Waiting for \nmore players...");
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
				scoreLabels[i].setForeground(colors[i+1]);
				scoreLabels[i].setVisible(true);
				scorePanel.add(scoreLabels[i]);
			}
		} else {
			changeConnectionStatus(null);
		}
	}

	@Override
	public void spectator(int numberOfPlayers, int boardSize) {
		JOptionPane.showMessageDialog(this, "You are spectating");
		computerPlayer = null;
	}

	@Override
	public void gameStarting(int myID) {
		clientNetwork.setName(computerName);
		// gui
		cardLayout.show(mainPanel, Constants.BOARD_CARD_NAME);
		setSize( boardSize * space + thickness * 2, boardSize * space + thickness + space );
		displayPanel.repaint();
	}

	@Override
	public void gameOver(int winner) {
		displayPanel.repaint();
		if (computerPlayer == null) {
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
		switchCard(Constants.TITLE_CARD_NAME);
	}

	@Override
	public void turn(int player) {
		turnLabel.setForeground(colors[player]);
		String playerName = clientNetwork.getPlayerName(player);
		if (player == clientNetwork.getPlayerNumber()) {
			turnLabel.setText("Your turn.");
		}else if (playerName.length() > 0) {
			turnLabel.setText(playerName + "'s turn.");
		} else {
			turnLabel.setText(player + "'s turn.");
		}
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


// board display.


class DrawPanel extends JPanel {
	private static final long serialVersionUID = 1L;
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
				comp2D.fillRect(x * space, y * space, thickness, thickness);
			}
		}

		int[][][] boardLinesCopy = parent.clientNetwork.getBoardLinesCopy();
		int[][] boardSquaresCopy = parent.clientNetwork.getBoardSquaresCopy();

		// Checks all lines on the X axis then draws the lines on the X axis.
		for (int x = 0; x < boardLinesCopy[Constants.X_AXIS].length; x++) {
			for (int y = 0; y < boardLinesCopy[Constants.X_AXIS][x].length; y++) {
				comp2D.setColor( HumanDisplay.colors[parent.clientNetwork.getOwnerLine(Constants.X_AXIS, x, y)] );
				comp2D.fillRect(x * space + thickness, y * space, lineLength, thickness);
			}
		}
		// Checks all lines on the Y axis then draws the lines on the Y axis.
		for (int x = 0; x < boardLinesCopy[Constants.Y_AXIS].length; x++) {
			for (int y = 0; y < boardLinesCopy[Constants.Y_AXIS][x].length; y++) {
				comp2D.setColor( HumanDisplay.colors[parent.clientNetwork.getOwnerLine(Constants.Y_AXIS, x, y)] );
				comp2D.fillRect(x * space, y * space + thickness, thickness, lineLength);
			}
		}
		// Checks all squares then displays the owner.
		for (int x = 0; x < boardSquaresCopy.length; x++) {
			for (int y = 0; y < boardSquaresCopy[x].length; y++) {
				comp2D.setColor( HumanDisplay.colors[parent.clientNetwork.getOwnerSquare(x, y)] );
				comp2D.fillRect(x * space + thickness + thickness / 2, y * space + thickness + thickness / 2, thickness * 2, thickness * 2);
			}
		}
		comp2D.setColor(Color.BLACK);

	}
}