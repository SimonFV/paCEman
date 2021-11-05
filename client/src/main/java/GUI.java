package src.main.java;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public class GUI {
    private Client client;
    private JFrame frame;
    private JPanel panel;
    private JButton send_button;
    private JButton connect_button;
    private String message_received;
    private JTextField send_text;

    public GUI(Client client) {
        this.client = client;

        frame = new JFrame();
        panel = new JPanel();

        connect_button = new JButton("Connect");
        connect_button.addActionListener(e -> connect());
        connect_button.setBounds(100, 100, 100, 40);
        connect_button.setFocusable(false);

        send_button = new JButton("Send");
        send_button.addActionListener(e -> send(send_text.getText()));
        send_button.setBounds(100, 200, 100, 40);
        send_button.setFocusable(false);

        send_text = new JTextField(30);
        send_text.setBounds(210, 200, 200, 30);

        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(null);
        panel.add(send_button);
        panel.add(connect_button);
        panel.add(send_text);

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("PaCEman");
        frame.setSize(800, 600);

        frame.setVisible(true);
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
                    message_received = client.read();
                    if (message_received != "-1") {
                        System.out.println("Recibido: " + message_received);
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
