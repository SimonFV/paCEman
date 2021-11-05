package src.main.java;

import java.io.*;
import java.net.*;

public class Client {

    private Socket socket;
    private String hostname;
    private int port;
    private InputStream input;
    private DataOutputStream output;
    private InputStreamReader reader;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(hostname, port);
            input = socket.getInputStream();
            output = new DataOutputStream(socket.getOutputStream());

            return true;
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
        return false;
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public String read() {
        try {
            reader = new InputStreamReader(input);
            int character;
            StringBuilder buffer = new StringBuilder();

            while ((character = reader.read()) != -1 && character != ';') {
                buffer.append((char) character);
            }
            if (character == -1) {
                return "-1";
            }
            return buffer.toString();
        } catch (UnknownHostException ex) {
            System.out.println("Servidor no encontrado al intentar leer: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
        return "-1";
    }

    public void send(String msg) {
        try {
            byte[] data = msg.getBytes();
            output.write(data, 0, data.length);
            output.flush();
        } catch (UnknownHostException ex) {
            System.out.println("Servidor no encontrado al intentar leer: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
