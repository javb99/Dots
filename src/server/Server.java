package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import utilities.Constants;

public class Server {
	// gameplay related
	public static final int X_AXIS = 0;
	public static final int Y_AXIS = 1;
	public int boardSize;
	public int[][][] boardLines;
	public int[][] boardSquares;
	public int[] score;
	public int players;
	public int player;
	public int movesLeft;
	// server related
	public int gamesToPlay;
	public ArrayList<Integer> winners;
	public boolean gameStarted;
	public Selector selector;
	public ServerSocketChannel server;
	public ArrayList<Socket> clients;
	public ArrayList<String> clientNames;
	
	public Server(int boardSize, int players, int games) {
		this.boardSize = boardSize;
		this.players = players;
		this.gamesToPlay = games;
		setupSession();
		try { 
			selector = Selector.open(); 
			server = ServerSocketChannel.open(); 
			server.socket().bind(new InetSocketAddress(Constants.PORT)); 
			server.configureBlocking(false); 
			server.register(selector, SelectionKey.OP_ACCEPT, "Main accept server"); 
			while (true) {
				System.out.println("selecting");
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) { 
					SelectionKey key = iter.next(); 
					iter.remove(); 
					if (key.isConnectable()) { 
						((SocketChannel)key.channel()).finishConnect();
						
					} else if (key.isAcceptable()) {
						// accept connection
						if (!gameStarted) {
							SocketChannel client = server.accept(); 
							client.configureBlocking(false); 
							client.socket().setTcpNoDelay(true); 
							client.register(selector, SelectionKey.OP_READ, clients.size());
							clients.add(client.socket());
							clientNames.add("");
							notifyConnectionFinnished(client.socket(), players, boardSize);
							if (clients.size() == players) {
								setupGame();
							}
							
						} else {
							close(key);
						}
						
					} else if (key.isReadable()) {
						readInput(key);
					} 
				}
			}   		
		} catch (IOException ioe) {
			System.out.println(ioe);
		} finally {
			try {
				selector.close();
				server.socket().close();
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void close(SelectionKey key) throws IOException {
	    key.cancel();
	    key.channel().close();
	}
	
	/**
	 * Sends the message to the connection plus a char at the front telling how long the message is.
	 * @param connection: The socket to send the message.
	 * @param message: The string to send.
	 */
	public void send(Socket connection, String message) {
		try {			
			char lengthChar = (char) message.length();
			String command = lengthChar + message;
			ByteBuffer buff = ByteBuffer.wrap(command.getBytes());;
			connection.getChannel().write(buff);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loops through the clients that are connected and sends the message to them.
	 * @param message: The string to send.
	 */
	public void send(String message) {
		for (int i = 0; i < clients.size(); i++) {
			send(clients.get(i), message);
		}
	}
	
	/**
	 * Sends the entire board(lines) to the client specified.
	 * @param clientId: Index of client to send the dumped variable.
	 */
	public void dumpBoardLines(Socket client) {
		StringBuilder builder = new StringBuilder("dumpLines ");
		for (int axis = Constants.X_AXIS; axis < boardLines.length; ++axis) {
			for (int x = 0; x < boardLines[axis].length; ++x) {
				for (int y = 0; y < boardLines[axis][x].length; ++y) {
					send("move " + boardLines[axis][x][y] + "_" + axis + "_" + x + "_" + y);
				}
			}
		}
		send(client, builder.toString());
	}
	
	/**
	 * Sends the entire board(squares) to the client specified.
	 * @param clientId: Index of client to send the dumped variable.
	 */
	public void dumpBoardSquares(Socket client) {
		StringBuilder builder = new StringBuilder("dumpSquares ");
		for (int x = 0; x < boardSquares.length; ++x) {
			for (int y = 0; y < boardSquares[x].length; ++y) {
				send(client, "square " + boardSquares[x][y] + "_" + x + "_" + y);
			}
		}
		send(client, builder.toString());
	}
	
	public void notifyConnectionFinnished(Socket client, int numPlayers, int boardSize) {
		send(client, "connection " + numPlayers + "_" + boardSize);
	}
	
	public void notifyStartGame() {
		for (int i = 0; i < clients.size(); i++) {
			int id = i +1;
			send(clients.get(i), "start "  + (id));
		}
	}
	
	public void notifyPlayerName(int playerID, String playerName) {
		System.out.println("player name changed to: " + playerName);
		send("name " + playerID + "_" + playerName);
	}
	
	public void notifyPlayerTurn(int playerID) {
		System.out.println(playerID + "'s turn now.");
		send("turn " + playerID);
	}
	
	public void notifyMove(int playerID, int axis, int x, int y) {
		System.out.println("player " + boardLines[axis][x][y] + " placed a line");
		send("move " + boardLines[axis][x][y] + "_" + axis + "_" + x + "_" + y);
	}
	
	public void notifySquare(int playerID, int x, int y) {
		System.out.println("player " + playerID + " finished a square");
		send("square " + boardSquares[x][y] + "_" + x + "_" + y);
	}
	
	public void notifyScore(int playerID, int score) {
		send("score " + playerID + "_" + score);
	}
	
	public void notifyNumberOfPlayers(int numberOfPlayers) {
		send("players " + numberOfPlayers);
	}
	
	public void notifyEndGame(int winner) {
		send("end " + winner);
	}
	
	/**
	 * Notifies all the clients that the session is over then tells the number of games each player won.
	 * @param scores should be length players +1
	 */
	public void notifyEndSession(int[] scores) {
		StringBuilder builder = new StringBuilder();
		for (int player = 1; player < scores.length; player++) {
			builder.append(scores[player] + "_");
		}
		String scoresMessage = builder.toString();
		send("sessionEnd " + scoresMessage);
	}
	
	/**
	 * Reads from the client specified then processes what it read.
	 * @param client: The socket to read from.
	 * @throws IOException: when reseting sessions.
	 */
	public void readInput(SelectionKey key) throws IOException {
		Socket client = null;
		try {
			client = clients.get((int) key.attachment());
		} catch (IndexOutOfBoundsException ioobe) {
			close(key);
			return;
		}
			
		System.out.println("reading input");
		try {
			ByteBuffer buff = ByteBuffer.wrap(new byte[1]);
			client.getChannel().read(buff);
			int length = buff.get(0);
			buff = ByteBuffer.wrap(new byte[length]);
			client.getChannel().read(buff);
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < buff.capacity(); i++) {
				builder.append((char) buff.get(i));
			}
			String message = builder.toString();
			String line = new String(message);

			String[] commands = line.split(" ");
			
			// evals to true if the client is trying to make a move.
			if (commands[0].matches("line")) {
				if (gameStarted == true) {
					String[] lineParams = commands[1].split("_");
					int player = Integer.parseInt(lineParams[0]);
					int axis = Integer.parseInt(lineParams[1]);
					int x = Integer.parseInt(lineParams[2]);
					int y = Integer.parseInt(lineParams[3]);
					//System.out.println("player:" + player);
					System.out.println("axis:" + axis);
					System.out.println("x:" + x);
					System.out.println("y:" + y);
					if (!inputMove(axis,x,y,player)) {
						notifyPlayerTurn(this.player);
					}

				}
			} else if(commands[0].matches("name")) {
				String[] lineParams = commands[1].split("_");
				int playerID = Integer.parseInt(lineParams[0]);
				String playerName = lineParams[1];
				
				clientNames.set(playerID-1, playerName);
				notifyPlayerName(playerID, playerName);
				
			} else if(commands[0].matches("dumpLines")) {
				dumpBoardLines(client);
				
			} else if(commands[0].matches("dumpSquares")) {
				dumpBoardSquares(client);
				
			} else {
				System.out.println("message not correct format:" + message + ":end");
				System.exit(1);
			}
			
		} catch (IOException ioe) {
			System.out.println("client lost connection.");
			sessionOver(score);
			
		} catch (NullPointerException npe) {
			try {
				close(key);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Initializes the variables related to a session.
	 */
	public void setupSession() {
		if (clients != null && clients.size() > 0) {
			for (Socket client : clients) {
				try {
					client.close();
				} catch (IOException ioe) {
					System.out.println("IOException while closing connection to client.");
				}
				
			}
		}
		winners = new ArrayList<Integer>();
		clients = new ArrayList<Socket>();
		clientNames = new ArrayList<String>();
	}
	
	/**
	 * Initializes the variables of the board. Then starts game by setting gameStarted to true.
	 */
	private void setupGame() {
		boardLines = new int[2][][];
		boardLines[X_AXIS] = new int[boardSize][boardSize +1];
		boardLines[Y_AXIS] = new int[boardSize +1][boardSize];
		boardSquares = new int[boardSize][boardSize];
		score = new int[players];
		player = 0;
		movesLeft = 1;
		notifyStartGame();
		gameStarted = true;
		switchPlayer();
	}
	
	/**
	 * Tests if it is that player's turn then notifies the clients of the line, squares created by it, that players score, and if the game is over.
	 * @param axis: The axis of the line that is being set.
	 * @param x: The X coordinate of the line that is being set.
	 * @param y: The Y coordinate of the line that is being set.
	 * @param player: The player id of the player who wants to place the line.
	 * @return false: if it is not that players turn. true: if it is that players turn.
	 */
	public boolean inputMove(int axis, int x, int y, int player){
		// checks if spot is taken
		System.out.println("line taken in owned by: " + boardLines [axis] [x] [y] + ". played by this player: " + player + ". it is this players turn:" + this.player);
		if (axis > Constants.Y_AXIS || x > boardLines[axis].length || y > boardLines[axis][x].length) {
			System.out.println("line not valid.");
		}
		if (boardLines [axis] [x] [y] == 0 && player == this.player) {
			boardLines [axis] [x] [y] = player;
			movesLeft -= 1;
			notifyMove(player, axis, x, y);
			checkSquare(axis, x, y, player);
			notifyScore(this.player, score[this.player - 1]);
			int winner = isGameWon();
			if (winner > 0) {
				gameOver(winner);
				return true;
			}
			
			if (movesLeft < 1) {
				switchPlayer();
				movesLeft = 1;
			} else {
				notifyPlayerTurn(this.player); // Notify the player it is their turn again.
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks if the game has been won.
	 */
	private int isGameWon() {
		//System.out.println("is game won?");
		//System.out.println("score length: " + score.length);
		int totalScore = 0;
		int max = 0;
		for (int i = 0; i < score.length; i++) {
			//System.out.println("player " + (i+1) + "'s score: " + score[i] + ".");
			totalScore += score[i];
			if (score[i] >= score[max]) {
				max = i;
			}
		}
		if (totalScore == (boardSize * boardSize)) {
			return max +1;
		}
		return 0;
	}
	
	private void gameOver(int winner) {
		System.out.println("there is a winner: " + winner);
		notifyEndGame(winner);
		winners.add(winner);
		System.out.println("games to play: " + gamesToPlay + ". winners.size: " + winners.size());
		if (gamesToPlay - winners.size() > 0) {
			setupGame();
		} else {
			sessionOver(getSessionScores());
		}
	}
	
	private void sessionOver(int[] sessionScores) {
		System.out.println("session over.");
		notifyEndSession(sessionScores);
		setupSession();
		gameStarted = false;
	}
	
	private int[] getSessionScores() {
		int[] gamesWon = new int[players +1];
		for (int game = 0; game < winners.size(); ++game) {
			gamesWon[winners.get(game)] += 1;
		}
		int winner = 0;
		for (int i = 1; i < gamesWon.length; ++i) {
			if (gamesWon[i] >= gamesWon[winner]) {
				winner = i;
			}
		}
		return gamesWon;
	}
	
	/**
	 * Checks all four possible orientations if a square has been finished by this line.
	 * @param axis: The axis of the line that is being checked from.
	 * @param x: The X coordinate of the line that is being checked from.
	 * @param y: The Y coordinate of the line that is being checked from.
	 * @param player: The player id of the player that placed this line.
	 */
	public void checkSquare(int axis, int x, int y, int player) {
		if (axis == X_AXIS) {
			// if checking from top
			if ((y >= 0 && y < boardSize && x >= 0 && x < boardSize) &&  boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y +1] > 0 && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x +1][y] > 0) {
				boardSquares[x] [y] = player;
				notifySquare(player, x, y);
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from bottom
			if (((y-1) >= 0 && (y-1) < boardSize && x >= 0 && x < boardSize) && boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y -1] > 0 && boardLines [Y_AXIS][x][y -1] > 0 && boardLines [Y_AXIS][x +1][y -1] > 0) {
				boardSquares[x] [y-1] = player;
				notifySquare(player, x, y-1);
				movesLeft += 1;
				score[player - 1] += 1;
			}
		}
		if (axis == Y_AXIS) {
			// if checking from left
			if ((x >= 0 && x < boardSize && y >= 0 && y < boardSize) && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x +1][y] > 0 && boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y +1] > 0) {
				boardSquares[x] [y] = player;
				notifySquare(player, x, y);
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from right
			if (((x-1) >= 0 && (x-1) < boardSize && y >= 0 && y < boardSize) && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x -1][y] > 0 && boardLines [X_AXIS][x -1][y] > 0 && boardLines [X_AXIS][x -1][y +1] > 0) {
				boardSquares[x-1] [y] = player;
				notifySquare(player, x-1, y);
				movesLeft += 1;
				score[player - 1] += 1;
			}
		}
	}
	/**
	 * Switches who's turn it is then notifies the clients.
	 */
	private void switchPlayer() {
		if (player >= players) {
			player = 1;
		} else {
			player += 1;
		}
		notifyPlayerTurn(this.player);
	}
	public static void main(String[] args) {
		int boardSizeL = 4;
		int playersL = 2;
		int gamesL = 2;
		if (args.length == 4) {
			boardSizeL = Integer.parseInt(args[0]);
			playersL = Integer.parseInt(args[1]);
			gamesL = Integer.parseInt(args[2]);
			Constants.PORT = Integer.parseInt(args[3]);
		}
		if (playersL >= 6 || boardSizeL > 15){
			JOptionPane.showMessageDialog(null, "comand-line argument(s) invalid", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}else {
			new Server(boardSizeL, playersL, gamesL);
		}
	}
}