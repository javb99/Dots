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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for(int axis=Constants.X_AXIS; axis<=Constants.Y_AXIS; ++axis) {
				for(int x=0; x<boardSize; ++x) {
					for(int y=0; y<boardSize; ++y) {
						int owner = boardController.getOwnerLine(axis, x, y);
						if(owner == 0) {
							boardController.playLine(axis, x, y);
							return;
						}
					}
				}
			}
		}
	}
}
