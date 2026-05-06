import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    static final int W = 600, H = 600, SIZE = 30;
    static final int COLS = W / SIZE, ROWS = H / SIZE;

    List<Point> snake = new ArrayList<>();
    Point food, bonus, poison;
    int dx = 1, dy = 0;
    int score = 0, high = 0, delay = 130;
    float foodAlpha = 1.0f;
    int foodAlphaDir = -1;
    boolean gameStarted = false, gameOver = false, paused = false;
    javax.swing.Timer timer;
    Random rnd = new Random();
    ImageIcon foodIcon;
    Image foodImage;
    ImageIcon bonusIcon;
    Image bonusImage;
    ImageIcon poisonIcon;
    Image poisonImage;
    JButton easyBtn, mediumBtn, hardBtn;
    javax.swing.Timer alphaTimer;

    public SnakeGame() {
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(60, 80, 30));
        setLayout(null);

        easyBtn = new JButton("easy");
        mediumBtn = new JButton("normal");
        hardBtn = new JButton("hard");

        easyBtn.setBounds(W/2 - 80, H/2 - 60, 160, 40);
        mediumBtn.setBounds(W/2 - 80, H/2 - 10, 160, 40);
        hardBtn.setBounds(W/2 - 80, H/2 + 40, 160, 40);

        easyBtn.setFont(new Font("Consolas", Font.BOLD, 18));
        mediumBtn.setFont(new Font("Consolas", Font.BOLD, 18));
        hardBtn.setFont(new Font("Consolas", Font.BOLD, 18));

        easyBtn.addActionListener(e -> startGame(160));
        mediumBtn.addActionListener(e -> startGame(130));
        hardBtn.addActionListener(e -> startGame(100));

        add(easyBtn);
        add(mediumBtn);
        add(hardBtn);

        foodIcon = new ImageIcon("images/food.png");
        foodImage = foodIcon.getImage();
        bonusIcon = new ImageIcon("images/bonus.png");
        bonusImage = bonusIcon.getImage();
        poisonIcon = new ImageIcon("images/poison.png");
        poisonImage = poisonIcon.getImage();
        
        setFocusable(true);
        addKeyListener(this);
        // init();
        // timer = new javax.swing.Timer(delay, this);
        // timer.start();
        alphaTimer = new javax.swing.Timer(50, e -> updateFoodAlpha());
        alphaTimer.start();
    }

    void init() {
        snake.clear();
        for (int i = 3; i >= 0; i--) snake.add(new Point(i, 10));
        score = 0; dx = 1; dy = 0;
        gameOver = false;
        spawnFood();
        if (timer != null) timer.setDelay(delay);
    }

    void startGame(int initialDelay) {

        remove(easyBtn);
        remove(mediumBtn);
        remove(hardBtn);

        delay = initialDelay;

        if (timer != null) {
            timer.stop();
        }

        gameStarted = true;
        init();
        timer = new javax.swing.Timer(delay, this);
        timer.start();

        requestFocusInWindow();
        repaint();
    }
    
    void spawnFood() {
        do {
            food = new Point(rnd.nextInt(COLS), rnd.nextInt(ROWS));
        } while (snake.contains(food));

        if (rnd.nextInt(3) == 0) {
            do {
                bonus = new Point(rnd.nextInt(COLS), rnd.nextInt(ROWS));
            } while (snake.contains(bonus) || bonus.equals(food));  
        } else {
            bonus = null;
        }

        if (rnd.nextInt(100) < 20) {
            do {
                poison = new Point(rnd.nextInt(COLS), rnd.nextInt(ROWS));
            } while (snake.contains(poison) || poison.equals(food) || (bonus != null && poison.equals(bonus)));
        } else {
            poison = null;
        }
    }

    void updateFoodAlpha() {
        foodAlpha += foodAlphaDir * 0.05f;
        if (foodAlpha >= 1.0f) {
            foodAlpha = 1.0f;
            foodAlphaDir = -1;
        } else if (foodAlpha <= 0.3f) {
            foodAlpha = 0.3f;
            foodAlphaDir = 1;
        }
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameStarted || gameOver || paused) return;
        Point head = snake.get(0);
        Point next = new Point(head.x + dx, head.y + dy);
        if (next.x < 0 || next.x >= COLS || next.y < 0 || next.y >= ROWS
                || snake.contains(next)) {
            gameOver = true;
            if (score > high) high = score;
            repaint();
            return;
        }
        snake.add(0, next);
        if (next.equals(food)) {
            score += 10;
            if (delay > 70) { delay -= 3; timer.setDelay(delay); }
            spawnFood();
        } else if (bonus != null && next.equals(bonus)) {
            score += 30; 
            bonus = null;
        } else if (poison != null && next.equals(poison)) {
            score -= 20;
            if (score < 0) score = 0;
            poison = null;
            snake.remove(snake.size() - 1);
            if (snake.size() > 3) {
                snake.remove(snake.size() - 1);
            }
        } else {
            snake.remove(snake.size() - 1);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(30, 45, 35));
        for (int i = 0; i <= COLS; i++) g2.drawLine(i * SIZE, 0, i * SIZE, H);
        for (int i = 0; i <= ROWS; i++) g2.drawLine(0, i * SIZE, W, i * SIZE);

        for (int i = snake.size() - 1; i >= 0; i--) {
            Point p = snake.get(i);
            int shade = Math.max(50, 180 - i * 8);
            g2.setColor(new Color(30, 100, shade));
            g2.fillRoundRect(p.x * SIZE + 1, p.y * SIZE + 1,
                    SIZE - 2, SIZE - 2, 10, 10);
            if (i == 0) {
                g2.setColor(Color.WHITE);
                g2.fillOval(p.x * SIZE + 8, p.y * SIZE + 8, 6, 6);
                g2.fillOval(p.x * SIZE + 18, p.y * SIZE + 8, 6, 6);
            }
        }

        if (gameStarted) {
        // g2.setColor(new Color(230, 60, 60));
        // g2.fillOval(food.x * SIZE + 3, food.y * SIZE + 3, SIZE - 6, SIZE - 6);
        // g2.setColor(new Color(80, 160, 70));
        // g2.fillRect(food.x * SIZE + 13, food.y * SIZE + 1, 4, 6);
        if (foodImage != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, foodAlpha));
            g2.drawImage(foodImage, food.x * SIZE, food.y * SIZE, SIZE, SIZE, this);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // if (bonus != null) {
        //     g2.setColor(new Color(255, 215, 0));
        //     g2.fillOval(bonus.x * SIZE + 3, bonus.y * SIZE + 3,
        //             SIZE - 6, SIZE - 6);
        //     g2.setColor(new Color(255, 255, 180));
        //     g2.setFont(new Font("Arial", Font.BOLD, 18));
        //     g2.drawString("★", bonus.x * SIZE + 8, bonus.y * SIZE + 22);
        // }
        if (bonus != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, foodAlpha));
            g2.drawImage(bonusImage, bonus.x * SIZE, bonus.y * SIZE, SIZE, SIZE, this);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        if (poison != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, foodAlpha));
            g2.drawImage(poisonImage, poison.x * SIZE, poison.y * SIZE, SIZE, SIZE, this);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        }

        g2.setColor(new Color(255, 255, 255, 130));
        g2.setFont(new Font("Consolas", Font.BOLD, 20));
        g2.drawString("Score: " + score, 12, 25);
        g2.drawString("High: " + high, 180, 25);
        g2.drawString("Speed: " + (190 - delay), 340, 25);

        if (!gameStarted) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, W, H);
        
        g2.setColor(new Color(120, 230, 120));
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "SnakeGame";
        int w1 = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (W - w1) / 2, H / 3 - 20);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        String subtitle = "Select Difficulty";
        int w2 = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (W - w2) / 2, H / 3 + 15);
        } else if (paused) {
        drawCenter(g2, "PAUSED", "Press P to resume");
        } else if (gameOver) {
        drawCenter(g2, "GAME OVER", "Press SPACE to restart");
        }
    }

    void drawCenter(Graphics2D g2, String big, String small) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, W, H);
        
        g2.setColor(new Color(120, 230, 120));
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        int w1 = g2.getFontMetrics().stringWidth(big);
        g2.drawString(big, (W - w1) / 2, H / 2 - 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        int w2 = g2.getFontMetrics().stringWidth(small);
        g2.drawString(small, (W - w2) / 2, H / 2 + 30);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_UP && dy != 1) { dx = 0; dy = -1; }
        else if (k == KeyEvent.VK_DOWN && dy != -1) { dx = 0; dy = 1; }
        else if (k == KeyEvent.VK_LEFT && dx != 1) { dx = -1; dy = 0; }
        else if (k == KeyEvent.VK_RIGHT && dx != -1) { dx = 1; dy = 0; }
        else if (k == KeyEvent.VK_P) paused = !paused;
        else if (k == KeyEvent.VK_SPACE && gameOver) {
        gameStarted = false;
        gameOver = false;
        delay = 130;
        add(easyBtn);
        add(mediumBtn);
        add(hardBtn);
        repaint();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("SnakeGame: Devouring Memories");
        frame.add(new SnakeGame());
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
