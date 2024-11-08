package me.tim.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    private static final int PORT = 1337;

    public static final LinkedList<ClientHandler> clients = new LinkedList<>();

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                System.out.printf("Client %s connected!\n", socket.getInetAddress());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ClientHandler client = new ClientHandler(socket, inputStream, objectOutputStream);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
