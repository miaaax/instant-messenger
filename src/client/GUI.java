package client;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;

public class GUI {
    public final Client client;
    private final LoginView loginView;
    private ConversationView conversationView;

    public GUI(Client client) {
        this.client = client;
        loginView = new LoginView(this);
        loginView.setVisible(true);
    }

    public void loginResult(boolean success) {
        if (success) {
            showConversationView();
        } else {
            JOptionPane.showMessageDialog(null, "Login Failed", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showConversationView(){
        loginView.setVisible(false);
        loginView.dispose();

        conversationView = new ConversationView(this);
        conversationView.setVisible(true);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Client client = new Client();

                try {
                    client.connectToServer();
                } catch (IOException e) {
                    System.out.println("Error connecting to server.");
                    System.exit(1);
                }

                new GUI(client);

            }
        });

    }
}
