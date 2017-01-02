package SnakeGame;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static SnakeGame.Constants.*;

public class SnakePlayer {
	private int m_width;
	private int m_height;
	private byte[][] m_gameBoard;
	private Point m_head;
	private Point m_food;
	
	public SnakePlayer(int width, int height) {
		m_width = width;
		m_height = height;
	}
	
	public void loadInBoard(byte [][] currGameBoard, Point head, Point food) {
		m_gameBoard = currGameBoard;
		m_head = head;
		m_food = food;
		clearPath();
	}
	private void clearPath() {
        for (int i = 0; i < m_height; ++ i) {
        	for (int j = 0; j < m_width; ++ j) {
        		if ((m_gameBoard[i][j] & PATH) > 0) {
        			m_gameBoard[i][j] &= ~PATH;
        		}
        	}
        }
    }
	
	// bfs
	public void findShortestPath() {
		int x0 = m_head.x, y0 = m_head.y;
		int [][] map = new int[m_height][m_width];
		boolean [][] seen = new boolean[m_height][m_width];
		for (int i = 0; i < m_height; ++ i) {
			Arrays.fill(map[i], Integer.MAX_VALUE);
		}
		if (!pntInRange(map, y0, x0)) return;
		map[y0][x0] = 0;
		
		Queue<Point> q = new LinkedList<>();
		q.offer(new Point(m_head));
		int d = 0;
		boolean found = false;
		
		while (!q.isEmpty() && !found) {
			int size = q.size();
			for (int i = 0; i < size; ++ i) {
				Point currP = q.poll();
				int i0 = currP.y;
				int j0 = currP.x;
				map[i0][j0] = d;
				
				if (validP(map, seen, i0 + 1, j0))
					found |= processP(map, seen, q, i0 + 1, j0, d);
				if (validP(map, seen, i0 - 1, j0))
					found |= processP(map, seen, q, i0 - 1, j0, d);
				if (validP(map, seen, i0, j0 + 1))
					found |= processP(map, seen, q, i0, j0 + 1, d);
				if (validP(map, seen, i0, j0 - 1))
					found |= processP(map, seen, q, i0, j0 - 1, d);
			}
			++ d;
			//System.out.println("[Info] SnakePlayer:findShortestPath() " + q.size());
		}
		//System.out.println("[Info] SnakePlayer:findShortestPath() " + d);
		
		if (found)
			calculateThePath(map);
	}
	
	public byte[][] getBoardWithPath() {
		return m_gameBoard;
	}
	
	public Direction getDirection(Direction currDirection) {
		//m_head
		int i0 = m_head.y, j0 = m_head.x;
		if (pntInRange(m_gameBoard, i0 - 1, j0) && (m_gameBoard[i0 - 1][j0] & PATH) > 0) {
			return Direction.down;
		} else if (pntInRange(m_gameBoard, i0 + 1, j0) && (m_gameBoard[i0 + 1][j0] & PATH) > 0) {
			return Direction.up;
		} else if (pntInRange(m_gameBoard, i0, j0 - 1) && (m_gameBoard[i0][j0 - 1] & PATH) > 0) {
			return Direction.left;
		} else if (pntInRange(m_gameBoard, i0, j0 + 1) && (m_gameBoard[i0][j0 + 1] & PATH) > 0) {
			return Direction.right;
		}
		
		return Direction.none;
	}
	
	
	private boolean pntInRange(int [][] map, int i, int j) {
		return i >= 0 && i < m_height && j >= 0 && j < m_width;
	}
	private boolean pntInRange(byte [][] map, int i, int j) {
		return i >= 0 && i < m_height && j >= 0 && j < m_width;
	}
	private boolean validP(int [][] map, boolean [][] seen, int i, int j) {
		return pntInRange(map, i, j) && ((m_gameBoard[i][j] & SNAKE) == 0) && !seen[i][j];
	}
	private boolean processP(int [][] map, boolean [][] seen, Queue<Point> q, int i, int j, int dist) {
		if (m_food.y == i && m_food.x == j) {
			map[i][j] = dist + 1;
			return true;
		}
		
		q.offer(new Point(j, i));
		seen[i][j] = true;
		return false;
	}
	private void calculateThePath(int [][] map) {
		int i0 = m_food.y, j0 = m_food.x;
		int dist = map[i0][j0];
		m_gameBoard[i0][j0] |= PATH;
		do {
			boolean found = false;
			if (pntInRange(map, i0 - 1, j0) && map[i0 - 1][j0] == dist - 1) {
				dist --;
				i0 --;
				found = true;
			} else if (pntInRange(map, i0 + 1, j0) && map[i0 + 1][j0] == dist - 1) {
				dist --;
				i0 ++;
				found = true;
			} else if (pntInRange(map, i0, j0 - 1) && map[i0][j0 - 1] == dist - 1) {
				dist --;
				j0 --;
				found = true;
			} else if (pntInRange(map, i0, j0 + 1) && map[i0][j0 + 1] == dist - 1) {
				dist --;
				j0 ++;
				found = true;
			}
			
			if (!found) {
				System.out.println("[Error] SnakePlayer: calculateThePath() Counld not find path!");
				break;
			}
			m_gameBoard[i0][j0] |= PATH;
		} while (j0 != m_head.x || i0 != m_head.y);
	}
	
	private void printMap (int [][] map) {
		for (int i = 0; i < map.length; ++ i) {
			System.out.println(" ");
			System.out.println(" ");
			for (int j = 0; j < map[0].length; ++ j) {
				if (map[i][j] == Integer.MAX_VALUE) System.out.print("            *");
				else System.out.print("           " + map[i][j]);
			}
		}
	}
}
