package fifo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TimerTaskRun implements Runnable {
    private DatagramSocket server;
    private String data;
    private DatagramPacket recvPacket;
    private long startTime;

    private int EXPIRE = 3000;
    private int RATE = 1000;

    public TimerTaskRun(DatagramSocket server, String data, DatagramPacket recvPacket) {
        this.startTime = System.currentTimeMillis();
        this.server = server;
        this.data = data;
        this.recvPacket = recvPacket;
    }

    public void run() {
        while (true) {
            long now = System.currentTimeMillis();

            // the expiration time is 3 seconds, aka try 3 times
            if (now - startTime > EXPIRE) {
                break;
            }
            try {
                Thread.sleep(RATE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            send(this.server, this.data, this.recvPacket);
        }
    }

    private void send(DatagramSocket socket, String sendBuf, DatagramPacket recvPacket) {
        int port = recvPacket.getPort();
        InetAddress addr = recvPacket.getAddress();
        final DatagramPacket sendPacket
                = new DatagramPacket(sendBuf.getBytes(), sendBuf.length(), addr, port);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
