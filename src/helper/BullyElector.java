package helper;


import frontEnd.BullySelector;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class BullyElector {

    public int electionPort;

    public BullyElector(int electionPort){
        this.electionPort=electionPort;
    }







    public void sentElectionMessage(){

        DatagramSocket datagramSocket = null;

        try {
            datagramSocket = new DatagramSocket(electionPort);
            byte[] message = "$ELECTION".getBytes();
            InetAddress host = InetAddress.getByName("localhost");

            DatagramPacket request = new DatagramPacket(message, message.length,host,electionPort);
            datagramSocket.send(request);

            //get message
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            replyString=new String(reply.getData()).trim();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
    }


}
