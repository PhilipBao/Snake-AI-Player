package SnakeGame;
import java.awt.EventQueue;
import javax.swing.JFrame;

public class SnakeGame extends JFrame {
	public SnakeGame() {
        super.add(new SnakeBoard());
        setResizable(false);
        pack();
        
        setTitle("Snake Game");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {                
                JFrame ex = new SnakeGame();
                ex.setVisible(true);                
            }
        });
	}

}
