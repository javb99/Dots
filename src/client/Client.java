package client;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import server.Server;
import utilities.Constants;

public class Client extends JFrame implements ActionListener, ChangeListener, Runnable, IDisplay{

	private static final long serialVersionUID = 1L;
	//  non gui related.
	public String ip = Constants.IP;
	public int port;
	public boolean isMultiPlayer = false;
	public ArrayList<ClientNetwork> networks;
	public ArrayList<IDisplay> displays;
	@SuppressWarnings("unused")
	private Server server;
	private Thread runner;

	//  gui related.
	public CardLayout cardLayout;
	public JPanel mainPanel;
	//   Single player screen related.
	public JPanel singlePlayerScreenPanel;
	public JSpinner playerSlider;
	public JSpinner sizeSlider;
	public JPanel[] playerSetupPanels;
	public JComboBox<String>[] computerNameBoxes;
	public JTextField[] nameTextFields;
	//   Multi player screen related.
	public JPanel multiPlayerScreenPanel;
	public JTextField portField;
	public JTextField ipField;
	public JPanel playerSetupPanel;
	public JComboBox<String> computerNameBox;
	public JTextField nameTextField;
	public JButton connectButton;
	//   Title screen related.
	public JPanel titleScreenPanel;
	public JLabel connectingLabel;
	//  Board screen related.
	public JPanel boardPanel;

	public static void main(String[] args) {
		int thicknessIN = 16;
		if (args.length >= 1) {
			thicknessIN = Integer.parseInt(args[0]);
		}
		if (thicknessIN > 64){
			JOptionPane.showMessageDialog(null, "comand-line argument(s) invalid", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		Constants.client = new Client();
	}

	public Client() {
		super("Dots And Lines Game client");

		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	@Override
	public void run() {
		setupGUI();
	}

	public void setupGUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setupBoardPanel();
		setupTitleScreenPanel();
		setupSinglePlayerPanel();
		setupMultiPlayerPanel();

		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);
		mainPanel.add(titleScreenPanel, Constants.TITLE_CARD_NAME);
		mainPanel.add(singlePlayerScreenPanel, Constants.SINGLE_PLAYER_CARD_NAME);
		mainPanel.add(multiPlayerScreenPanel, Constants.MULTI_PLAYER_CARD_NAME);
		switchCard(Constants.TITLE_CARD_NAME);

		add(mainPanel);
		this.setVisible(true);
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

		JLabel sizeLabel = new JLabel("Board Size: ");
		singlePlayerScreenPanel.add(sizeLabel);

		sizeSlider = new JSpinner(new SpinnerNumberModel(5, 3, 15, 2));
		singlePlayerScreenPanel.add(sizeSlider);

		JLabel playerNumberLabel = new JLabel("# players.");
		singlePlayerScreenPanel.add(playerNumberLabel);

		playerSlider = new JSpinner(new SpinnerNumberModel(2, 1, Constants.colors.length -1, 1));
		playerSlider.addChangeListener(this);
		singlePlayerScreenPanel.add(playerSlider);

		computerNameBoxes = new JComboBox[Constants.colors.length -1];
		nameTextFields = new JTextField[Constants.colors.length -1];
		playerSetupPanels = new JPanel[Constants.colors.length -1];

		for (int i = 0; i < computerNameBoxes.length; ++i) {
			playerSetupPanels[i] = new JPanel();

			nameTextFields[i] = new JTextField("Player " + (i +1));
			computerNameBoxes[i] = new JComboBox<String>(Constants.validComputerNames);
			playerSetupPanels[i].add(nameTextFields[i]);
			playerSetupPanels[i].add(computerNameBoxes[i]);
			singlePlayerScreenPanel.add(playerSetupPanels[i]);
			if (i > 1) {
				playerSetupPanels[i].setVisible(false);
			}
		}
		JButton startSinglePlayerButton = new JButton("Start");
		startSinglePlayerButton.setActionCommand(Constants.START_SINGLE_PLAYER);
		startSinglePlayerButton.addActionListener(this);

		JButton titleScreenButton = new JButton("Back");
		titleScreenButton.setActionCommand(Constants.GO_TO_TITLE);
		titleScreenButton.addActionListener(this);

		singlePlayerScreenPanel.add(startSinglePlayerButton);
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

		// name and computer selector.
		playerSetupPanel = new JPanel();

		nameTextField = new JTextField("Player");
		computerNameBox = new JComboBox<String>(Constants.validComputerNames);
		playerSetupPanel.add(nameTextField);
		playerSetupPanel.add(computerNameBox);
		multiPlayerScreenPanel.add(playerSetupPanel);

		// connect button.
		connectButton = new JButton("Connect");
		connectButton.setActionCommand(Constants.START_MULTI_PLAYER);
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

	public void setupBoardPanel() {

	}

	private void startSinglePlayer() {

		networks = new ArrayList<ClientNetwork>();
		displays = new  ArrayList<IDisplay>();

		// TODO: be prepared for names that are the same.
		int numberOfPlayers = (int) playerSlider.getValue();
		int boardSize = (int) sizeSlider.getValue();
		server = new Server(boardSize, numberOfPlayers, 1, Constants.LOCAL_PORT);

		for (int i = 0; i < numberOfPlayers; ++i) { // Add a player each iteration.

			networks.add(new ClientNetwork("localhost", Constants.LOCAL_PORT, false));
			ClientNetwork network = networks.get(i);
			String name = nameTextFields[i].getText();
			String computerName = (String) computerNameBoxes[i].getSelectedItem();

			network.setName(name);

			// Connects the AI to the network.
			if (computerName.equals("James")) {
				displays.add(new JamesComputer(network, name));
				System.out.println("Added James as a player.");
			} else if (computerName.equals("Joe")) {
				displays.add(new JoesComputer(network, name));
				System.out.println("Added Joe as a player.");
			} else if (computerName.equals("Human")) {
				displays.add(new HumanDisplay(network, name, true));
				System.out.println("Added Human as a player.");
			} else {
				System.out.println("error line 242: computer name is not valid.");
			}

			network.addDisplay(this);
			network.addDisplay(displays.get(displays.size()-1));
		}

		boolean hasVisualDisplay = false;
		for (IDisplay display: displays) {
			if (display instanceof HumanDisplay) {
				hasVisualDisplay = true;
				break;
			}
		}
		if (!hasVisualDisplay) {
			HumanDisplay humanDisplay = new HumanDisplay(networks.get(0), nameTextFields[0].getText(), false);
			networks.get(0).addDisplay(humanDisplay);
			System.out.println("Added Human as a Display.");
		}

		for (ClientNetwork network : networks) {
			network.runner.start();
		}
	}

	public void startMultiPlayer() { // Add HumanDisplay as a display everytime.
		String ip = ipField.getText();
		int port;
		try {
			port = Integer.parseInt(portField.getText());
		} catch (NumberFormatException nfe) {
			return;
		}
		if (port <= 65535 && port > 1024 && checkIPv4(ip)) {
			this.port = port;
			this.ip = ip;
		}

		String name = nameTextField.getText();
		String computerName = (String) computerNameBox.getSelectedItem();

		changeConnectionStatus(null);
		networks.add(new ClientNetwork(ip, port, false));
		ClientNetwork network = networks.get(0);

		if (computerName.equals("James")) {
			displays.add(new JamesComputer(network, name));
		} else if (computerName.equals("Joe")) {
			displays.add(new JoesComputer(network, name));
		}
		displays.add(new HumanDisplay(network, name, computerName.equals("Human")));

		network.addDisplay(this);
		for (IDisplay display: displays) {
			network.addDisplay(display);
		}
		network.runner.start();

	}

	public void changeConnectionStatus(String status) {
		if (status != null) {
			connectingLabel.setText(status);
			connectingLabel.setVisible(true);
			connectButton.setVisible(false);
		} else {
			connectingLabel.setText("Connecting...");
			connectingLabel.setVisible(false);
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
			int value = (int) playerSlider.getValue();
			//this.setSize(160, 110 + value * 40);
			Dimension size = playerSetupPanels[0].getSize();
			this.setSize(160, 130 + value * (size.height + 5));
			break;

		default:
			if (cardId.startsWith(Constants.BOARD_CARD_NAME)) {
				this.setSize( networks.get(0).getBoardSize() * Constants.SPACE + Constants.THICKNESS * 2, networks.get(0).getBoardSize() * Constants.SPACE + Constants.THICKNESS + Constants.SPACE );
				cardLayout.show(mainPanel, cardId);
			}
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

		case Constants.START_SINGLE_PLAYER:
			startSinglePlayer();
			break;

		case Constants.START_MULTI_PLAYER:
			startMultiPlayer();
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
	public void stateChanged(ChangeEvent event) {
		JSpinner source = (JSpinner) event.getSource();
		if (source.equals(playerSlider)) {
			int value = (int) source.getValue();
			for (int i = 0; i < playerSetupPanels.length; ++i) {
				playerSetupPanels[i].setVisible(i < value);
			}
			switchCard(Constants.SINGLE_PLAYER_CARD_NAME);
		}
	}

	@Override
	public void turn(int player) {	}

	@Override
	public void move(int player, int axis, int x, int y) {	}

	@Override
	public void square(int player, int x, int y) {	}

	@Override
	public void connected(int numberOfPlayers, int boardSize) {
		changeConnectionStatus("waiting for more players.....");
	}

	@Override
	public void gameStarting(int myID) {
		changeConnectionStatus(null);
	}

	@Override
	public void spectator(int numberOfPlayers, int boardSize) {	}

	@Override
	public void gameOver(int winner) {

	}

	@Override
	public void sessionOver(int[] scores) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.out.println("InterruptedException when thread sleeping.");
		}
		switchCard(Constants.TITLE_CARD_NAME);
	}
}
