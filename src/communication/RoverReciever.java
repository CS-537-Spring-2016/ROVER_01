package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class RoverReciever {
    
    private ServerSocket listenSocket;
    private int port;
    
    public RoverReciever(int port) throws IOException {
        this.port = port;
        listenSocket = new ServerSocket(port);
    }
    
    public void startServer() throws IOException {

        // create a thread that waits for client to connect to 
        new Thread(() -> {
            while (true) {
                try {
                    // wait for a connection
                    Socket connectionSocket = listenSocket.accept();

                    // once there is a connection, serve them on thread
                    new Thread(new RoverHandler(connectionSocket)).start();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
