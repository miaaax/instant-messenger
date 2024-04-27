package client;

import shared.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


public class ConversationView extends JFrame {
    private final GUI gui;
    private int recipientID;
    private String recipientName;
    public JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton backButton;
    private JScrollPane scrollPane;
    private JLabel recipientLabel;

    public ConversationView(GUI gui, int ID, String rName) {
        // main frame
        super("Conversation");

        this.gui = gui;
        this.recipientID = ID;
        this.recipientName = rName;

        setSize(625, 575);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // back button
        ImageIcon backIcon = new ImageIcon("client/icons/back.png");
        Image scaledBackImage = backIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        ImageIcon scaledBackIcon = new ImageIcon(scaledBackImage);
        backButton = new JButton(scaledBackIcon);
        backButton.setPreferredSize(new Dimension(25, 25));

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.showHomeView();
            }
        });
        header.add(backButton, BorderLayout.WEST);

        // CID :: Recipient's name
        recipientLabel = new JLabel(String.format("[UID%s] %s", recipientID, recipientName), SwingConstants.LEFT);
        recipientLabel.setFont(new Font("Arial", Font.BOLD, 15));
        recipientLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        header.add(recipientLabel, BorderLayout.CENTER);

        // collation of header
        add(header, BorderLayout.NORTH);

        // chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // message field
        messageField = new JTextField();
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // send button
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // footer panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        add(messagePanel, BorderLayout.SOUTH);
        setVisible(true);

        // opening message
        chatArea.setForeground(Color.gray);
        chatArea.append(String.format("[System] You can now send messages to %s.\n", recipientName));
        chatArea.setForeground(Color.black);
    }

    private void sendMessage() throws IOException, ClassNotFoundException {
        String message = messageField.getText().trim();

        if (message.equals("/quit")) {
            gui.logoutResult(gui.client.logout());
            return;
        }

        Message m = new Message(
                0,
                new ArrayList<>() {{
                    add(recipientID);
                }},
                Message.Type.TEXT,
                Message.Status.REQUEST,
                message
        );

        if (!message.isEmpty()) {
            gui.client.sendMessage(m);
            String timestamp = m.getTimestamp().toString();  // Assuming it returns the string directly without the brackets
            String[] parts = timestamp.split(" "); // Split by space
            String truncatedTimestamp = parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3];

            chatArea.append(String.format("[%s] [UID%s] %s: %s\n",
                    truncatedTimestamp,
                    gui.client.user.getUserId(),
                    gui.client.user.getUsername(),
                    message));

            // Clear the message field
            messageField.setText("");
        }
    }
}