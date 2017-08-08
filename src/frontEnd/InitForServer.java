package frontEnd;

import helper.PortDefinition;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class InitForServer extends Thread{

    ArrayList<String> histories;

    public InitForServer(ArrayList<String> histories){
        this.histories=histories;
    }

    @Override
    public void run() {
        DatagramSocket init=null;
        try {
            init = new DatagramSocket(PortDefinition.FE_INITIAL_PORT);
            InetAddress host = InetAddress.getByName("localhost");
            byte[] buffer = new byte[500];


            while(true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                init.receive(request);
                String message=new String(request.getData());
                if(message.trim().equals("$INIT")){
                    System.out.println(request.getPort()+"request init work help from FE");
                    for (int i = 0; i < histories.size(); i++) {
                        sentMessage(host, init, histories.get(i), request.getPort());
                    }
                    System.out.println("FE sent all history to"+request.getPort());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("-------");
        }finally {
            if(init!=null)
                init.close();
        }
    }


    public void sentMessage(InetAddress host,DatagramSocket socket,String messageStr,int port){

        try {
            byte[] message = messageStr.getBytes();
            DatagramPacket replyPacket = new DatagramPacket(message, message.length, host,port);
            socket.send(replyPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

