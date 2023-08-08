package com.bothale.client;

import com.bothale.util.CryptoUtility;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import java.security.*;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Represents a client that sends signed HTTP requests to a server.
 * The client uses its private key to sign the request and sends the corresponding public key
 * and its fingerprint as headers for the server to verify the signature.
 */
public class Client {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * Constructs a new Client instance using the provided key pair.
     *
     * @param keyPair The RSA key pair containing both public and private keys.
     */
    public Client(KeyPair keyPair) {
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    /**
     * Sends a signed HTTP request to the server.
     * The request includes the public key and its fingerprint as headers.
     * The body of the request is signed using the client's private key.
     *
     * @return The HTTP status code received from the server in response.
     * @throws Exception if there's an error during request generation or transmission.
     */
    public int sendRequest() throws Exception {
        LOGGER.info("Creating HTTP request...");
        HttpPost post = new HttpPost("http://localhost:8080/");
        String message = "Hello, Server!";
        post.setEntity(new StringEntity(message));

        // Sign the message
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        byte[] signedMessage = signature.sign();

        // Attach signature as a header
        post.addHeader("X-Signature", Base64.getEncoder().encodeToString(signedMessage));

        // Send the client's public key as a header
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        post.addHeader("X-Public-Key", publicKeyBase64);

        // Generate and send the fingerprint of the public key as a header
        String fingerprint = CryptoUtility.generateFingerprint(publicKey);
        post.addHeader("X-Public-Key-Fingerprint", fingerprint);

        // Print the complete request
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("Request Method: " + post.getMethod() + "\n");
        requestLog.append("Request URI: " + post.getURI() + "\n");
        for (Header header : post.getAllHeaders()) {
            requestLog.append(header.getName() + ": " + header.getValue() + "\n");
        }
        requestLog.append("\nRequest Body: " + message);
        LOGGER.info("Complete Request:\n" + requestLog.toString());

        LOGGER.info("Sending the request to the server...");
        HttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(post);
        return response.getStatusLine().getStatusCode();
    }

}