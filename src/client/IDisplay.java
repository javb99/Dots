package client;

public interface IDisplay {
	
	public void connected();
	
	public void gameStarting(int numberOfPlayers, int boardSize, int myID);
	
	public void turn(int player);
	
	public void move(int player, int axis, int x, int y);
	
	public void square(int player, int x, int y);
	
	public void gameOver(int winner);
}
