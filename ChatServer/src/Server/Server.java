package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
    private final int severPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();


    public Server(int severPort) {
        this.severPort = severPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(severPort);
            while (true){
                System.out.println("About connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this,clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ServerWorker> getWorkerList(){
        return workerList;
    }

    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
