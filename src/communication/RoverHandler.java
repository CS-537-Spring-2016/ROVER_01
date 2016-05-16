package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/** When any ROVER discovered science, it will write a message to your all
 * ROVERS. That message will be "sent" here. This block of code will read
 * whatever written to you. Your job is to use the data to tell your rover to go
 * pick up that science.
 * 
 * @author Shay */
public class RoverHandler implements Runnable {

    Socket roverSocket;

    public RoverHandler(Socket socket) {
        this.roverSocket = socket;
    }

    @Override
    public void run() {

        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(roverSocket.getInputStream()));

            while (true) {

                String line = input.readLine();
                // protocol: ROCK CRYSTAL 25 30
                System.out.println("INCOMING MESSAGE: " + line);

                /* IMPLEMENT YOUR CODE HERE, WHAT DO YOU WANT TO DO WITH THE
                 * DATA? */
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
