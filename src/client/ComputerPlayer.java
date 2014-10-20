package client;

public abstract class ComputerPlayer implements IDisplay, Runnable {

	protected BoardController boardController;
	protected Thread runner;
	public String name;

	public ComputerPlayer(BoardController controller, String name) {
		this();
		setBoardController(controller);
		this.name = name;
	}

	public ComputerPlayer(){
		super();
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public void setBoardController(BoardController boardController) {
		this.boardController = boardController;
	}

	@Override
	public void run() {};

	@Override
	public void move(int player, int axis, int x, int y) {}
	@Override
	public void connected(int numberOfPlayers, int boardSize) {}
	@Override
	public void gameStarting(int myID) {}
	@Override
	public void spectator(int numberOfPlayers, int boardSize) {}
	@Override
	public void gameOver(int winner) {}
	@Override
	public void sessionOver(int[] scores) {}
}
