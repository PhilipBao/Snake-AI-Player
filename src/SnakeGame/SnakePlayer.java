package SnakeGame;

import static SnakeGame.Constants.*;
import static SnakeGame.Utils.*;

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
	private Queue<Point> m_snake;
	
	private Stack <Direction> m_path;
	
	private byte m_pathCnter;
	
	public SnakePlayer(int width, int height) {
		m_width = width;
		m_height = height;
		m_path = new Stack<Direction>();
	}
	
	public boolean needReload () {
		return m_path.isEmpty();
	}
	
	public void loadInBoard(byte [][] currGameBoard, Queue<Point> snake, Point food) {
		m_gameBoard = currGameBoard;
		m_snake = snake;
		m_head = ((LinkedList<Point>) snake).get(snake.size() - 1);
		m_food = food;
		clearPath(CLEAR_ALL);
		m_path.clear();
	}
	
	public byte[][] getBoardWithPath() {
		return m_gameBoard;
	}
	
	public Direction getDirection() {
		clearPath(CLEAR_OVERLAP_ONLY);
		if (m_path.isEmpty())
			return Direction.none;
		else 
			return m_path.pop();
	}
	
	public void findPath() {
		Point [][] vecMapS = findShortestPath(m_head, m_food, m_gameBoard);
		
		Stack <Direction> shortPath = constructPath(m_head, m_food, vecMapS, m_gameBoard, HIDE_PATH);
		if (!shortPath.isEmpty()) {
			byte [][] tmpGB =  copy2dArr(m_gameBoard);
			Point tmp_food = new Point();
			moveAlongPath(m_snake, m_food, tmpGB, shortPath, tmp_food);
			findLongestPath(m_food, tmp_food, tmpGB, NO_COLLISION);
			if (m_pathCnter > 0) {
				constructPath(m_head, m_food, vecMapS, m_gameBoard, SHOW_PATH);
				m_path = shortPath;
				return;
			}
		}
		
		Point [][] vecMapL = findLongestPath(m_head, m_food, m_gameBoard, NO_COLLISION);
		Stack <Direction> longPath = constructPath(m_head, m_food, vecMapL, m_gameBoard, HIDE_PATH);
		if (m_pathCnter > 0) {
			constructPath(m_head, m_food, vecMapL, m_gameBoard, SHOW_PATH);
			m_path = longPath;
			return;
		}
		for (int i = 0; i < 10; ++ i) System.out.println("GAME OVER");
		Point [][] vecMapTry = findLongestPath(m_head, m_food, m_gameBoard, WITH_COLLISION);
		m_path = constructPath(m_head, m_food, vecMapTry, m_gameBoard, HIDE_PATH);
	}
	
	private void moveAlongPath(Queue<Point> snake, Point food, byte[][] gameBoard, Stack <Direction> path, Point foodLoc) {
		int snakeLen = snake.size();
		Point p = new Point(food);
		List <Direction> pathList = path;
		List <Point> snakeList = (List<Point>) snake;
		for (int i = 0; i < snakeLen; ++ i) {
			
			gameBoard[p.y][p.x] = PLACE_HOLDER;
			if (i < path.size()) {
				
				Direction prev = pathList.get(i);
				if (prev == Direction.up)         p.y --;
				else if (prev == Direction.down)  p.y ++;  
				else if (prev == Direction.right)  p.x --; 
				else if (prev == Direction.left) p.x ++;  
				else {
					System.out.println("[Error] SnakePlayer.moveAlongPath(): Unexpected value!");
					return;
				}
			} else {
				
				p = snakeList.get(snakeList.size() - 1 - (i - pathList.size()));
			}
		}
		for (int i = 0; i < m_height; ++ i)
        	for (int j = 0; j < m_width; ++ j)
        		gameBoard[i][j] = (byte) ((gameBoard[i][j] == PLACE_HOLDER) ? 1 : 0);
		gameBoard[p.y][p.x] = FOOD;
		foodLoc = new Point(p);
	}
	
	// bfs
	public Point [][] findShortestPath(Point head, Point food, byte [][] gameBoard) {
		m_pathCnter = 0;
		
		int x0 = head.x, y0 = head.y;
		int d = 0;
		
		Queue<Point> q = new LinkedList<>();
		
		int [][] map = new int[m_height][m_width];
		boolean [][] seen = new boolean[m_height][m_width];
		Point [][] parent = new Point[m_height][m_width];
		
		for (int i = 0; i < m_height; ++ i) {
			Arrays.fill(map[i], Integer.MAX_VALUE);
		}
		if (!pntInRange(map, y0, x0)) {
			System.out.println("[Error] SnakePlayer.findShortestPath(): idx of head is invalid!");
			return null;
		}
		map[y0][x0] = 0;
		q.offer(head);
		
		while (!q.isEmpty() && m_pathCnter == 0) {
			int size = q.size();
			for (int i = 0; i < size; ++ i) {
				Point currP = q.poll();
				int i0 = currP.y;
				int j0 = currP.x;
				map[i0][j0] = d;
				
				if (validP(map, seen, i0 + 1, j0, gameBoard, NO_COLLISION)) {
					m_pathCnter += processP(food, map, seen, q, i0 + 1, j0, d);
					parent[i0 + 1][j0] = currP;
				}
				if (validP(map, seen, i0 - 1, j0, gameBoard, NO_COLLISION)) {
					m_pathCnter += processP(food, map, seen, q, i0 - 1, j0, d);
					parent[i0 - 1][j0] = currP;
				}
				if (validP(map, seen, i0, j0 + 1, gameBoard, NO_COLLISION)) {
					m_pathCnter += processP(food, map, seen, q, i0, j0 + 1, d);
					parent[i0][j0 + 1] = currP;
				}
				if (validP(map, seen, i0, j0 - 1, gameBoard, NO_COLLISION)) {
					m_pathCnter += processP(food, map, seen, q, i0, j0 - 1, d);
					parent[i0][j0 - 1] = currP;
				}
					
			}
			++ d;
		}
		if (m_pathCnter == 0) {
			System.out.println("[Error] SnakePlayer.findShortestPath(): Counld not find the path!");
			return null;
		}
		return parent;
	}

	// dfs
	public Point [][] findLongestPath(Point head, Point food, byte [][] gameBoard, boolean enCollision) {
		m_pathCnter = 0;
		
		int x0 = head.x, y0 = head.y;
		int x1 = food.x, y1 = food.y;
		int [][] map = new int[m_height][m_width];
		boolean [][] seen = new boolean[m_height][m_width];
		Point [][] parent = new Point[m_height][m_width];
		
		for (int i = 0; i < m_height; ++ i) {
			Arrays.fill(map[i], Integer.MAX_VALUE);
		}
		if (!pntInRange(map, y0, x0)) {
			System.out.println("[Error] SnakePlayer.findLongestPath(): idx of head is invalid!");
			return null;
		}
		map[y0][x0] = getEstDist(x0, y0, x1, y1);
		
		
		dfs(head, head, food, gameBoard, map, seen, parent, enCollision);
		if (m_pathCnter == 0) {
			System.out.println("[Error] SnakePlayer.findLongestPath(): Counld not find the path!");
			return null;
		}
		return parent;
	}
	
	private void dfs(Point origin, Point from, Point to, byte[][] gb, int [][] map, boolean [][] seen, Point [][] parent, boolean enCollision) {
		if (m_pathCnter > 0) return;
		
		seen[from.y][from.x] = true;
		if (from.x == to.x && from.y == to.y) {
			m_pathCnter ++;
		} else {
			List<Point> adjPnts = getAdjPnts(from, seen, map, gb, enCollision);
			if (adjPnts.isEmpty()) return;
			for (Point p : adjPnts) {
				map[p.y][p.x] = Math.min(map[p.y][p.x], getEstDist(p.x, p.y, to.x, to.y));
			}
			Collections.sort(adjPnts, (a, b) -> {
				return map[b.y][b.x] - map[a.y][a.x];
			});

			for (Point p : adjPnts) {
				if (!seen[p.y][p.x]) {
					parent[p.y][p.x] = from;
					dfs(origin, p, to, gb, map, seen, parent, enCollision);
				}
			}
		}
	}

	private Stack<Direction> constructPath(Point from, Point to, Point[][] parent, byte [][] gameBoard, boolean showPath) {
		Stack<Direction> res_path = new Stack<Direction>();
		if (parent == null) {
			System.out.println("[Error] SnakePlayer: constructPath() Parent vector field is null");
			return res_path;
		}
		Point p2 = to;
		Point tmp = null;
		
		do {
			if (p2 != null)
				tmp = parent[p2.y][p2.x];
			if (p2 == null || tmp == null || getEstDist(tmp, p2) != 1) {
				if (p2 == null)
					System.out.println("[Error] SnakePlayer: constructPath() No Path Found p2 is null");
				if (tmp == null)
					System.out.println("[Error] SnakePlayer: constructPath() No Path Found tmp is null");
				if (getEstDist(tmp, p2) != 1)
					System.out.println("[Error] SnakePlayer: constructPath() No Path Found tmp is not adj to p2");
				break;
			}

			Direction tmpD = Direction.none;
			if (tmp.x - p2.x == 1)
				tmpD = Direction.left;
			if (tmp.x - p2.x == -1)
				tmpD = Direction.right;
			if (tmp.y - p2.y == 1)
				tmpD = Direction.down;
			if (tmp.y - p2.y == -1)
				tmpD = Direction.up;

			res_path.push(tmpD);
			p2 = tmp;
			if (showPath)
				gameBoard[p2.y][p2.x] |= PATH;
		} while (p2.y != from.y || p2.x != from.x);
		return res_path;
	}

	private List<Point> getAdjPnts(Point p, boolean [][] seen, int [][] map, byte [][] gb, boolean enCollision) {
		List<Point> res = new ArrayList<Point>();
		if (validP(map, seen, p.y + 1, p.x, gb, enCollision)) 
			res.add(new Point(p.x, p.y + 1));
		if (validP(map, seen, p.y - 1, p.x, gb, enCollision)) 
			res.add(new Point(p.x, p.y - 1));
		if (validP(map, seen, p.y, p.x + 1, gb, enCollision)) 
			res.add(new Point(p.x + 1, p.y));
		if (validP(map, seen, p.y, p.x - 1, gb, enCollision)) 
			res.add(new Point(p.x - 1, p.y));
		return res;
	}
	private int getEstDist(int x0, int y0, int x1, int y1) {
		return Math.abs(x1 - x0) + Math.abs(y1 - y0);
	}
	private int getEstDist(Point p1, Point p2) {
		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
	}

	
	private byte processP(Point food, int [][] map, boolean [][] seen, Queue<Point> q, int i, int j, int dist) {
		if (food.y == i && food.x == j) {
			map[i][j] = dist + 1;
			return 1;
		}
		q.offer(new Point(j, i));
		seen[i][j] = true;
		return 0;
	}
	private boolean pntInRange(int [][] map, int i, int j) {
		return i >= 0 && i < m_height && j >= 0 && j < m_width;
	}
	private boolean validP(int [][] map, boolean [][] seen, int i, int j, byte [][] gameBoard, boolean enCollision) {
		return pntInRange(map, i, j) && (enCollision || (gameBoard[i][j] & SNAKE) == 0) && !seen[i][j];
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
}
