package client;

public interface IDisplay extends Player {
	
	/**
	 * Called directly after connection to the server is made.
	 */
	public void connected();
	
	/**
	 * Called when the game is set up and ready to go.
	 * After this point the server is ready to accept moves.
	 * @param numberOfPlayers: The number of players this game win be played with.
	 * @param boardSize: The length and width of the board in squares.
	 * @param myID: Your unique player id.
	 */
	public void gameStarting(int numberOfPlayers, int boardSize, int myID);
	
	/**
	 * game over.
	 * @param winner: id of the winning player.
	 */
	public void gameOver(int winner);
}
