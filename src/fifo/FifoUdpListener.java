package fifo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FifoUdpListener extends Thread {

    private static Lock lock;

    private static int LISTEN_PORT_NUMBER;
    private static int send_port_number;

    private static boolean sendPortNbrChanged = false;

    public static DatagramSocket server_receive = null;
    public static DatagramSocket server_send = null;

    public static byte[] recvBuf = null;
    public static Queue<String> queue = null;
    public static Queue<Thread> threadList = null;
    public static TimerTaskRun timerTaskRun = null;

    public FifoUdpListener(int listenPortNbr, int sendPortNbr) {
        lock = new ReentrantLock();

        LISTEN_PORT_NUMBER = listenPortNbr;
        send_port_number = sendPortNbr;

        try {
            server_receive = new DatagramSocket(LISTEN_PORT_NUMBER);
            server_send = new DatagramSocket(send_port_number);
        } catch (SocketException se) {
            System.err.println(se);
        }

        recvBuf = new byte[100];
        queue = new LinkedBlockingQueue<String>();
        threadList = new LinkedBlockingQueue<Thread>();
    }

    public static void setNewSendPortNumber(int newSendPortNbr) {
        send_port_number = newSendPortNbr;
        sendPortNbrChanged = true;
    }

    @Override
    public void run() {

        while (true) {
            if (sendPortNbrChanged) {
                try {
                    server_send = new DatagramSocket(send_port_number);
                } catch (SocketException se) {
                    System.err.println(se);
                }
            }

            final DatagramPacket recvPacket
                    = new DatagramPacket(recvBuf, recvBuf.length);
            try {
                server_receive.receive(recvPacket);
            } catch (IOException ie) {
                System.err.println(ie);
            }

            String recvStr = (new String(recvPacket.getData(), 0, recvPacket.getLength())).trim();
            System.out.println(recvStr);
            if (null == recvStr || recvStr.equals("")) {
                continue;
            }

            // 200 means all RMs processed the task successfully
            if (recvStr.equals("200")) {

                // clear the clock
                Thread thread2 = threadList.remove();
                thread2.stop();
                System.out.println("Killed");

                // send the next task if any
                if (queue.size() != 0) {
                    queue.remove();

                    if (queue.size() != 0) {
                        // get the next message
                        final String head = queue.peek();

                        timerTaskRun = new TimerTaskRun(server_send, head, recvPacket);
                        Thread thread = new Thread(timerTaskRun);
                        threadList.add(thread);
                        thread.start();
                    }
                }
            } else {
                // if it's a task message
                lock.lock();
                queue.add(recvStr);
                if (queue.size() == 1) {
                    lock.unlock();
                    // if the queue was empty, we send the task straight
                    final String head = queue.peek();
                    timerTaskRun = new TimerTaskRun(server_send, head, recvPacket);
                    Thread thread = new Thread(timerTaskRun);
                    threadList.add(thread);
                    thread.start();
                } else {
                    lock.unlock();
                }
            }

        }
    }

}
