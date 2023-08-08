package com.bothale.util;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class CryptoUtility {
    private static final Logger LOGGER = Logger.getLogger(CryptoUtility.class.getName());

    /**
     * Generates an RSA key pair.
     *
     * @return The generated RSA key pair.
     * @throws NoSuchAlgorithmException if RSA algorithm is not supported.
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        LOGGER.info("Generating RSA key pair...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    /**
     * Generates a fingerprint for the provided public key.
     *
     * @param publicKey The public key to generate a fingerprint for.
     * @return The Base64 encoded fingerprint of the public key.
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not supported.
     */
    public static String generateFingerprint(PublicKey publicKey) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] publicKeyBytes = publicKey.getEncoded();
        byte[] fingerprintBytes = sha256.digest(publicKeyBytes);
        return Base64.getEncoder().encodeToString(fingerprintBytes);
    }

    /**
     * Converts a byte array to a PublicKey.
     *
     * @param publicKeyBytes The byte array representation of the public key.
     * @return The PublicKey object.
     * @throws NoSuchAlgorithmException if RSA algorithm is not supported.
     * @throws InvalidKeySpecException  if the provided key specification is inappropriate.
     */
    public static PublicKey bytesToPublicKey(byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }
}
