package SnakeGame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class SnakeBoard extends JPanel implements ActionListener {
    private final int B_WIDTH = 30;
    private final int B_HEIGHT = 30;
    private final int IMG_SIZE = 10;// in px
    private final int DELAY = 200;
    private enum Direction {up, down, left, right};
    
    Direction m_currD;
    
    private Timer m_timer;
    private boolean m_paused;
    
    private Image m_foodImg;
    private Image m_headImg;
    private Image m_bodyImg;
    private Image m_spaceImg;
    
    private int m_bodyLen;
    
    // 0 - space, 1 - snake, 2 - food
    byte [][] m_gameBoard;
    Point m_head;
    Queue <Point> m_body;
    
    public SnakeBoard() {
        addKeyListener(new GameKeyAdapter());
        setBackground(Color.black);
        setFocusable(true);

        setPreferredSize(new Dimension(B_WIDTH * IMG_SIZE, B_HEIGHT * IMG_SIZE));
        loadImgs();
        initGame();
        m_timer = new Timer(DELAY, this);
        m_timer.start();
    }
    
    private void loadImgs() {
        ImageIcon iif = new ImageIcon("src/SnakeGame/img/green_10px.jpg");
        m_foodImg = iif.getImage();
        ImageIcon iih = new ImageIcon("src/SnakeGame/img/black_10px.jpg");
        m_headImg = iih.getImage();
        ImageIcon iib = new ImageIcon("src/SnakeGame/img/grey_10px.jpg");
        m_bodyImg = iib.getImage();
        ImageIcon iis = new ImageIcon("src/SnakeGame/img/white_10px.jpg");
        m_spaceImg = iis.getImage();
        //System.out.println(new File(iis.toString()).exists());
    }
    
    private void initGame() {
    	//m_timer = new Timer(DELAY, this);
        //m_timer.start();
        
    	m_paused = true;
    	m_body = new LinkedList<Point>(); 
    	m_bodyLen = 3;
    	m_gameBoard = new byte [B_HEIGHT][B_WIDTH];
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2] = 1;
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2 - 1] = 1;
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2 - 2] = 1;
    	m_body.offer(new Point(B_WIDTH/2 - 2, B_HEIGHT/2));
    	m_body.offer(new Point(B_WIDTH/2 - 1, B_HEIGHT/2));
    	m_body.offer(new Point(B_WIDTH/2, B_HEIGHT/2));
    	
    	m_head = new Point(B_HEIGHT/2, B_WIDTH/2);
    	m_currD = Direction.right;
        locateFood();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //System.out.println("randered");
        for (int i = 0; i < B_HEIGHT; ++ i) {
        	for (int j = 0; j < B_WIDTH; ++ j) {
        		if (m_gameBoard[i][j] == 1) {
        			if (i == m_head.y && j == m_head.x)
        				g.drawImage(m_headImg, j * IMG_SIZE, i * IMG_SIZE, this);
        			else 
        				g.drawImage(m_bodyImg, j * IMG_SIZE, i * IMG_SIZE, this);
        		} else if (m_gameBoard[i][j] == 2) {
        			g.drawImage(m_foodImg, j * IMG_SIZE, i * IMG_SIZE, this);
        		} else {
        			g.drawImage(m_spaceImg, j * IMG_SIZE, i * IMG_SIZE, this);
        		}
        	}
        }
        Toolkit.getDefaultToolkit().sync();
    }

	
	private void locateFood() {
		int t_x, t_y;
		do {
			t_x = (int) (Math.random() * B_WIDTH);
			t_y = (int) (Math.random() * B_HEIGHT);
		} while (t_x >= B_WIDTH || t_y >= B_HEIGHT || m_gameBoard[t_y][t_x] != 0);
		m_gameBoard[t_y][t_x] = 2;
	}
	
    private void checkFood() {
        if (m_gameBoard[m_head.y][m_head.x] == 2) {
            m_bodyLen++;
            m_gameBoard[m_head.y][m_head.x] = 0;
            locateFood();
        }
    }
    private boolean willCollide() {
    	if (m_head.y < 0 || m_head.y >= B_HEIGHT || m_head.x < 0 || m_head.x >= B_WIDTH) {
    		System.out.println("Collision1!");
    		return true;
    	}
    	if (m_gameBoard[m_head.y][m_head.x] == 1)
    		System.out.println("Collision2!");
    	return m_gameBoard[m_head.y][m_head.x] == 1;
    }
    private void move() {
        if (m_currD == Direction.right) m_head.x ++;
        else if (m_currD == Direction.left) m_head.x--;
        else if (m_currD == Direction.up) m_head.y ++;
        else if (m_currD == Direction.down) m_head.y --;
        
        if (willCollide()) {
        	System.out.println("Collision!");
        	initGame();
        } else {
        	checkFood();
        	if (m_bodyLen <= m_body.size()) {
            	Point tail = m_body.poll();
            	m_gameBoard[tail.y][tail.x] = 0;
            }
        	m_body.offer(new Point(m_head.x, m_head.y));
        	m_gameBoard[m_head.y][m_head.x] = 1;
        }
    }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(m_paused);
		if (!m_paused) {
            move();
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
