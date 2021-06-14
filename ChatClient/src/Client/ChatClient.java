package Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final int severPort;
    private final String severName;
    private Socket socket;
    private InputStream severIn;
    private OutputStream severOut;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String severName, int severPort) {
        this.severName = severName;
        this.severPort = severPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from " + fromLogin + "==>" + msgBody);
            }
        });
        if (!client.connect()){
            System.err.println("Connect failed.");
        }else {
            System.out.println("Connect successful...");
            if(client.login("guest","guest")){
                System.out.println("Login Successful");

            }else {
                System.err.println("Login failed");
            }
            //client.logoff();
        }
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        severOut.write(cmd.getBytes());
    }

    public boolean login(String login, String passWord) throws IOException {
        String cmd = "login " + login + " " + passWord + "\n";
        severOut.write(cmd.getBytes());
        String response = bufferedIn.readLine();
        System.out.println("Response Line " + response);
        if ("ok login ".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        }
        else{
            return false;
        }
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run() {
                readMessageLoop();
            }
        };
    }

    private void readMessageLoop() {
        String line;
        while (true){
            try {
                if (!((line = bufferedIn.readLine()) != null)) {
                    String[] tokens = line.split(" ");
                    if (tokens!=null && tokens.length>0){
                        String cmd = tokens[0];
                        if ("online".equalsIgnoreCase(cmd)){
                            handelOnline(tokens);
                        }else if("offline".equalsIgnoreCase(cmd)){
                            handelOffline(tokens);
                        }else if("msg".equalsIgnoreCase(cmd)){
                            String[] tokenMsg = line.split(" ",3);
                            handelMessage(tokenMsg);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }
    }

    private void handelMessage(String[] tokenMsg) {
        String login = tokenMsg[1];
        String msgBody = tokenMsg[2];
        for (MessageListener listener :messageListeners){
            listener.onMessage(login,msgBody);
        }
    }

    private void handelOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.offline(login);
        }
    }

    private void handelOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(severName, severPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.severOut = socket.getOutputStream();
            this.severIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(severIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        severOut.write(cmd.getBytes());
    }
}
