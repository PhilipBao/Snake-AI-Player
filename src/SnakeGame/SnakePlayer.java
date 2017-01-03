package SnakeGame;

import static SnakeGame.Constants.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import SnakeGame.Constants.Direction;

public class SnakePlayer {
	private int m_width;
	private int m_height;
	private byte[][] m_gameBoard;
	private Point m_head;
	private Point m_food;
	
	private int[][] m_map;
	private Stack <Direction> m_path;
	
	public SnakePlayer(int width, int height) {
		m_width = width;
		m_height = height;
		m_path = new Stack<Direction>();
	}
	
	public boolean needReload () {
		return m_path.isEmpty();
	}
	
	public void loadInBoard(byte [][] currGameBoard, Point head, Point food) {
		m_gameBoard = currGameBoard;
		m_head = head;
		m_food = food;
		clearPath(false);
		m_path.clear();
	}
	
	public byte[][] getBoardWithPath() {
		return m_gameBoard;
	}
	
	public Direction getDirection() {
		//System.out.println(m_path.peek());
		//printDirStack(m_path);
		clearPath(true);
		if (m_path.isEmpty())
			return Direction.none;
		else 
			return m_path.pop();
	}
	
	// bfs
	public void findShortestPath() {
		int x0 = m_head.x, y0 = m_head.y;
		m_map = new int[m_height][m_width];
		boolean [][] seen = new boolean[m_height][m_width];
		for (int i = 0; i < m_height; ++ i) {
			Arrays.fill(m_map[i], Integer.MAX_VALUE);
		}
		if (!pntInRange(m_map, y0, x0)) return;
		m_map[y0][x0] = 0;
		
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
				m_map[i0][j0] = d;
				
				if (validP(m_map, seen, i0 + 1, j0))
					found |= processP(m_map, seen, q, i0 + 1, j0, d);
				if (validP(m_map, seen, i0 - 1, j0))
					found |= processP(m_map, seen, q, i0 - 1, j0, d);
				if (validP(m_map, seen, i0, j0 + 1))
					found |= processP(m_map, seen, q, i0, j0 + 1, d);
				if (validP(m_map, seen, i0, j0 - 1))
					found |= processP(m_map, seen, q, i0, j0 - 1, d);
			}
			++ d;
		}
		if (!found)
			System.out.println("[Error] SnakePlayer.findShortestPath() Counld not find the path!");
		else
			constructPathBFS();
			
	}

	// dfs
	public void findlongestPath() {
		int x0 = m_head.x, y0 = m_head.y;
		int x1 = m_food.x, y1 = m_food.y;
		m_map = new int[m_height][m_width];
		boolean [][] seen = new boolean[m_height][m_width];
		Point [][] parent = new Point[m_height][m_width];
		for (int i = 0; i < m_height; ++ i) {
			Arrays.fill(m_map[i], Integer.MAX_VALUE);
		}
		if (!pntInRange(m_map, y0, x0)) return;
		m_map[y0][x0] = getEstDist(x0, y0, x1, y1);
		dfs(new Point(m_head), new Point(m_head), new Point(m_food), seen, parent);
	}
	
	
	
	
	
	private void dfs(Point origin, Point from, Point to, boolean [][] seen, Point [][] parent) {
		if (!m_path.isEmpty()) return;
		
		seen[from.y][from.x] = true;
		if (from.x == to.x && from.y == to.y) {
			constructPathDFS(origin, to, parent);
		} else {
			List<Point> adjPnts = getAdjPnts(from, seen);
			if (adjPnts.isEmpty()) return;
			for (Point p : adjPnts) {
				m_map[p.y][p.x] = Math.min(m_map[p.y][p.x], getEstDist(p.x, p.y, to.x, to.y));
			}
			Collections.sort(adjPnts, (a, b) -> {
				return m_map[b.y][b.x] - m_map[a.y][a.x];
			});

			for (Point p : adjPnts) {
				if (!seen[p.y][p.x]) {
					parent[p.y][p.x] = from;
					dfs(origin, p, to, seen, parent);
				}
			}
		}
	}
	private void constructPathDFS(Point from, Point to, Point [][] parent) {
		Point p2 = to;
		Point tmp;
		 do {
			tmp = parent[p2.y][p2.x];
			
			if (p2 == null || tmp == null || getEstDist(tmp, p2) != 1) {
				System.out.println("[Error] SnakePlayer: constructPathDFS() Counld not find path!");
				break;
			}
			
			Direction tmpD = Direction.none;
			if (tmp.x - p2.x == 1) tmpD = Direction.left;
			if (tmp.x - p2.x == -1) tmpD = Direction.right;
			if (tmp.y - p2.y == 1) tmpD = Direction.down;
			if (tmp.y - p2.y == -1) tmpD = Direction.up;
			
			m_path.push(tmpD);
			p2 = tmp;
			m_gameBoard[p2.y][p2.x] |= LPATH;
		} while (p2.y != from.y || p2.x != from.x);
	}

	private List<Point> getAdjPnts(Point p, boolean [][] seen) {
		List<Point> res = new ArrayList<Point>();
		if (validP(m_map, seen, p.y + 1, p.x)) res.add(new Point(p.x, p.y + 1));
		if (validP(m_map, seen, p.y - 1, p.x)) res.add(new Point(p.x, p.y - 1));
		if (validP(m_map, seen, p.y, p.x + 1)) res.add(new Point(p.x + 1, p.y));
		if (validP(m_map, seen, p.y, p.x - 1)) res.add(new Point(p.x - 1, p.y));
		return res;
	}
	private int getEstDist(int x0, int y0, int x1, int y1) {
		return Math.abs(x1 - x0) + Math.abs(y1 - y0);
	}
	private int getEstDist(Point p1, Point p2) {
		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
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
	private boolean pntInRange(int [][] map, int i, int j) {
		return i >= 0 && i < m_height && j >= 0 && j < m_width;
	}
	private boolean validP(int [][] map, boolean [][] seen, int i, int j) {
		return pntInRange(map, i, j) && ((m_gameBoard[i][j] & SNAKE) == 0) && !seen[i][j];
	}
	
	private void constructPathBFS() {
		int [][] map = m_map;
		int i0 = m_food.y, j0 = m_food.x;
		int dist = map[i0][j0];
		m_gameBoard[i0][j0] |= PATH;
		
		do {
			boolean found = false;
			if (pntInRange(map, i0 - 1, j0) && map[i0 - 1][j0] == dist - 1) {
				dist --;
				i0 --;
				found = true;
				m_path.push(Direction.up);
			} else if (pntInRange(map, i0 + 1, j0) && map[i0 + 1][j0] == dist - 1) {
				dist --;
				i0 ++;
				found = true;
				m_path.push(Direction.down);
			} else if (pntInRange(map, i0, j0 - 1) && map[i0][j0 - 1] == dist - 1) {
				dist --;
				j0 --;
				found = true;
				m_path.push(Direction.right);
			} else if (pntInRange(map, i0, j0 + 1) && map[i0][j0 + 1] == dist - 1) {
				dist --;
				j0 ++;
				found = true;
				m_path.push(Direction.left);
			}
			
			if (!found) {
				System.out.println("[Error] SnakePlayer: constructPathBFS() Counld not find path!");
				break;
			}
			m_gameBoard[i0][j0] |= PATH;
		} while (j0 != m_head.x || i0 != m_head.y);
		//printDirQueue((List<Direction>)m_path);
	}
	
	private void clearPath(boolean overlapOnly) {
        for (int i = 0; i < m_height; ++ i) {
        	for (int j = 0; j < m_width; ++ j) {
        		if ((m_gameBoard[i][j] & SNAKE) > 0 ||
        				(((m_gameBoard[i][j] & PATH) | (m_gameBoard[i][j] & LPATH)) > 0 && 
        						!overlapOnly)) {
        			m_gameBoard[i][j] &= ~PATH;
        			m_gameBoard[i][j] &= ~LPATH;
        		}
        	}
        }
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
	private void printMap (Point [][] map) {
		for (int i = 0; i < map.length; ++ i) {
			System.out.println(" ");
			System.out.println(" ");
			for (int j = 0; j < map[0].length; ++ j) {
				if (map[i][j] == null) System.out.print("                       *          ");
				else System.out.print("           " + map[i][j]);
			}
		}
	}

	private void printDirStack(List<Direction> dirQ) {
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
}
