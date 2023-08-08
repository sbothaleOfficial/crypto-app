package com.bothale.main;

import com.bothale.client.Client;
import com.bothale.server.Server;
import com.bothale.util.CryptoUtility;

import java.security.KeyPair;
import java.util.logging.Logger;

public class AppMain {

    private static final Logger LOGGER = Logger.getLogger(AppMain.class.getName());

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting the application...");

        // Generate client's key pair
        LOGGER.info("Generating client's key pair...");
        KeyPair clientKeyPair = CryptoUtility.generateKeyPair();

        // Initialize server
        LOGGER.info("Initializing the server...");
        Server server = new Server();

        // Initialize server in a new thread
        new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                LOGGER.severe("Error while starting the server: " + e.getMessage());
            }
        }).start();

        // Give server some time to start
        Thread.sleep(2000);

        // Initialize client and send request using the generated key pair
        LOGGER.info("Initializing client and sending request...");
        Client client = new Client(clientKeyPair);
        int statusCode = client.sendRequest();
        LOGGER.info("Received response with status code: " + statusCode);

        // Stop the server after the client received the response
        LOGGER.info("Stopping the server...");
        server.stop();

        LOGGER.info("Application finished.");
    }
}