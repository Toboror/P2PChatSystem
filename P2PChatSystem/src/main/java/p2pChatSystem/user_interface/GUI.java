package p2pChatSystem.user_interface;

import p2pChatSystem.network.TCPmessaging;
import p2pChatSystem.network.UDPconnection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static p2pChatSystem.network.TCPmessaging.startTCPServer;
import static p2pChatSystem.network.UDPconnection.*;
import static p2pChatSystem.network.UDPconnection.currentUsername;

public class GUI {



    // Sets up GUI for the chat system. The GUI contains a chat area, message field, send button, and user list.
    public static void setupGUI() {
        JFrame frame = new JFrame("Java Chat System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());

        JTextArea chatArea = new JTextArea(15, 40);
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);

        JTextField messageField = new JTextField(30);
        JButton sendButton = new JButton("Send");
        JButton changeUsernameButton = new JButton("Change Username");
        bottomPanel.add(messageField);
        bottomPanel.add(sendButton);
        bottomPanel.add(changeUsernameButton);

        DefaultListModel<String> userListModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(userListModel);
        userList.setBorder(BorderFactory.createTitledBorder("Online Users"));
        rightPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentRecipient = userList.getSelectedValue();
                if (currentRecipient != null) {
                    chatArea.append("Now chatting with: " + currentRecipient + "\n");
                }
            }
        });

        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(rightPanel, BorderLayout.EAST);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        initializeUsername(frame, chatArea, userListModel);
        UDPconnection.startUDPReceiver(chatArea, userListModel);
        startTCPServer(chatArea);
        startPresenceBroadcast();
        startInactivityChecker(userListModel);

        sendButton.addActionListener(e -> sendMessage(messageField, chatArea));
        messageField.addActionListener(e -> sendMessage(messageField, chatArea));

        changeUsernameButton.addActionListener(e -> changeUsername(frame, chatArea, userListModel));
    }

    // Initialize username
    private static void initializeUsername(JFrame frame, JTextArea chatArea, DefaultListModel<String> userListModel) {
        while (true) {
            currentUsername = JOptionPane.showInputDialog(frame, "Enter your username:");
            if (currentUsername != null && !currentUsername.trim().isEmpty() && !onlineUsers.containsKey(currentUsername)) {
                try {
                    localAddress = InetAddress.getLocalHost(); // Initialize localAddress
                } catch (UnknownHostException e) {
                    chatArea.append("Error obtaining local address: " + e.getMessage() + "\n");
                    e.printStackTrace();
                    System.exit(1); // Exit if unable to resolve local address
                }
                break;
            }
            JOptionPane.showMessageDialog(frame, "Invalid or duplicate username. Try again.");
        }
        sendInitialPresence(chatArea, userListModel);
    }

    // Send initial presence
    private static void sendInitialPresence(JTextArea chatArea, DefaultListModel<String> userListModel) {
        try {
            String presenceMessage = currentUsername + " is online";
            UDPconnection.sendUDPBroadcast(presenceMessage);
            onlineUsers.put(currentUsername, localAddress);
            userLastActivity.put(currentUsername, System.currentTimeMillis());
            UDPconnection.updateUserList(userListModel);
        } catch (IOException e) {
            chatArea.append("Error broadcasting presence: " + e.getMessage() + "\n");
        }
    }

    // Handle username change
    private static void changeUsername(JFrame frame, JTextArea chatArea, DefaultListModel<String> userListModel) {
        String newUsername = JOptionPane.showInputDialog(frame, "Enter your new username:");
        if (newUsername != null && !newUsername.trim().isEmpty() && !onlineUsers.containsKey(newUsername)) {
            String oldUsername = currentUsername;
            currentUsername = newUsername;
            onlineUsers.remove(oldUsername);
            userLastActivity.remove(oldUsername);
            try {
                sendUDPBroadcast(oldUsername + " has changed username to " + newUsername);
            } catch (IOException e) {
                chatArea.append("Error broadcasting username change: " + e.getMessage() + "\n");
            }
            updateUserList(userListModel);
        } else {
            JOptionPane.showMessageDialog(frame, "Username already exists or is invalid.");
        }
    }

    // Send message
    private static void sendMessage(JTextField messageField, JTextArea chatArea) {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && currentRecipient != null) {
            InetAddress recipientAddress = onlineUsers.get(currentRecipient);
            if (recipientAddress != null) {
               TCPmessaging.sendTCPMessage(currentUsername + ": " + message, recipientAddress, chatArea);
                messageField.setText("");
            } else {
                chatArea.append("Error: Recipient not found.\n");
            }
        } else {
            chatArea.append("Select a recipient to message.\n");
        }
    }

}
