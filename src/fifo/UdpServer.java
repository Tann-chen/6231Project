package fifo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UdpServer {

    private static Lock lock = new ReentrantLock();
    public static int PORT_NUMBER = 8888;

    public static void main(String[] args) throws IOException {
        final DatagramSocket server = new DatagramSocket(PORT_NUMBER);
        byte[] recvBuf = new byte[100];
        Queue<String> queue = new LinkedBlockingQueue<String>();
        Queue<Thread> threadList = new LinkedBlockingQueue<Thread>();
        TimerTaskRun timerTaskRun = null;

        while (true) {
            final DatagramPacket recvPacket
                    = new DatagramPacket(recvBuf, recvBuf.length);
            server.receive(recvPacket);
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

                        timerTaskRun = new TimerTaskRun(server, head, recvPacket);
                        Thread thread = new Thread(timerTaskRun);
                        threadList.add(thread);
                        thread.start();
                    }
                } else {
                }
            } else {
                // if it's a task message
                lock.lock();
                queue.add(recvStr);
                if (queue.size() == 1) {
                    lock.unlock();
                    // if the queue was empty, we send the task straight
                    final String head = queue.peek();
                    timerTaskRun = new TimerTaskRun(server, head, recvPacket);
                    Thread thread = new Thread(timerTaskRun);
                    threadList.add(thread);
                    thread.start();
                } else {
                    lock.unlock();
                }
            }
        }
    }

    // not used, but keep it here for now
    private static void send(DatagramSocket socket, String sendBuf, DatagramPacket recvPacket) {
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
