# Signed HTTP Requests System

This system allows a client to send signed HTTP requests to a server. The client signs the request using its private key and sends the corresponding public key along with its fingerprint as headers for the server to verify the signature.

## Classes Overview

### 1. AppMain

This is the main class that demonstrates how the system works. The application initializes both the client and server. The client generates an RSA key pair, signs a message, and sends the signed message to the server. The server then verifies the signature using the provided public key.

### 2. Client

Represents a client that sends signed HTTP requests to a server. The client:
- Uses its private key to sign the request
- Sends the corresponding public key and its fingerprint as headers for the server to verify the signature

Methods:
- **Client(KeyPair keyPair)**: Constructor that initializes the client with a provided RSA key pair
- **sendRequest()**: Sends a signed HTTP request to the server and returns the HTTP status code received from the server in response

### 3. Server

Represents a server that verifies signed HTTP requests from clients. The server:
- Uses the public key provided in the request headers to verify the request's signature
- Compares the provided fingerprint with the fingerprint of the provided public key to ensure its authenticity

Methods:
- **Server()**: Default constructor
- **start()**: Starts the server and sets up the necessary endpoints to listen for incoming requests
- **stop()**: Stops the server, freeing up the resources

### 4. CryptoUtility

Utility class that provides various cryptographic functionalities:
- RSA key pair generation
- Generating a fingerprint for a public key
- Converting a byte array to a PublicKey

Methods:
- **generateKeyPair()**: Generates an RSA key pair and returns it
- **generateFingerprint(PublicKey publicKey)**: Generates a fingerprint for the provided public key and returns the Base64 encoded fingerprint
- **bytesToPublicKey(byte[] publicKeyBytes)**: Converts a byte array representation of a public key to a PublicKey object

---

## Getting Started

To use this system, you need to:
1. Initialize the server by calling the `start()` method of the `Server` class
2. Initialize the client by providing an RSA key pair to the `Client` constructor
3. Send a signed HTTP request from the client to the server using the `sendRequest()` method of the `Client` class

---