package client;

public interface BoardController {

	/*
	 * Returns the score of the given player.
	 */
	public int getScore(int player);
	
	/* 
	 * Returns the player number of your player.
	 */
	public int getPlayerNumber();

	/* 
	 * Returns the total number of players.
	 */
	public int getPlayerCount();

	/* 
	 * Returns the size of the board.
	 */
	public int getBoardSize();
	
	/*
	 * Returns the owner of the given line on the given axis.
	 */
	public int getOwnerLine(int axis, int x, int y);
	
	/*
	 * Returns the owner of the given square.
	 */
	public int getOwnerSquare(int x, int y);
	
	/*
	 * Plays at the given line location.
	 */
	public void playLine(int axis, int x, int y);
}
