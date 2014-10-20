package utilities;

public class Constants {

	public static final int X_AXIS = 0;
	public static final int Y_AXIS = 1;

	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;

	public static final String TITLE_CARD_NAME = "Score Card";
	public static final String BOARD_CARD_NAME = "Board Card";
	public static final String SINGLE_PLAYER_CARD_NAME = "Single Player Card";
	public static final String MULTI_PLAYER_CARD_NAME = "Multi Player Card";

	public static final String START_SINGLE_PLAYER = "start single player";
	public static final String CONNECT_TO_SERVER = "connect to server";
	public static final String GO_TO_SINGLE_PLAYER = "single player";
	public static final String GO_TO_MULTI_PLAYER = "multi player";
	public static final String GO_TO_TITLE = "title screen";
	public static final String IP_CHANGE = "ip change";
	public static final String PORT_CHANGE = "port change";

	public static final String[] validComputerNames = new String[] {"Human", "James", "Joe"};
	public static int LOCAL_PORT = 25566;
	public static int PORT = 65001; //must be higher than 1024 to work on OSX
	public static String IP = "24.21.64.51";
}
