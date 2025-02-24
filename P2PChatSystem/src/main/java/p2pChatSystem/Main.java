package p2pChatSystem;

import p2pChatSystem.user_interface.GUI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::setupGUI);
    }
}
