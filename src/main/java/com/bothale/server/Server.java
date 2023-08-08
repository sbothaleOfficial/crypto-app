package com.bothale.server;

import com.bothale.util.CryptoUtility;

import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Represents a server that verifies signed HTTP requests from clients.
 * The server uses the public key provided in the request headers to verify the request's signature.
 */
public class Server {

    private HttpServer server;
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    /**
     * Default constructor for the Server.
     */
    public Server() {}

    /**
     * Starts the server and sets up the necessary endpoints to listen for incoming requests.
     * The server listens for POST requests and verifies the signature using the public key
     * provided in the request headers.
     *
     * @throws Exception if there's an error during server setup or initialization.
     */
    public void start() throws Exception {
        LOGGER.info("Starting the server...");

        // Set up HTTP server
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, 0);
                return;
            }

            // Extract message and signature
            String message = new String(readAllBytesFromStream(exchange.getRequestBody()));
            String signatureBase64 = exchange.getRequestHeaders().getFirst("X-Signature");
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            // Extract client's public key from the headers
            String publicKeyBase64 = exchange.getRequestHeaders().getFirst("X-Public-Key");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

            PublicKey clientPublicKey = null;
            try {
                clientPublicKey = CryptoUtility.bytesToPublicKey(publicKeyBytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Check if the received fingerprint matches the generated one
            String receivedFingerprint = exchange.getRequestHeaders().getFirst("X-Public-Key-Fingerprint");
            String expectedFingerprint = null;
            try {
                expectedFingerprint = CryptoUtility.generateFingerprint(clientPublicKey);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            if (!expectedFingerprint.equals(receivedFingerprint)) {
                exchange.sendResponseHeaders(HttpStatus.SC_FORBIDDEN, 0);
                return;
            }

            try {
                // Verify the signature
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initVerify(clientPublicKey);
                signature.update(message.getBytes());

                byte[] responseBytes;
                int statusCode;
                if (signature.verify(signatureBytes)) {
                    responseBytes = "Valid signature!".getBytes();
                    statusCode = HttpStatus.SC_OK;
                } else {
                    responseBytes = "Invalid signature!".getBytes();
                    statusCode = HttpStatus.SC_FORBIDDEN;
                }

                exchange.sendResponseHeaders(statusCode, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } catch (Exception e) {
                exchange.sendResponseHeaders(HttpStatus.SC_INTERNAL_SERVER_ERROR, 0);
            }

        });
        server.start();
    }

    /**
     * Reads all bytes from an input stream and returns them in a byte array.
     *
     * @param is the input stream to be read.
     * @return a byte array containing all the bytes read from the input stream.
     * @throws IOException if an I/O error occurs while reading from the input stream.
     */
    private byte[] readAllBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    /**
     * Stops the server, freeing up the resources.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}