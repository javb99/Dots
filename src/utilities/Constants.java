package utilities;

import java.awt.Color;

import client.Client;

public class Constants {

	public static final int X_AXIS = 0;
	public static final int Y_AXIS = 1;

	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;

	public static final String TITLE_CARD_NAME = "Score_Card";
	public static final String BOARD_CARD_NAME = "Board_Card";
	public static final String SINGLE_PLAYER_CARD_NAME = "Single_Player_Card";
	public static final String MULTI_PLAYER_CARD_NAME = "Multi_Player_Card";

	public static final String START_SINGLE_PLAYER = "start_single_player";
	public static final String START_MULTI_PLAYER = "start_multi_player";
	public static final String GO_TO_SINGLE_PLAYER = "single_player";
	public static final String GO_TO_MULTI_PLAYER = "multi_player";
	public static final String GO_TO_TITLE = "title_screen";
	public static final String IP_CHANGE = "ip_change";
	public static final String PORT_CHANGE = "port_change";

	public static final String[] validComputerNames = new String[] {"Human", "James", "Joe"};
	public static final Color[] colors = new Color[] {Color.white, Color.red, Color.blue, Color.green, Color.orange, Color.pink, Color.cyan};
	public static final int THICKNESS = 16;
	public static final int SPACE = THICKNESS * 4;
	public static final int LINELENGTH = SPACE - THICKNESS;
	public static Client client;
	public static int LOCAL_PORT = 25566;
	public static int PORT = 65001; //must be higher than 1024 to work on OSX
	public static String IP = "24.21.64.51";
}
