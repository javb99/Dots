package client;

import utilities.Constants;

public class JamesComputer extends ComputerPlayer {

	JamesComputer(BoardController controller) {
		super(controller);
	}

	@Override
	public void turn(int player) {
		
		if(player == boardController.getPlayerNumber()) {
			int boardSize = boardController.getBoardSize();
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			findMove:
			for(int axis=Constants.X_AXIS; axis<=Constants.Y_AXIS; ++axis) {
				int xSize = axis == Constants.Y_AXIS ? boardSize+1 : boardSize;
				for(int x=0; x<xSize; ++x) {
					int ySize = axis == Constants.X_AXIS ? boardSize+1 : boardSize;
					for(int y=0; y<ySize; ++y) {
						int owner = boardController.getOwnerLine(axis, x, y);
						if(owner == 0) {
							boardController.playLine(axis, x, y);
							break findMove;
						}
					}
				}
			}
		}
	}
}
