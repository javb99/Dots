package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import javax.swing.JOptionPane;

public class Server {
	// gameplay related
	public static final int X_AXIS = 0;
	public static final int Y_AXIS = 1;
	public int boardSize;
	public int[][] boardx;
	public int[][] boardy;
	public int[][] board;
	public int[] score;
	public int players;
	public int player;
	public int movesLeft;
	// server related
	public static final int PORT = 1010;
	public Selector selector;
	public ServerSocketChannel server;
	public int numberClients;
	HashMap<String, Socket> clients;
	
	public Server(int boardSize, int players) {
		this.boardSize = boardSize;
		this.players = players;
		try { 
			selector = Selector.open(); 
			server = ServerSocketChannel.open(); 
			server.socket().bind(new InetSocketAddress(PORT)); 
			server.configureBlocking(false); 
			server.register(selector, SelectionKey.OP_ACCEPT, "Main accept server"); 
			while (true) {
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) { 
					SelectionKey key = iter.next(); 
					iter.remove(); 
					if (key.isConnectable()) { 
						((SocketChannel)key.channel()).finishConnect(); 
					} 
					if (key.isAcceptable()) { 
						// accept connection 
						SocketChannel client = server.accept(); 
						client.configureBlocking(false); 
						client.socket().setTcpNoDelay(true); 
						String clientKey = "client"+numberClients;
						client.register(selector, SelectionKey.OP_READ, clientKey);
						clients.put(clientKey, client.socket());
					} 
					if (key.isReadable()) { 
						Socket sock = clients.get((String) key.attachment());
						BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
						readInput(reader);
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
			PrintWriter pw = new PrintWriter( new BufferedOutputStream(connection.getOutputStream() ), false );
			char lengthChar = (char) message.length();
			pw.write(lengthChar + message);
			pw.flush();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void readInput(BufferedReader reader) {
		try {
			int length = reader.read();
			char[] message = new char[length];
			reader.read(message);
			String line = new String(message);
			String[] commands = line.split(" ");
			// evals to true is the client is trying to make a move.
			if (commands[0] == "line") {
				String[] lineParams = commands[1].split("_");
				int player = Integer.parseInt(lineParams[0]);
				int axis = Integer.parseInt(lineParams[1]);
				int x = Integer.parseInt(lineParams[2]);
				int y = Integer.parseInt(lineParams[3]);
				if( player == this.player) {
					inputMove(axis,x,y,player);
				}
			} else if (false) {
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void setupGame() {
		boardx = new int[boardSize][boardSize +2];
		boardy = new int[boardSize +2][boardSize];
		board = new int[boardSize][boardSize];
		score = new int[players];
		player = 0;
		movesLeft = 1;
		switchPlayer();
	}
	public void inputMove(int axis, int x, int y, int player){
		if (axis == Y_AXIS){
			// checks if spot is taken
			if (boardy[x] [y] == 0) {
				// clicked on the West.
				boardy[x] [y] = player;
				checkSquare(Y_AXIS, x, y, player);
				movesLeft -= 1;
				if (movesLeft < 1) {
					switchPlayer();
					movesLeft = 1;
				}
			}
		} else if (axis == X_AXIS) {
			// checks if spot is taken
			if (boardx[x] [y] == 0) {
				// clicked on the North
				boardx[x] [y] = player;
				checkSquare(X_AXIS, x, y, player);
				movesLeft -= 1;
				if (movesLeft < 1) {
					switchPlayer();
					movesLeft = 1;
				}
			}
		}
	}
	
	public void checkSquare(int type, int x, int y, int player) {
		if (type == X_AXIS) {
			// if checking from top
			if (y < boardSize &&  boardx[x][y] > 0 && boardx[x][y +1] > 0 && boardy[x][y] > 0 && boardy[x +1][y] > 0) {
				board[x] [y] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from bottom
			if (y > 0 && boardx[x][y] > 0 && boardx[x][y -1] > 0 && boardy[x][y -1] > 0 && boardy[x +1][y -1] > 0) {
				board[x] [y-1] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
		}
		if (type == Y_AXIS) {
			// if checking from left
			if (x < boardSize && boardy[x][y] > 0 && boardy[x +1][y] > 0 && boardx[x][y] > 0 && boardx[x][y +1] > 0) {
				board[x] [y] = player;
				movesLeft += 1;
				score[player - 1] += 1;
			}
			// if checking from right
			if (x > 0 && boardy[x][y] > 0 && boardy[x -1][y] > 0 && boardx[x -1][y] > 0 && boardx[x -1][y +1] > 0) {
				board[x-1] [y] = player;
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
		// TODO: send message informing clients of turn change.
	}
	public static void main(String[] args) {
		int boardSizeL = 10;
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
