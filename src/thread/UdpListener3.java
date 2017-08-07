package thread;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import servers.Server3;

public class UdpListener3 extends Thread{

    private String message;
    private int port;
    private Server3 server3;

    public UdpListener3(int portNumber,Server3 server3){
        this.port=portNumber;
        this.server3 = server3;
    }


    public String getMessage() {
		return message;
	}


	@Override
    public void run() {
		DatagramSocket datagramSocket = null;
        try {
            //create belonging socket
            datagramSocket = new DatagramSocket(port);
            byte[] buffer = new byte[1000];
//            System.out.println("port: "+port);
            //listening
            while(true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(request);
                String requestStrng=new String(request.getData());
//                System.out.println("requestString: "+requestStrng);
                server3.setCount(server3.getCount()+1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
    }
}
