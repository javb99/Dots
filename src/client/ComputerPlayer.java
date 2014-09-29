package client;

public abstract class ComputerPlayer implements Player {

	protected BoardController boardController;
	
	ComputerPlayer(BoardController controller) {
		boardController = controller;
	}
	@Override
	public void move(int player, int axis, int x, int y) {  }
}
