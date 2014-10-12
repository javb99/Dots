package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import utilities.Constants;

public class ClientNetwork implements Runnable, BoardController {
	
	// network related
	private Thread runner;
	public Socket server;
	private Socket client;
	// game related
	private boolean gameStarted;
	private int boardSize;
	private int[][][] boardLines;
	private int[][] boardSquares;
	private int[] scores;
	private int winner;
	private int players;
	private int player;
	private int myID;
	private ArrayList<String> clientNames;
	// display related
	public IDisplay display;
	
	
	
	public ClientNetwork(IDisplay display, int port) { // todo not using port passed in.
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
		this.display = display;
	}
	
	@Override
	public void run() {
		try {
			client = new Socket();
			client.connect(new InetSocketAddress(Constants.IP, Constants.PORT));
			BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			while(true) {
				readInput(input);
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		} finally {
			try {
				client.close();
			} catch (Exception e) {
				// do nothing - client failed
			}
		}
	}
	// use this to send moves to server.
	public void sendLine(int axis, int x, int y) {
		send(client, "line " + myID + "_" + axis + "_" + x + "_" + y);
	}
	
	// use this to send name changes to server.
	public void sendNameChange(String name) {
		send(client, "name " + myID + "_" + name);
	}
	
	public void send(Socket connection, String message) {
		try {
			/**byte[] lengthBytes = NetworkHelper.intToByteArray(message.length());
			System.out.println("length bytes going out: " + message.length());
			ByteBuffer messageBytes = ByteBuffer.wrap(message.getBytes());
			connection.getChannel().write(ByteBuffer.wrap(lengthBytes));
			connection.getChannel().write(messageBytes);**/
			
			char lengthChar = (char) message.length(); 
			String command = lengthChar + message; 
			connection.getOutputStream().write(command.getBytes()); 

			
		} catch (Exception e) {
			e.printStackTrace();;
		}
	}
	
	private void readInput(BufferedReader reader) {
		String playerName;
		int player;
		int x;
		int y;
		int axis;
		int score;
		
		try {
			int length = reader.read();
			char[] message = new char[length];
			reader.read(message);
			String line = new String(message);
			
			System.out.println("command: " + line + ". length char:" + length);
			if ( line.length() < 3) {
				System.out.println("command not long enough.");
			}
			String[] commands = line.split(" ");
			String[] commandParams = commands[1].split("_");
			
			switch(commands[0]) {
			case "connection":
				players = Integer.parseInt(commandParams[0]);
				boardSize = Integer.parseInt(commandParams[1]);
				clientNames = new ArrayList<String>();
				for (int i = 0; i < players+1; ++i) {
					clientNames.add("");
				}
				setupGame();
				display.connected(players, boardSize);
				break;
				
			case "name": // Notifying that there is a name now registered with this number.
				player = Integer.parseInt(commandParams[0]);
				playerName = commandParams[1];
				clientNames.set(player, playerName);
				System.out.println("player name changed to: " + playerName);
				break;
				
			case "start": // Notifying that the game is starting.
				myID = Integer.parseInt(commandParams[0]);
				setupGame();
				player = 0;
				gameStarted = true;
				display.gameStarting(myID);
				break;
				
			case "turn": // Notifying who's turn it is.
				this.player = Integer.parseInt(commandParams[0]);
				display.turn(this.player);
				break;
				
			case "move": // Notifying of a move that was made.
				//send(client, "dumpLines");
				player = Integer.parseInt(commandParams[0]);
				axis = Integer.parseInt(commandParams[1]);
				x = Integer.parseInt(commandParams[2]);
				y = Integer.parseInt(commandParams[3]);
				boardLines[axis][x][y] = player;
				display.move(player, axis, x, y);
				break;
				
			case "dumpLines":
				String[] lineParams;
				for (String lines : commandParams) {
					 lineParams = lines.split(",");
					 System.out.println("lines: " + lines);
					 axis = Integer.parseInt(lineParams[0]);
					 x = Integer.parseInt(lineParams[1]);
					 y = Integer.parseInt(lineParams[2]);
					 player = Integer.parseInt(lineParams[3]);
					 boardLines[axis][x][y] = player;
				}
				break;
				
			case "dumpSquares":
				String[] squareParams;
				for (String lines : commandParams) {
					 squareParams = lines.split(",");
					 x = Integer.parseInt(squareParams[0]);
					 y = Integer.parseInt(squareParams[1]);
					 player = Integer.parseInt(squareParams[2]);
					 boardSquares[x][y] = player;
				}
				break;
				
			case "square": // Notifying of a square that is now owned.
				player = Integer.parseInt(commandParams[0]);
				x = Integer.parseInt(commandParams[1]);
				y = Integer.parseInt(commandParams[2]);
				boardSquares[x][y] = player;
				display.square(player, x, y);
				break;
				
			case "score": // Notifying of the score for a player.
				player = Integer.parseInt(commandParams[0]);
				score = Integer.parseInt(commandParams[1]);
				scores[player] = score;
				break;
				
			case "end": // Notifying of the winner of the game
				winner = Integer.parseInt(commandParams[0]);
				display.gameOver(winner);
				break;
				
			case "sessionEnd": // Notifying of the winner of the session.
				int[] scores = new int[commandParams.length];
				for (int i = 0; i < commandParams.length; i++) {
					scores[i] = Integer.parseInt(commandParams[i]);
				}
				display.sessionOver(scores);
				break;
				
			default:
				System.out.println("Unrecognized command.");
			}
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "server crashed or is full of players");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	private void setupGame() {
		boardLines = new int[2][][];
		boardLines [Constants.X_AXIS] = new int[boardSize][boardSize +1];
		boardLines [Constants.Y_AXIS] = new int[boardSize +1][boardSize];
		boardSquares = new int[boardSize][boardSize];
		scores = new int[players + 1];
	}
	
	public boolean isGameStarted() {
		return this.gameStarted;
	}
	
	public boolean isSpectator() {
		return myID > 0;
	}
	// Board Controller Implementation

	public int getScore(int player) {
		return scores[player];
	}
	
	public int getPlayerNumber() {
		return myID;
	}
	
	public String getPlayerName(int playerNumber) {
		return clientNames.get(playerNumber);
	}
	
	public int getOwnerLine(int axis, int x, int y) {
		return boardLines[axis][x][y];
	}
	
	public int getOwnerSquare(int x, int y) {
		return boardSquares[x][y];
	}

	@Override
	public int getPlayerCount() {
		return players;
	}

	@Override
	public int getBoardSize() {
		return boardSize;
	}

	@Override
	public void playLine(int axis, int x, int y) {
		sendLine(axis, x, y);
	}
	
	@Override
	public void setName(String playerName) {
		sendNameChange(playerName);
	}
	
	@Override
	public int[][][] getBoardLinesCopy() {
		return boardLines.clone();
	}
	
	@Override
	public int[][] getBoardSquaresCopy() {
		return boardSquares.clone();
	}
	
	
}