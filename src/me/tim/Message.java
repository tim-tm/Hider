package me.tim;

import java.io.Serializable;
import java.security.PublicKey;

public class Message implements Serializable {
    private final Command command;
    private final byte[] content;
    private final PublicKey publicKey;

    public Message(PublicKey publicKey) {
        this.command = Command.HANDSHAKE;
        this.content = null;
        this.publicKey = publicKey;
    }

    public Message(Command command, byte[] content) {
        this.command = command;
        this.content = content;
        this.publicKey = null;
    }

    public Command getCommand() {
        return command;
    }

    public byte[] getContent() {
        return content;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public enum Command {
        HANDSHAKE,
        BROADCAST
    }
}
