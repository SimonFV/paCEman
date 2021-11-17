package src.main.java;

import java.awt.*;
import javax.swing.ImageIcon;


public class PaCEman {
    private final Integer PACMAN_SPEED = 6;

    public Image up, down, left, right;
    public Integer paceman_x, paceman_y, pacemand_x, pacemand_y;
    public Integer score, lives;

    public PaCEman() {
        down = new ImageIcon("src/main/resources/images/down.gif").getImage();
        up = new ImageIcon("src/main/resources/images/up.gif").getImage();
        left = new ImageIcon("src/main/resources/images/left.gif").getImage();
        right = new ImageIcon("src/main/resources/images/right.gif").getImage();
    }


    public void drawPaCEman(Graphics2D g2d, Integer req_dx, Integer req_dy, Board board) {
        if (req_dx == -1) {
            g2d.drawImage(left, paceman_x + 1, paceman_y + 1, board);
        } else if (req_dx == 1) {
            g2d.drawImage(right, paceman_x + 1, paceman_y + 1, board);
        } else if (req_dy == -1) {
            g2d.drawImage(up, paceman_x + 1, paceman_y + 1, board);
        } else {
            g2d.drawImage(down, paceman_x + 1, paceman_y + 1, board);
        }
    }

    public void movePaCEman(Integer req_dx, Integer req_dy, Integer BLOCK_SIZE, Integer N_BLOCKS,
            short[] screenData) {

        Integer pos;
        short ch;

        if (paceman_x % BLOCK_SIZE == 0 && paceman_y % BLOCK_SIZE == 0) {
            pos = paceman_x / BLOCK_SIZE + N_BLOCKS * (int) (paceman_y / BLOCK_SIZE);
            ch = screenData[pos];

            // pacman come un punto
            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score += 10;
            }

            // pacman come una fruta
            if ((ch & 32) != 0) {
                screenData[pos] = (short) (ch & 15);
                score += 100;
            }

            // pacman come una pastilla
            if ((ch & 64) != 0) {
                screenData[pos] = (short) (ch & 15);
                lives++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacemand_x = req_dx;
                    pacemand_y = req_dy;
                }
            }

            // Check for standstill
            if ((pacemand_x == -1 && pacemand_y == 0 && (ch & 1) != 0)
                    || (pacemand_x == 1 && pacemand_y == 0 && (ch & 4) != 0)
                    || (pacemand_x == 0 && pacemand_y == -1 && (ch & 2) != 0)
                    || (pacemand_x == 0 && pacemand_y == 1 && (ch & 8) != 0)) {
                pacemand_x = 0;
                pacemand_y = 0;
            }
        }
        paceman_x = paceman_x + PACMAN_SPEED * pacemand_x;
        paceman_y = paceman_y + PACMAN_SPEED * pacemand_y;
    }

}
