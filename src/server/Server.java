package server;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
	public int players = 2;
	public int player;
	public int movesLeft;
	// server related
	public int gamesToPlay;
	public int gameNumber;
	public int[] winners;
	public boolean gameStarted;
	public Selector selector;
	public ServerSocketChannel server;
	public int numberClients;
	Socket[] clients;
	
	public Server(int boardSize, int players, int games) {
		this.boardSize = boardSize;
		this.players = players;
		this.gamesToPlay = games;
		gameNumber = 0;
		winners = new int[gamesToPlay];
		clients = new Socket[players];
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
					} 
					if (key.isAcceptable()) {
						System.out.println("got connection.");
						// accept connection 
						SocketChannel client = server.accept(); 
						client.configureBlocking(false); 
						client.socket().setTcpNoDelay(true); 
						client.register(selector, SelectionKey.OP_READ, numberClients);
						clients[numberClients] = client.socket();
						
						if (numberClients + 1 == players) {
							setupGame();
						}
						numberClients += 1;
					} 
					if (key.isReadable()) { 
						Socket sock = clients[(int) key.attachment()];
						readInput(sock);
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
			System.out.println("error: " + e.toString());
		}
	}
	
	/**
	 * Loops through the clients that are connected and sends the message to them.
	 * @param message: The string to send.
	 */
	public void send(String message) {
		for (int i = 0; i < clients.length; i++) {
			send(clients[i], message);
		}
	}
	
	public void notifyStartGame(int numPlayers, int boardSize) {
		//send("start " + numPlayers + "_" + boardSize);
		for (int i = 0; i < clients.length; i++) {
			send(clients[i], "start " + numPlayers + "_" + boardSize + "_" + (i + 1));
		}
	}
	
	public void notifyPlayerTurn(int playerID) {
		System.out.println(playerID + "'s turn now.");
		send("turn " + playerID);
	}
	
	public void notifyMove(int playerID, int axis, int x, int y) {
		System.out.println("player " + playerID + " placed a line");
		send("move " + playerID + "_" + axis + "_" + x + "_" + y);
	}
	
	public void notifySquare(int playerID, int x, int y) {
		System.out.println("player " + playerID + " finished a square");
		send("square " + playerID + "_" + x + "_" + y);
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
	
	public void notifyEndSession(int winner) {
		send("sessionEnd " + winner);
	}
	
	/**
	 * Reads from the client specified then processes what it read.
	 * @param client: The socket to read from.
	 */
	public void readInput(Socket client) {
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
					System.out.println("player:" + player);
					System.out.println("axis:" + axis);
					System.out.println("x:" + x);
					System.out.println("y:" + y);
					if (!inputMove(axis,x,y,player)) {
						notifyPlayerTurn(this.player);
					}

				}
			} else {
				System.out.println("message not correct format:" + message + ":end");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("client lost connection.");
			System.exit(1);
		}
	}
	
	/**
	 * Initializes the variables of the board. Then starts game by setting gameStarted to true.
	 */
	private void setupGame() {
		boardLines = new int[2][][];
		boardLines[X_AXIS] = new int[boardSize][boardSize +2];
		boardLines[Y_AXIS] = new int[boardSize +2][boardSize];
		boardSquares = new int[boardSize][boardSize];
		score = new int[players];
		player = 0;
		movesLeft = 1;
		notifyStartGame(players, boardSize);
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
		if (boardLines [axis] [x] [y] == 0 && player == this.player) {
			boardLines [axis] [x] [y] = player;
			notifyMove(player, axis, x, y);
			checkSquare(axis, x, y, player);
			notifyScore(this.player, score[this.player - 1]);
			int winner = isGameWon();
			if (winner > 0) {
				System.out.println("there is a winner: " + winner);
				notifyEndGame(winner);
				winners[gameNumber] = winner;
				if (gamesToPlay > 0) {
					++gameNumber;
					setupGame();
				} else {
					notifyEndSession(getSessionWinner());
				}
				return true;
			}
			movesLeft -= 1;
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
		System.out.println("is game won?");
		System.out.println("score length: " + score.length);
		int totalScore = 0;
		int max = 0;
		for (int i = 0; i < score.length; i++) {
			System.out.println("player " + i + "'s score: " + score[i] + ".");
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
	
	private int getSessionWinner() {
		int[] gamesWon = new int[players];
		for (int game = 0; game < winners.length; ++game) {
			++gamesWon[winners[game]];
		}
		int winner = 0;
		for (int i = 0; i < gamesWon.length; ++i) {
			if (gamesWon[i] >= gamesWon[winner]) {
				winner = i;
			}
		}
		return winner;
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
			if (y < boardSize &&  boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y +1] > 0 && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x +1][y] > 0) {
				boardSquares[x] [y] = player;
				notifySquare(player, x, y);
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from bottom
			if (y > 0 && boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y -1] > 0 && boardLines [Y_AXIS][x][y -1] > 0 && boardLines [Y_AXIS][x +1][y -1] > 0) {
				boardSquares[x] [y-1] = player;
				notifySquare(player, x, y-1);
				movesLeft += 1;
				score[player - 1] += 1;
			}
		}
		if (axis == Y_AXIS) {
			// if checking from left
			if (x < boardSize && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x +1][y] > 0 && boardLines [X_AXIS][x][y] > 0 && boardLines [X_AXIS][x][y +1] > 0) {
				boardSquares[x] [y] = player;
				notifySquare(player, x, y);
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from right
			if (x > 0 && boardLines [Y_AXIS][x][y] > 0 && boardLines [Y_AXIS][x -1][y] > 0 && boardLines [X_AXIS][x -1][y] > 0 && boardLines [X_AXIS][x -1][y +1] > 0) {
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
		int gamesL = 1;
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
