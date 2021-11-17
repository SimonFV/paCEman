package src.main.java;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {
    private PaCEman paCEman = new PaCEman();

    private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private final Color dotColor = new Color(255, 255, 0); // Color Amarillo
    private Color mazeColor = new Color(0, 0, 255); // Color Azul
    private boolean inGame = false;
    private boolean dying = false;

    private final Integer BLOCK_SIZE = 24;
    private final Integer N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;

    private int N_GHOSTS = 6;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image ghost;
    private Image apple;

    private Integer req_dx, req_dy;

    private final short levelData[] = {19, 26, 26, 26, 26, 26, 18, 18, 18, 26, 26, 26, 26, 26, 22,
            21, 0, 0, 0, 0, 0, 17, 16, 20, 0, 0, 0, 0, 0, 21, 21, 0, 19, 18, 18, 18, 16, 16, 16, 26,
            26, 26, 26, 26, 20, 21, 0, 17, 16, 16, 16, 16, 16, 20, 0, 0, 0, 0, 0, 21, 21, 0, 17, 16,
            16, 16, 16, 16, 16, 18, 18, 18, 18, 18, 20, 21, 0, 17, 16, 16, 16, 24, 24, 24, 16, 16,
            16, 16, 16, 20, 17, 18, 16, 16, 16, 20, 0, 0, 0, 17, 16, 16, 16, 16, 20, 17, 16, 16, 16,
            16, 20, 0, 0, 0, 17, 16, 16, 16, 16, 20, 17, 16, 16, 24, 16, 20, 0, 0, 0, 17, 16, 16,
            16, 16, 20, 17, 16, 20, 0, 17, 16, 18, 18, 18, 16, 16, 16, 16, 16, 20, 17, 24, 28, 0,
            25, 24, 16, 16, 16, 24, 24, 24, 24, 24, 20, 21, 0, 0, 0, 0, 0, 17, 32, 20, 0, 0, 0, 0,
            0, 21, 17, 18, 22, 0, 19, 18, 16, 64, 16, 18, 18, 18, 18, 18, 20, 17, 16, 20, 0, 17, 16,
            16, 16, 16, 16, 16, 16, 16, 16, 20, 25, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24,
            24, 28};

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Board() {

        loadImages();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }


    private void loadImages() {
        ghost = new ImageIcon("src/main/resources/images/ghost.gif").getImage();
        apple = new ImageIcon("src/main/resources/images/apple.png").getImage();

    }

    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);
        timer.start();
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {
            paCEman.movePaCEman(req_dx, req_dy, BLOCK_SIZE, N_BLOCKS, screenData);
            paCEman.drawPaCEman(g2d, req_dx, req_dy, this);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
        g2d.setColor(mazeColor);
        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);

        String start = "Press SPACE to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(dotColor);
        g2d.setFont(small);
        g2d.drawString(start, (SCREEN_SIZE - metr.stringWidth(start)) / 2, SCREEN_SIZE / 2);

    }

    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(dotColor);
        String s = "Score: " + paCEman.score.intValue();
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (int i = 0; i < paCEman.lives; i++) {
            g.drawImage(paCEman.left, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void checkMaze() {

        int i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            paCEman.score += 10000;
            paCEman.lives++;


            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void death() {

        paCEman.lives--;

        if (paCEman.lives == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void moveGhosts(Graphics2D g2d) {

        int pos;
        int count;

        for (int i = 0; i < N_GHOSTS; i++) {
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;

                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }

            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);

            if (paCEman.paceman_x > (ghost_x[i] - 12) && paCEman.paceman_x < (ghost_x[i] + 12)
                    && paCEman.paceman_y > (ghost_y[i] - 12)
                    && paCEman.paceman_y < (ghost_y[i] + 12) && inGame) {

                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {
        g2d.drawImage(ghost, x, y, this);
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(mazeColor);
                g2d.setStroke(new BasicStroke(2));

                // borde izquierdo
                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                // borde superior
                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                // borde derecho
                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
                }

                // borde inferior
                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
                }

                // Puntos
                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(dotColor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                // Frutas
                if ((screenData[i] & 32) != 0) {
                    g2d.drawImage(apple, x, y, this);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                if ((screenData[i] & 64) != 0) {
                    g2d.setColor(dotColor);
                    g2d.fillOval(x + 10, y + 10, 10, 10);
                }

                i++;
            }
        }
    }

    private void initGame() {

        paCEman.lives = 3;
        paCEman.score = 0;
        initLevel();
        N_GHOSTS = 4;
        currentSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    private void continueLevel() {

        int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE; // posicion inicial de los fantasmas
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        paCEman.paceman_x = 7 * BLOCK_SIZE; // posicion donde empieza pacman
        paCEman.paceman_y = 11 * BLOCK_SIZE;
        paCEman.pacemand_x = 0; // reinicia la direccion de movimiento
        paCEman.pacemand_y = 0;
        req_dx = 0; // reinicia los controles de direccion
        req_dy = 0;
        dying = false;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    // controles de pacman
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}
