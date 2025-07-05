import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {

    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final int UNIT_SIZE = 25;
    private final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    private int DELAY;  // Will set based on difficulty

    private final int[] x = new int[GAME_UNITS]; // snake x coords
    private final int[] y = new int[GAME_UNITS]; // snake y coords
    private int bodyParts = 6;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private char direction = 'R';
    private boolean running = false;
    private Timer timer;
    private Random random;

    private int highScore = 0;
    private final String highScoreFile = "snake_highscore.dat";

    private JFrame frame;
    private JButton playAgainBtn;
    private JButton exitBtn;

    public SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        loadHighScore();

        selectDifficultyAndStart();

        setupFrame();
    }

    private void selectDifficultyAndStart() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select Difficulty Level:",
                "Difficulty Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        // Set timer delay according to difficulty
        switch (choice) {
            case 0: // Easy
                DELAY = 150;
                break;
            case 2: // Hard
                DELAY = 50;
                break;
            case 1: // Medium or default
            default:
                DELAY = 100;
                break;
        }

        startGame();
    }

    private void setupFrame() {
        frame = new JFrame("Snake Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);

        // Buttons Panel (initially invisible)
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(Color.black);
        playAgainBtn = new JButton("Play Again");
        exitBtn = new JButton("Exit");

        playAgainBtn.addActionListener(e -> {
            selectDifficultyAndStart();
            playAgainBtn.setVisible(false);
            exitBtn.setVisible(false);
        });
        exitBtn.addActionListener(e -> System.exit(0));

        buttonsPanel.add(playAgainBtn);
        buttonsPanel.add(exitBtn);
        playAgainBtn.setVisible(false);
        exitBtn.setVisible(false);

        frame.add(buttonsPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void startGame() {
        applesEaten = 0;
        bodyParts = 6;
        direction = 'R';

        // Reset snake coordinates (optional, start in center)
        x[0] = WIDTH / 2;
        y[0] = HEIGHT / 2;
        for (int i = 1; i < bodyParts; i++) {
            x[i] = x[0] - i * UNIT_SIZE;
            y[i] = y[0];
        }

        newApple();
        running = true;

        if (timer != null) timer.stop();
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Draw apple
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            // Draw score & high score
            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 20));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, 10, g.getFont().getSize());
            g.drawString("High Score: " + highScore, WIDTH - metrics.stringWidth("High Score: " + highScore) - 10, g.getFont().getSize());

        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            if (applesEaten > highScore) {
                highScore = applesEaten;
                saveHighScore();
            }
            newApple();
        }
    }

    public void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }
        // Check if head touches borders
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            playAgainBtn.setVisible(true);
            exitBtn.setVisible(true);
            repaint();
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (WIDTH - metrics1.stringWidth("Game Over")) / 2, HEIGHT / 2 - 50);

        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2, HEIGHT / 2);
        g.drawString("High Score: " + highScore, (WIDTH - metrics2.stringWidth("High Score: " + highScore)) / 2, HEIGHT / 2 + 40);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
            }
        }
    }

    private void saveHighScore() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(highScoreFile))) {
            dos.writeInt(highScore);
        } catch (IOException e) {
            System.err.println("Could not save high score.");
        }
    }

    private void loadHighScore() {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(highScoreFile))) {
            highScore = dis.readInt();
        } catch (IOException e) {
            highScore = 0; // no file found, default 0
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame());
    }
}
