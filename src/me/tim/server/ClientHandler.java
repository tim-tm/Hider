package me.tim.server;

import me.tim.Crypto;
import me.tim.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final KeyPair keyPair;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;
    private Key sharedSecret;

    public ClientHandler(Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.socket = socket;
        try {
            this.keyPair = Crypto.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            Message message;
            while ((message = (Message)this.input.readObject()) != null) {
                if (message.getCommand().equals(Message.Command.HANDSHAKE)) {
                    PublicKey key = message.getPublicKey();
                    assert key != null;
                    this.handshake(key);
                } else {
                    byte[] cipherMessage = message.getContent();
                    String msg = Crypto.decrypt(cipherMessage, this.sharedSecret);
                    System.out.printf("%s: %s\n", this.socket.getInetAddress(), msg);
                    this.broadcastMessage(msg);
                }
            }
            this.input.close();
            this.output.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void handshake(PublicKey key) {
        System.out.printf("Client key: %s\n", key.toString());
        System.out.printf("Server key: %s\n", this.keyPair.getPublic().toString());
        try {
            this.sharedSecret = Crypto.combineKeys(this.keyPair.getPrivate(), key);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("Shared Secret: %s\n", Arrays.toString(this.sharedSecret.getEncoded()));
        try {
            Message pubkey = new Message(this.keyPair.getPublic());
            this.output.writeObject(pubkey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : Server.clients) {
            if (client == this) continue;

            try {
                byte[] cipherMessage = Crypto.encrypt(message, client.sharedSecret);
                Message msg = new Message(Message.Command.BROADCAST, cipherMessage);
                client.output.writeObject(msg);
            } catch (IOException e) {
                Server.clients.remove(client);
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
