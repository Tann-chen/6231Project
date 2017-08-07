package helper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.Timer;


public class HeartBeat implements ActionListener{

    private static int failureDetectorPort=PortDefinition.FailureDetector;
    private Timer timer;
    private int replicaNo;


    public HeartBeat(int portNo){
        this.timer= new Timer(2000,this);
        this.replicaNo=portNo;
    }

    public void startUp(){
        this.timer.start();
    }

    public void showDown(){
        this.timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        sentHeartBeat();
    }


    public void sentHeartBeat() {
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
            byte[] message = String.valueOf(replicaNo).getBytes();
            InetAddress host = InetAddress.getByName("localhost");

            DatagramPacket heartBeatPacket = new DatagramPacket(message, message.length,host,failureDetectorPort);
            datagramSocket.send(heartBeatPacket);
            System.out.println(replicaNo+"heartbeat");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
    }
}
