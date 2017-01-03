package SnakeGame;

import java.awt.Point;
import java.util.List;

import SnakeGame.Constants.Direction;

public final class Utils {
	public static void printMap (int [][] map) {
		System.out.println(" ");
		for (int i = 0; i < map.length; ++ i) {
			System.out.println(" ");
			System.out.println(" ");
			for (int j = 0; j < map[0].length; ++ j) {
				if (map[i][j] == Integer.MAX_VALUE) System.out.print("            *");
				else System.out.print("           " + map[i][j]);
			}
		}
		System.out.println(" ");
	}
	public static void printMap (byte [][] map) {
		System.out.println(" ");
		for (int i = 0; i < map.length; ++ i) {
			System.out.println(" ");
			System.out.println(" ");
			for (int j = 0; j < map[0].length; ++ j) {
				if (map[i][j] == Integer.MAX_VALUE) System.out.print("            *");
				else System.out.print("           " + map[i][j]);
			}
		}
		System.out.println(" ");
	}
	public static void printMap (Point [][] map) {
		for (int i = 0; i < map.length; ++ i) {
			System.out.println(" ");
			System.out.println(" ");
			for (int j = 0; j < map[0].length; ++ j) {
				if (map[i][j] == null) System.out.print("                       *          ");
				else System.out.print("           " + map[i][j]);
			}
		}
	}

	public static void printDirStack(List<Direction> dirQ) {
		System.out.println(" ");
		for (Direction d : dirQ) {
			switch (d) {
			case up:
				System.out.print("up");
				break;
			case down:
				System.out.print("down");
				break;
			case left:
				System.out.print("left");
				break;
			case right:
				System.out.print("right");
				break;
			case none:
				System.out.print("none");
				break;
			}
			System.out.print(" ");
		}
		System.out.println(" ");
	}
	
	public static byte [][] copy2dArr(byte [][] in) {
		byte [][] out = new byte[in.length][];
		for(int i = 0; i < in.length; i++)
		    out[i] = in[i].clone();
		return out;
	}
}
