package client;

public class ComputerPlayer implements Player {

	protected BoardController boardController;
	
	ComputerPlayer(BoardController controller) {
		boardController = controller;
	}
	
	@Override
	public void turn(int player) {

	}

	@Override
	public void move(int player, int axis, int x, int y) {

	}

	@Override
	public void square(int player, int x, int y) {
	
	}

}
