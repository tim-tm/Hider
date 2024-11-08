package me.tim.client;

import me.tim.Crypto;
import me.tim.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Scanner;

public class Client extends Thread {
    private static final int PORT = 1337;
    private final Socket socket;
    private final KeyPair keyPair;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;
    private Key sharedSecret;

    public Client(Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.socket = socket;
        this.input = input;
        this.output = output;
        try {
            this.keyPair = Crypto.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            Message msg;
            while ((msg = (Message) this.input.readObject()) != null) {
                if (msg.getCommand().equals(Message.Command.HANDSHAKE)) {
                    PublicKey key = msg.getPublicKey();
                    assert key != null;
                    this.handshake(key);
                } else {
                    byte[] cipherMessage = msg.getContent();
                    String message = Crypto.decrypt(cipherMessage, this.sharedSecret);
                    System.out.printf("%s: %s\n", this.socket.getInetAddress(), message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void handshake(PublicKey key) {
        System.out.printf("Server key: %s\n", key.toString());
        System.out.printf("Client key: %s\n", this.keyPair.getPublic().toString());
        try {
            this.sharedSecret = Crypto.combineKeys(this.keyPair.getPrivate(), key);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("Shared Secret: %s\n", Arrays.toString(this.sharedSecret.getEncoded()));
    }

    public Key getSharedSecret() {
        return sharedSecret;
    }

    public static void main(String[] args) throws IOException {
        String host;
        if (args.length != 1) {
            InetAddress net = InetAddress.getLocalHost();
            host = net.getHostName();
        } else {
            host = args[0];
        }
        Socket socket = new Socket(host, PORT);

        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        Client client = new Client(socket, inputStream, outputStream);
        client.start();

        Message pubkey = new Message(client.keyPair.getPublic());
        outputStream.writeObject(pubkey);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            byte[] content = Crypto.encrypt(input, client.getSharedSecret());
            Message message = new Message(Message.Command.BROADCAST, content);
            outputStream.writeObject(message);
        }
        outputStream.close();
    }
}
