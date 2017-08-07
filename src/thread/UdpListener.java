package thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import servers.Server1;

public class UdpListener extends Thread{

    private String message;
    private int port;
    private Server1 server1;


    public UdpListener(int portNumber,Server1 server1){
        this.port=portNumber;
        this.server1 = server1;
        this.message = "";
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
            //listening
            while(true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(request);
//                String requestStrng=new String(request.getData());
                server1.setCount(server1.getCount()+1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
        
        
        
        
//		 DatagramSocket datagramSocket = null;
//	        try {
//	            //create belonging socket
//	            datagramSocket = new DatagramSocket(port);
//	            byte[] buffer = new byte[1000];
//	            byte[] reply = new byte[1000];
////	            System.out.println(centerServerImp.centerName+"is ready to listen UDP requests between servers");
//	            //listening
//	            while(true){
////	            	message="";
//	                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
//	                datagramSocket.receive(request);
//	                message=new String(request.getData());
//	                System.out.println("udpListener: "+getMessage());
////	                commonServer.setMessage(message);
//	                server1.setMessage(message);
//
//	                
//	                if(!message.equals("")){
//	    		    	String[] strings = message.split(",");
//	    		    	switch(strings[0]){
//	    		    		case "1":
//	    		    			flag = server1.createTRecord(strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
//	    		    			server1.multicast2(message);
//	    		    			reply = String.valueOf(flag).getBytes();
//	    		    			break;
//	    		    		case "2":
//	    		    			flag = server1.createSRecord(strings[1], strings[2], strings[3], strings[4], strings[5], strings[6]);
//	    		    			server1.multicast2(message);
//	    		    			reply = String.valueOf(flag).getBytes();
//	    		    			break;
//	    		    		case "3":
//	    		    			replyMessage = server1.getRecordCounts(strings[1]);
//	    		    			reply = replyMessage.getBytes();
//	    		    			break;
//	    		    		case "4":
//	    		    			flag = server1.editRecord(strings[1], strings[2], strings[3], strings[4]);
//	    		    			server1.multicast2(message);
//	    		    			reply = String.valueOf(flag).getBytes();
//	    		    			break;
//	    		    		case "5":
//	    		    			flag = server1.transferRecord(strings[1], strings[2], strings[3]);
//	    		    			server1.multicast2(message);
//	    		    			reply = String.valueOf(flag).getBytes();
//	    		    			break;
//	    		    		case "7":
//	    		    			replyMessage = server1.getRecordInfo(strings[1],strings[2]);
//	    		    			reply = replyMessage.getBytes();
//	    		    			break;
//	    		    		default:
//	    		    			System.out.println("error!");
//	    		    	}
////	    		    	message="";
//	    		    	
//	    				try {
//	    					DatagramPacket replyPacket = new DatagramPacket(reply, reply.length, request.getAddress(), request.getPort());
//	    					datagramSocket.send(replyPacket);
//	    					buffer = new byte[1000];// clear buffer, avoid the buffer last time influences the receiving buffer 
//	    					reply = new byte[1000];
//	    					
//	    				} catch (IOException e) {
//	    					// TODO Auto-generated catch block
//	    					e.printStackTrace();
//	    				}
//	                }
//	            }
//	        } catch (Exception e) {
//	            System.out.println(e.getMessage());
//	        }finally {
//	            if(datagramSocket != null)
//	                datagramSocket.close();
//	        }
    }
}
