package src.main.java;

import java.io.*;
import java.net.*;

public final class Client {

    private static Client instance;
    private Socket socket;
    private String hostname;
    private Integer port;
    private InputStream input;
    private DataOutputStream output;
    private InputStreamReader reader;

    private Client(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static Client getInstance(String hostname, Integer port) {
        if (instance == null) {
            instance = new Client(hostname, port);
        }
        return instance;
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
            Integer character;
            StringBuilder buffer = new StringBuilder();

            while ((character = Integer.valueOf(reader.read())) != -1
                    && character.intValue() != ';') {
                buffer.append(character.toString());
            }
            if (character.intValue() == -1) {
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
            output.write(msg.getBytes(), 0, msg.getBytes().length);
            output.flush();
        } catch (UnknownHostException ex) {
            System.out.println("Servidor no encontrado al intentar leer: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
