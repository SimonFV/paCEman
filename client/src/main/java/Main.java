package src.main.java;

public class Main {
    public static void main(String[] args) {

        Client client = new Client("127.0.0.1", 27015);
        new GUI(client);

    }
}
