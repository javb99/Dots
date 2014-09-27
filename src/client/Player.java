package client;

public interface Player {

	/**
	 * Tells you who's turn it is.
	 * @param player: The id of the player who's turn it is.
	 */
	public void turn(int player);
	
	/**
	 * Tells that this line is now owned.
	 * @param player: The id of the player that now owns this line.
	 * @param axis: The axis the line was placed on.
	 * x_axis = 0
	 * y_axis = 1
	 * @param x: The X coordinate the line was placed on.
	 * @param y: The Y coordinate the line was placed on.
	 */
	public void move(int player, int axis, int x, int y);
	
	/**
	 * Tells that this square is now owned.
	 * @param player: the id of the player who completed the square.
	 * @param x: The X coordinate the square that was completed.
	 * @param y: The Y coordinate the square that was completed.
	 */
	public void square(int player, int x, int y);
	
}
