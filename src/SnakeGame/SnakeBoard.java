package SnakeGame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.awt.Point;

import javax.swing.JPanel;
import javax.swing.Timer;

import static SnakeGame.Constants.*;

public class SnakeBoard extends JPanel implements ActionListener {
    
    private Timer m_timer;
    private boolean m_paused;
	private boolean m_showGrid = true;
	private SnakePlayer m_player;

    private int m_bodyLen;
    
    // 0 - space, 1 - snake, 2 - food, 4 - path
    byte [][] m_gameBoard;
    Point m_head, m_tail, m_food;
    Queue <Point> m_body;
    Direction m_currD;
    
    private boolean m_aiON;
    
    public SnakeBoard() {
    	m_player = new SnakePlayer(B_WIDTH, B_HEIGHT);
    			
        addKeyListener(new GameKeyAdapter());
        setBackground(Color.white);
        setFocusable(true);

        setPreferredSize(new Dimension(B_WIDTH * IMG_SIZE + 2 * MARGIN_SIZE, 
        		                       B_HEIGHT * IMG_SIZE + 2 * MARGIN_SIZE));
        initGame();
        m_timer = new Timer(DELAY, this);
        m_timer.start();
    }

    
    private void initGame() {
    	m_paused = true;
    	m_aiON = true;
    	m_body = new LinkedList<Point>(); 
    	m_bodyLen = 3;
    	m_gameBoard = new byte [B_HEIGHT][B_WIDTH];
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2] |= SNAKE;
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2 - 1] |= SNAKE;
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2 - 2] |= SNAKE;
    	
    	m_body.offer(new Point(B_WIDTH/2 - 2, B_HEIGHT/2));
    	m_body.offer(new Point(B_WIDTH/2 - 1, B_HEIGHT/2));
    	m_body.offer(new Point(B_WIDTH/2, B_HEIGHT/2));
    	
    	m_head = new Point(B_WIDTH/2, B_HEIGHT/2);
    	m_tail = new Point(B_WIDTH/2 - 2, B_HEIGHT/2);
    	m_currD = Direction.right;
        locateFood();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (m_showGrid) {
	        for (int i = 0; i <= B_HEIGHT*IMG_SIZE; i += 10) {
	            g.drawLine(MARGIN_SIZE, i + MARGIN_SIZE, B_WIDTH*IMG_SIZE + MARGIN_SIZE, i + MARGIN_SIZE);
	        }
	        for (int i = 0; i <= B_WIDTH*IMG_SIZE; i += 10) {
	            g.drawLine(i + MARGIN_SIZE, MARGIN_SIZE, i + MARGIN_SIZE, B_HEIGHT*IMG_SIZE + MARGIN_SIZE);
	        }
        }
        
        for (int i = 0; i < B_HEIGHT; ++ i) {
        	for (int j = 0; j < B_WIDTH; ++ j) {
        		if ((m_gameBoard[i][j] & SNAKE) > 0) {
        			if (i == m_head.y && j == m_head.x)
        				fillCell(g, j, i, Color.black);
        			else if (i == m_tail.y && j == m_tail.x)
        				fillCell(g, j, i, Color.magenta);
        			else 
        				fillCell(g, j, i, Color.GRAY);
        			
        		} else if ((m_gameBoard[i][j] & FOOD) > 0) {
        			fillCell(g, j, i, Color.green);
        		} else if ((m_gameBoard[i][j] & PATH) > 0) {
        			fillCell(g, j, i, Color.yellow);
        		} else if ((m_gameBoard[i][j] & LPATH) > 0) {
        			fillCell(g, j, i, Color.orange);
        		} else {
        			fillCell(g, j, i, Color.white);
        		}
        	}
        }
        Toolkit.getDefaultToolkit().sync();
        
    }
    private void fillCell(Graphics g, int x, int y, Color c) {
    	g.setColor(c);
    	int t = 0;
    	if (m_showGrid) t = 1;
        g.fillRect(x * IMG_SIZE + MARGIN_SIZE + t, y * IMG_SIZE + MARGIN_SIZE + t, 10 - t, 10 - t);
    }

	
	private void locateFood() {
		int t_x, t_y;
		do {
			t_x = (int) (Math.random() * B_WIDTH);
			t_y = (int) (Math.random() * B_HEIGHT);
		} while (t_x >= B_WIDTH || t_y >= B_HEIGHT || (t_x == m_head.x && t_y == m_head.y) ||
				     (m_gameBoard[t_y][t_x] != 0 && (m_gameBoard[t_y][t_x] & PATH) == 0));
		m_food = new Point(t_x, t_y);
		m_gameBoard[t_y][t_x] |= FOOD;
	}
	
    private boolean checkFood() {
        if ((m_gameBoard[m_head.y][m_head.x] & FOOD) > 0) {
            m_bodyLen++;
            if (m_bodyLen >= MAX_BODY_LEN) {
            	m_paused = true;
            	System.out.println("MAX_LEN: " + MAX_BODY_LEN + " reached!");
            } else {
            	m_gameBoard[m_head.y][m_head.x] &= ~FOOD;
            	locateFood();
            }
            return true;
        }
        return false;
    }
    private boolean willCollide() {
    	if (m_head.y < 0 || m_head.y >= B_HEIGHT || m_head.x < 0 || m_head.x >= B_WIDTH) {
    		System.out.println("[Info] SnakeBoard.willCollide(): Collision (1)!");
    		return true;
    	}
    	
    	if ((m_gameBoard[m_head.y][m_head.x] & SNAKE) > 0)
    		System.out.println("[Info] SnakeBoard.willCollide(): Collision (2)!");
    	return (m_gameBoard[m_head.y][m_head.x] & SNAKE) > 0;
    }
    private void move() {
    	
        if (m_currD == Direction.right) m_head.x ++;
        else if (m_currD == Direction.left) m_head.x--;
        else if (m_currD == Direction.up) m_head.y ++;
        else if (m_currD == Direction.down) m_head.y --;
        
        if (willCollide()) {
        	m_paused = true;
        	//initGame();
        } else {
        	checkFood();
	        
        	if (m_bodyLen <= m_body.size()) {
            	Point tail = m_body.poll();
            	m_gameBoard[tail.y][tail.x] &= ~SNAKE;
            	m_tail = m_body.peek();
            }
        	m_body.offer(new Point(m_head.x, m_head.y));
        	m_gameBoard[m_head.y][m_head.x] |= SNAKE;
        }
    }
    
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!m_paused) {
            move();
            if (m_aiON) {
            	if (m_player.needReload()) {
            		m_player.loadInBoard(m_gameBoard, m_body, m_food);
            		m_player.findPath();
            		m_gameBoard = m_player.getBoardWithPath();
            	}
	            Direction sugg = m_player.getDirection();
	            if (sugg != Direction.none) {
	            	m_currD = sugg;
	            }
            }
        }
        repaint();
	}
	
	private class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();
            if (key == KeyEvent.VK_ENTER)
            	m_paused = false;
            else if (key == KeyEvent.VK_ESCAPE)
            	m_paused = true;
            else if (key == KeyEvent.VK_G)
            	m_showGrid = !m_showGrid;
            else if (key == KeyEvent.VK_R)
            	initGame();
            else if (key == KeyEvent.VK_O)
            	m_aiON = !m_aiON;
            else {
	            if ((key == KeyEvent.VK_LEFT) && (m_currD != Direction.right))
	            	m_currD = Direction.left;
	
	            if ((key == KeyEvent.VK_RIGHT) && (m_currD != Direction.left))
	            	m_currD = Direction.right;
	
	            if ((key == KeyEvent.VK_UP) && (m_currD != Direction.up))
	            	m_currD = Direction.down;
	            	
	            if ((key == KeyEvent.VK_DOWN) && (m_currD != Direction.down))
	            	m_currD = Direction.up;
            }
        }
    }
}
