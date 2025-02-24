package p2pChatSystem.network;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class UDPconnection {

    public static final ConcurrentHashMap<String, InetAddress> onlineUsers = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Long> userLastActivity = new ConcurrentHashMap<>();
    public static String currentUsername;
    public static String currentRecipient;
    private static final int UDP_PORT = 8080; // UDP port for user discovery
    private static final long INACTIVITY_THRESHOLD = 10000; // 10 seconds
    private static DatagramSocket udpSocket;
    public static InetAddress localAddress;

    // UDP Receiver for user discovery. Receives a signal from other users and updates the user list.
    // The signal can be a user presence, username change, or user leave.
    public static void startUDPReceiver(JTextArea chatArea, DefaultListModel<String> userListModel) {
        new Thread(() -> {
            try {
                udpSocket = new DatagramSocket(UDP_PORT);
                localAddress = InetAddress.getLocalHost();
                byte[] buffer = new byte[1024];
                chatArea.append("Listening for UDP broadcasts on port: " + UDP_PORT + "\n");

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    InetAddress senderAddress = packet.getAddress();

                    // Ignore messages from self
                    if (senderAddress.equals(localAddress)) continue;

                    // Process user presence, username changes, or user leave
                    if (message.contains("is online")) {
                        handleUserPresence(message, senderAddress, userListModel);
                    } else if (message.contains("has changed username to")) {
                        handleUsernameChange(message, senderAddress, userListModel);
                    } else if (message.contains("has left")) {
                        handleUserLeave(message, userListModel);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Handles user presence. Add user to onlineUsers and update user list. Also update userLastActivity.
    private static void handleUserPresence(String message, InetAddress address, DefaultListModel<String> userListModel) {
        String username = message.split(" ")[0];
        onlineUsers.put(username, address);
        userLastActivity.put(username, System.currentTimeMillis());
        updateUserList(userListModel);
    }

    // Handles username change. Update username in onlineUsers and userLastActivity. Update user list.
    private static void handleUsernameChange(String message, InetAddress address, DefaultListModel<String> userListModel) {
        String[] parts = message.split(" has changed username to ");
        String oldUsername = parts[0];
        String newUsername = parts[1];
        onlineUsers.remove(oldUsername);
        userLastActivity.remove(oldUsername);
        onlineUsers.put(newUsername, address);
        userLastActivity.put(newUsername, System.currentTimeMillis());
        updateUserList(userListModel);
    }

    // Handles user leave. Remove user from onlineUsers and userLastActivity. Update user list.
    private static void handleUserLeave(String message, DefaultListModel<String> userListModel) {
        String username = message.split(" has left")[0];
        onlineUsers.remove(username);
        userLastActivity.remove(username);
        updateUserList(userListModel);
    }

    // Send UDP broadcast. Broadcasts a message to all users on the network. The message is sent to port UDP_PORT.
    public static void sendUDPBroadcast(String message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] sendData = message.getBytes();
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), UDP_PORT);
        socket.send(packet);
        socket.close();
    }

    // Broadcasts presence periodically. Sends a presence message every 3 seconds.
    public static void startPresenceBroadcast() {
        new Timer(3000, e -> {
            try {
                String presenceMessage = currentUsername + " is online";
                sendUDPBroadcast(presenceMessage);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // Checks and remove inactive users. Removes users who have not sent any activity for INACTIVITY_THRESHOLD milliseconds.
    public static void startInactivityChecker(DefaultListModel<String> userListModel) {
        new Timer(1000, e -> {
            long currentTime = System.currentTimeMillis();
            onlineUsers.keySet().removeIf(username -> {
                Long lastActivity = userLastActivity.get(username);
                if (lastActivity == null || (currentTime - lastActivity) > INACTIVITY_THRESHOLD) {
                    userLastActivity.remove(username);
                    return true;
                }
                return false;
            });
            updateUserList(userListModel);
        }).start();
    }

    // Updates the user list. Clear the list model and add all online users to the list model.
    public static void updateUserList(DefaultListModel<String> userListModel) {
        SwingUtilities.invokeLater(() -> {
            String previousSelection = currentRecipient; // Remember selected user
            userListModel.clear();
            onlineUsers.keySet().forEach(userListModel::addElement);

            // Reselect previous user if still online. Otherwise, reset currentRecipient.
            if (previousSelection != null && onlineUsers.containsKey(previousSelection)) {
                currentRecipient = previousSelection;
            } else {
                currentRecipient = null;
            }
        });
    }

}
