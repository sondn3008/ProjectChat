package Server;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private final Server sever;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    public ServerWorker(Server serverInput, Socket clientSocket) {
        this.sever = serverInput;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handelClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handelClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens!=null && tokens.length>0){
                String cmd = tokens[0];
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)){
                    handelLogoff();
                    break;
                }else if("login".equalsIgnoreCase(cmd)){
                    handelLogin(outputStream,tokens);
                }else if("msg".equalsIgnoreCase(cmd)) {
                    String[] tokenMsg = line.split(" ",3);
                    handelMessage(tokenMsg);
                }else if("join".equalsIgnoreCase(cmd)){
                    handelJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handelLeave(tokens);
                }else {
                        String message = "Unknown " + cmd + "\n";
                        outputStream.write(message.getBytes());
                    }
                }
            }
        //clientSocket.close();
    }

    private void handelLeave(String[] tokens) {
        if (tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    private void handelJoin(String[] tokens) {
        if (tokens.length > 1){
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    public boolean isMemberOfTop(String topic){
        return topicSet.contains(topic);
    }

    private void handelMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workerList = sever.getWorkerList();
        for (ServerWorker worker : workerList){
            if (isTopic){
                if (worker.isMemberOfTop(sendTo)){
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }else if(sendTo.equalsIgnoreCase(worker.getLogin())) {
                String outMsg = "msg " + login + " " + body + "\n";
                worker.send(outMsg);
            }
        }
    }

    private void handelLogoff() throws IOException {
        sever.removeWorker(this);
        List<ServerWorker> workerList = sever.getWorkerList();
        String offllineMessage = "offline " + login + "\n";
        for(ServerWorker worker : workerList){
            if (!login.equals(worker.getLogin())){
                worker.send(offllineMessage);
            }
        }
        clientSocket.close();
    }

    private void handelLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3){
            String login = tokens[1];
            String passWord = tokens[2];
            if (login.equals("guest") && passWord.equals("guest")){
                String msg = "ok login \n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User Login Successfully: " + login);
                List<ServerWorker> workerList = sever.getWorkerList();
                for(ServerWorker worker : workerList){
                    if (worker.getLogin() != null){
                        String msg2 = "online " + worker.getLogin() + "\n";
                        send(msg2);
                    }
                }
                String onllineMessage = "online " + login + "\n";
                for(ServerWorker worker : workerList){
                    if (!login.equals(worker.getLogin())){
                        worker.send(onllineMessage);
                    }
                }
            }
            else {
                String msg = "error login \n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    private void send(String msg) throws IOException {
        if (login!=null){
            outputStream.write(msg.getBytes());
        }
    }

    public String getLogin() {
        return login;
    }

}
