import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    // Set to store client output streams (for broadcasting messages)
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    
    public static void main(String[] args) throws IOException {
        int port = 12345;  // Port number for the server
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Chat server started on port " + port);

        try {
            // Keep accepting client connections
            while (true) {
                new ClientHandler(serverSocket.accept()).start();  // Create a new thread for each client
            }
        } finally {
            serverSocket.close();
        }
    }

    // This class handles the communication with a single client
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Set up input and output streams for the client
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Add the client's output stream to the list of writers
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                // Notify all clients about the new client
                broadcast("A new user has joined the chat.");

                // Listen for messages from the client
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;  // Exit if the message is "exit"
                    }
                    broadcast(message);  // Broadcast the message to all clients
                }

            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                // Clean up when the client disconnects
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                broadcast("A user has left the chat.");
            }
        }

        // Broadcast a message to all connected clients
        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
