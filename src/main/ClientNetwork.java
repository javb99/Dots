package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ClientNetwork implements Runnable{
	
	public static final int boardSize = 10;
	// network related
	private Thread runner;
	public Socket server;
	private Socket client;
	private PrintWriter out;
	// game related
	private boolean gameStarted;
	private int[][][] boardLines;
	private int[][] boardSquares;
	private int[] scores;
	private int winner;
	private int players;
	private int player;
	// display related
	public IDisplay display;
	
	
	
	public ClientNetwork(IDisplay display) {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
		this.display = display;
	}
	
	@Override
	public void run() {
		try {
			Selector selector = Selector.open();
			SocketChannel sockChannel = SocketChannel.open(new InetSocketAddress("localhost", 1010));
			Socket client = sockChannel.socket();
			BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(server.getOutputStream());
			display.connected();
			while(true) {
				selector.select(1);
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) { 
					SelectionKey key = iter.next(); 
					iter.remove(); 
					if (key.isReadable()) { 
						readInput(input);
					} 
				}
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
	public void sendLine(int player, int axis, int x, int y) {
		out.println("line " + player + "_" + axis + "_" + x + "_" + y);
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
				gameStarted = true;
				display.gameStarting(players);
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
	
	public int getScore(int player) {
		return scores[player];
	}
}
