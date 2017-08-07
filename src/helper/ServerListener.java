package helper;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class ServerListener extends Thread{
    public String serverName ;
    public int leaderElectionPort ;

    @Override
    public void run() {

        StartListening();
    }

    public ServerListener (String serverName, int leaderElectionPort){
        this.serverName = serverName;
        this.leaderElectionPort = leaderElectionPort;
    }


    private void StartListening(){
        try {
            for(int i = 0; i <100; i++){   //timeout 100 runs
                Socket incoming = null;
                ServerSocket s = new ServerSocket(leaderElectionPort);

                incoming = s.accept();
                System.out.println(serverName + " is alive!");
                Scanner scan = new Scanner(incoming.getInputStream());
                if (scan.hasNextLine()) {
                    System.out.println(scan.nextLine());
                }

                s.close();
                scan.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
