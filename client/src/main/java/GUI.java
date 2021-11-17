package src.main.java;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class GUI {
    private Client client;
    private JFrame mainFrame;
    private JPanel boardPanel, menuPanel;
    private JButton playerButton, observerButton;
    private String messageReceived;

    public GUI(Client client) {
        this.client = client;

        mainFrame = new JFrame();

        menuPanel = new JPanel();
        menuPanel.setBackground(Color.BLACK);
        menuPanel.setLayout(null);

        playerButton = new JButton("PLAY");
        playerButton.addActionListener(e -> startAsPlayer());
        playerButton.setBounds(140, 100, 100, 40);
        playerButton.setBackground(new Color(0, 0, 40));
        playerButton.setForeground(Color.YELLOW);
        playerButton.setBorder(new LineBorder(Color.BLUE));
        playerButton.setFocusable(false);
        menuPanel.add(playerButton);

        observerButton = new JButton("OBSERVE");
        observerButton.addActionListener(e -> startAsObserver());
        observerButton.setBounds(140, 200, 100, 40);
        observerButton.setBackground(new Color(0, 0, 40));
        observerButton.setForeground(Color.YELLOW);
        observerButton.setBorder(new LineBorder(Color.BLUE));
        observerButton.setFocusable(false);
        menuPanel.add(observerButton);

        mainFrame.add(menuPanel);
        mainFrame.setVisible(true);
        mainFrame.setTitle("PaCEman");
        mainFrame.setSize(380, 420);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
    }

    public void startAsPlayer() {
        mainFrame.getContentPane().removeAll();
        boardPanel = new Board();
        mainFrame.getContentPane().add(boardPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
        boardPanel.grabFocus();
        boardPanel.requestFocusInWindow();
    }

    public void startAsObserver() {
        mainFrame.getContentPane().removeAll();
        boardPanel = new Board();
        mainFrame.getContentPane().add(boardPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    // Inicia la conexion con el servidor
    public void connect() {
        if (client.connect()) {
            System.out.println("Conexion exitosa!");
            startReading();
        } else {
            System.out.println("No se pudo establecer conexion con el servidor.");
        }
    }

    // Inicia un nuevo hilo para recibir los mensajes del servidor
    public void startReading() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    messageReceived = client.read();
                    if (messageReceived != "-1") {
                        System.out.println("Recibido: " + messageReceived);
                    } else {
                        break;
                    }
                }
                System.out.println("Cliente desconectado.");
                client.disconnect();
            }
        });
        thread.start();
    }

    // Envia un mensaje al servidor
    public void send(String msg) {
        System.out.println("Enviando: " + msg);
        client.send(msg);
    }

}
