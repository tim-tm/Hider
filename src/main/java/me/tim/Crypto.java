package me.tim;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class Crypto {
    private static final String KEY_PAIR_ALGORITHM = "EC";
    private static final int KEY_PAIR_SIZE = 256;
    private static final String KEY_AGREEMENT_ALGORITHM = "ECDH";
    private static final String CIPHER_ALGORITHM = "AES";

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
        generator.initialize(KEY_PAIR_SIZE);
        return generator.generateKeyPair();
    }

    public static Key combineKeys(PrivateKey privateKey, PublicKey publicKey) throws InvalidKeyException, NoSuchAlgorithmException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        Key secretKey = new SecretKeySpec(keyAgreement.generateSecret(), CIPHER_ALGORITHM);
        return secretKey;
    }

    public static byte[] encrypt(String text, Key sharedSecret) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, sharedSecret);
            byte[] cipherText = cipher.doFinal(text.getBytes());
            return cipherText;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(byte[] cipherText, Key sharedSecret) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, sharedSecret);
            byte[] text = cipher.doFinal(cipherText);
            return new String(text);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
