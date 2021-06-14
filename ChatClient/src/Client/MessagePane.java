package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MessagePane extends JPanel implements MessageListener {
    private final ChatClient client;
    private final String login;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();


    public MessagePane(ChatClient client, String login) {
        this.client = client;
        this.login = login;
        client.addMessageListener(this);
        setLayout(new BorderLayout());
        add(new JScrollPane(messageList),BorderLayout.CENTER);
        add(messageList, BorderLayout.CENTER);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                try {
                    listModel.addElement(text);
                    inputField.setText("");
                    client.msg(login,text);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {
        if (login.equalsIgnoreCase(fromLogin)){
            String line = fromLogin + ": " + msgBody;
            listModel.addElement(line);
        }
    }
}
