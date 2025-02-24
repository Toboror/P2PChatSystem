package p2pChatSystem.network;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TCPmessaging {

    private static final int TCP_PORT = 9090; // TCP port for messaging

    // Utility method to get the current time as a string
    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /* TCP Server for receiving messages. Listens for incoming messages and displays them in the chat area.
 The message format is "sender: message". The sender is used to identify the recipient.
 The message is displayed in the chat area with the current time. */
    public static void startTCPServer(JTextArea chatArea) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
                chatArea.append("Listening for TCP messages on port: " + TCP_PORT + "\n");

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                        String message = in.readLine();
                        SwingUtilities.invokeLater(() -> chatArea.append(currentTime() + " -- " + message + "\n"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /* Sends a TCP message. The message is sent to the recipient's IP address and displayed in the chat area. */
    public static void sendTCPMessage(String message, InetAddress recipientAddress, JTextArea chatArea) {
        new Thread(() -> {
            try (Socket socket = new Socket(recipientAddress, TCP_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(message);
                SwingUtilities.invokeLater(() -> chatArea.append(currentTime() + " -- You: " + message.split(": ", 2)[1] + "\n"));
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> chatArea.append("Error sending message: " + e.getMessage() + "\n"));
                e.printStackTrace();
            }
        }).start();
    }

}
