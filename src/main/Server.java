package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import javax.swing.JOptionPane;

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
	public boolean gameStarted;
	public static final int PORT = 1010;
	public Selector selector;
	public ServerSocketChannel server;
	public int numberClients;
	Socket[] clients;
	
	public Server(int boardSize, int players) {
		this.boardSize = boardSize;
		this.players = players;
		clients = new Socket[players];
		try { 
			selector = Selector.open(); 
			server = ServerSocketChannel.open(); 
			server.socket().bind(new InetSocketAddress(PORT)); 
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
			
		} finally {
			try {
				selector.close();
				server.socket().close();
				server.close();
			} catch (Exception e) {
				// do nothing - server failed
			}
		}
	}
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
		}
	}
	
	
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
	public boolean inputMove(int axis, int x, int y, int player){
		// checks if spot is taken
		if (boardLines [axis] [x] [y] == 0 && player == this.player) {
			boardLines [axis] [x] [y] = player;
			notifyMove(player, axis, x, y);
			checkSquare(axis, x, y, player);
			notifyScore(this.player, score[this.player - 1]);
			isGameWon();
			movesLeft -= 1;
			if (movesLeft < 1) {
				switchPlayer();
				movesLeft = 1;
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	private void isGameWon() {
		int[] playerScores = new int[players + 1];
		// Checks all squares then displays the owner.
		for (int x = 0; x < boardSquares.length; x++) {
			for (int y = 0; y < boardSquares[x].length; y++) {
				playerScores[boardSquares[x][y]] += 1;
			}
		}
		int max = 0;
		for (int i = 1; i < playerScores.length; i++) {
			if (playerScores[i] > playerScores[max]) {
				max = i;
			}
		}
		for (int i = 1; i < playerScores.length; i++) {
			if (playerScores[i] > playerScores[max]) {
				max = i;
			}
		}
		for (int i = 1; i < playerScores.length; i++) {
			if (playerScores[i] > (boardSize * boardSize) / (players) ) {
				notifyEndGame(i);
			}
		}
	}
	public void checkSquare(int type, int x, int y, int player) {
		if (type == X_AXIS) {
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
		if (type == Y_AXIS) {
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
		if (args.length == 3) {
			boardSizeL = Integer.parseInt(args[0]);
			playersL = Integer.parseInt(args[1]);
			
		}
		if (playersL >= 6 || boardSizeL > 15){
			JOptionPane.showMessageDialog(null, "comand-line argument(s) invalid", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}else {
			Server s = new Server(boardSizeL, playersL);
		}
	}
}
