package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

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
	// display related
	public IDisplay display;
	
	
	
	public ClientNetwork(IDisplay display, int port) {
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
			display.connected();
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
		//out.println("line " + player + "_" + axis + "_" + x + "_" + y);
		send(client, "line " + myID + "_" + axis + "_" + x + "_" + y);
	}
	
	public void send(Socket connection, String message) {
		try {
			char lengthChar = (char) message.length();
			String command = lengthChar + message;
			connection.getOutputStream().write(command.getBytes());
			System.out.write(command.getBytes());
		} catch (Exception e) {
			System.out.println("error: " + e.toString());
		}
	}
	
	private void readInput(BufferedReader reader) {
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
			String[] commands = line.split(" ");
			String[] commandParams = commands[1].split("_");

			switch(commands[0]) {
			case "start": // Notifying that the game is starting.
				players = Integer.parseInt(commandParams[0]);
				boardSize = Integer.parseInt(commandParams[1]);
				myID = Integer.parseInt(commandParams[2]);
				boardLines = new int[2][][];
				boardLines [Constants.X_AXIS] = new int[boardSize][boardSize +2];
				boardLines [Constants.Y_AXIS] = new int[boardSize +2][boardSize];
				boardSquares = new int[boardSize][boardSize];
				scores = new int[players + 1];
				player = 0;
				gameStarted = true;
				display.gameStarting(players, boardSize, myID);
				break;
				
			case "turn": // Notifying who's turn it is.
				this.player = Integer.parseInt(commandParams[0]);
				display.turn(this.player);
				break;
				
			case "move": // Notifying of a move that was made.
				player = Integer.parseInt(commandParams[0]);
				axis = Integer.parseInt(commandParams[1]);
				x = Integer.parseInt(commandParams[2]);
				y = Integer.parseInt(commandParams[3]);
				boardLines[axis][x][y] = player;
				display.move(player, axis, x, y);
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
				
			default:
				System.out.println("Unrecognized command.");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public int[][][] getBoardLines() {
		return boardLines;
	}
	
	public int[][] getBoardSquares() {
		return boardSquares;
	}
	
	public boolean isGameStarted() {
		return this.gameStarted;
	}
	
	// Board Controller Implementation

	public int getScore(int player) {
		return scores[player];
	}
	
	public int getPlayerNumber() {
		return myID;
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
}
