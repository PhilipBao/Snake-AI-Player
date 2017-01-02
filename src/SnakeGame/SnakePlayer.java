package SnakeGame;

import static SnakeGame.Constants.PATH;
import static SnakeGame.Constants.SNAKE;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import SnakeGame.Constants.Direction;

public class SnakePlayer {
	private int m_width;
	private int m_height;
	private byte[][] m_gameBoard;
	private Point m_head;
	private Point m_food;
	
	private int[][] m_map;
	
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
	
	public byte[][] getBoardWithPath() {
		return m_gameBoard;
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
		if (found)
			calculateThePath(m_map);
		if (!found)
			System.out.println("[Error] SnakePlayer.findShortestPath() Counld not find the path!");
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
		dfs(new Point(m_head), new Point(m_food), seen, new LinkedList<> (), parent);
		
	}
	private void dfs(Point from, Point to, boolean [][] seen, Queue<Direction> path, Point [][] parent) {
		if (!path.isEmpty()) return;
		
		seen[from.y][from.x] = true;
		if (from.x == to.x && from.y == to.y) {
			constructPath(from, to, path, parent);
		} else {
			List<Point> adjPnts = getAdjPnts(from, seen);
			if (adjPnts.isEmpty()) return;
			for (Point p : adjPnts) {
				m_map[p.y][p.x] = Math.min(m_map[p.y][p.x], getEstDist(p.x, p.y, to.x, to.y));
			}
			Collections.sort(adjPnts, new Comparator <Point>() { 
				@Override
				public int compare(Point a, Point b) {
					return m_map[b.y][b.x] - m_map[a.y][a.x];
				}
			});
			for (Point p : adjPnts) {
				if (!seen[p.y][p.x]) {
					parent[p.y][p.x] = from;
					dfs(p, to, seen, path, parent);
				}
			}
		}
	}
	private void constructPath(Point from, Point to, Queue<Direction> path, Point [][] parent) {
		Point p = from;
		Point tmp;
		while (p.y != from.y || p.x != from.x) {
			tmp = parent[p.y][p.x];
			path.offer(getDir(tmp, p));
			p = tmp;
		}
	}
	private Direction getDir(Point from, Point to) {
		if (getEstDist(from.x, from.y, to.x, to.y) != 1) return Direction.none;
		if (from.x - to.x == 1) return Direction.left;
		if (from.x - to.x == -1) return Direction.right;
		if (from.y - to.y == 1) return Direction.up;
		if (from.y - to.y == -1) return Direction.down;
		return Direction.none;
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
	private boolean pntInRange(byte [][] map, int i, int j) {
		return i >= 0 && i < m_height && j >= 0 && j < m_width;
	}
	private boolean validP(int [][] map, boolean [][] seen, int i, int j) {
		return pntInRange(map, i, j) && ((m_gameBoard[i][j] & SNAKE) == 0) && !seen[i][j];
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
