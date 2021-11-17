package src.main.java;

// import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI {
    private Client client;
    private JFrame mainFrame;
    private JPanel boardPanel;
    // private JButton player;
    // private JButton observer;
    private String messageReceived;

    public GUI(Client client) {
        this.client = client;

        mainFrame = new JFrame();

        boardPanel = new Board();

        mainFrame.add(boardPanel);
        mainFrame.setVisible(true);
        mainFrame.setTitle("PaCEman");
        mainFrame.setSize(380, 420);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
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
