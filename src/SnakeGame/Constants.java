package SnakeGame;

public final class Constants {
	// Board size and configure
	public static final int B_WIDTH = 20;
	public static final int B_HEIGHT = 20;
	public static final int IMG_SIZE = 10;// in px
	public static final int MARGIN_SIZE = 10;// in px
	
	// Timer and Snake Constants
	public static final int DELAY = 10;
	public static final int MAX_BODY_LEN = 400;
	public static enum Direction {none, up, down, left, right};
	
	// 0 - space, 1 - snake, 2 - food, 4 - path
	public static final byte SNAKE = 1;
	public static final byte FOOD = 2;
	public static final byte PATH = 4;
	public static final byte LPATH = 8;
}
