package servers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import helper.HeartBeat;
import helper.LeaderElection;
import records.Record;
import records.StudentRecord;
import records.TeacherRecord;
import thread.UdpListener3;

public class Server3 implements CenterServer{
	private HashMap<Character,ArrayList<Record>> DDOServer3;
	private HashMap<Character,ArrayList<Record>> MTLServer3;
	private HashMap<Character,ArrayList<Record>> LVLServer3;
	private File loggingFileDDO = new File("DDOServer3.txt");
    private File loggingFileMTL = new File("MTLServer3.txt");
    private File loggingFileLVL = new File("LVLServer3.txt");
    private int count;
    private static  LeaderElection el;

	public Server3() {
		this.DDOServer3 = new HashMap<>();
		this.MTLServer3 = new HashMap<>();
		this.LVLServer3 = new HashMap<>();
		this.count = 0;
	}
	   
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public static void main(String[] args) {
		int port=5003;
    	Server3 server3 = new Server3();
    	String message = "";
    	boolean flag = false;
    	String replyMessage = "";
    	new UdpListener3(port+2000, server3).start();;
    	

//    	new HeartBeat(port).startUp();
//		el=new LeaderElection("Server3","localhost",3,5003,7003);

    	 DatagramSocket datagramSocket = null;
         try {
             //create belonging socket
             datagramSocket = new DatagramSocket(port);
             byte[] buffer = new byte[1000];
             byte[] reply = new byte[1000];
//             System.out.println(centerServerImp.centerName+"is ready to listen UDP requests between servers");
             //listening
             while(true){
//             	 message="";
             	
                 DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                 datagramSocket.receive(request);
                 message=new String(request.getData()).trim();
                 System.out.println("updListener: "+ message);
//                 System.out.println("request port"+ request.getPort());
                 try {
//  					System.out.println("reply: "+String.valueOf(reply));
//                	if(message != "200"){  // 收到的是操作的时候，才回复acknowledgement,如果收到的是acknowledgement，则不回复
                		if(request.getPort() != 4000){ //给primary发送acknowledgement
		                	reply = "200".getBytes();
		  					DatagramPacket acknowleagement = new DatagramPacket(reply, reply.length, request.getAddress(), request.getPort()+1000);
		  					datagramSocket.send(acknowleagement);
//		  					buffer = new byte[1000];
//		  					reply = new byte[1000];
                		}
//                		else{ //给FE发送acknowledgement
//                			reply = "200".getBytes();
//		  					DatagramPacket acknowleagement = new DatagramPacket(reply, reply.length, request.getAddress(), request.getPort());
//		  					datagramSocket.send(acknowleagement);
//		  					buffer = new byte[1000];
//		  					reply = new byte[1000];
//                		}
//                	 }
//                	else{
//                		server3.setCount(server3.getCount()+1);
//                	}
  					
  				} catch (IOException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}
                 
//                 if(message != "200"){  //当message是acknowledgement时，不进行操作
	                 if(request.getPort() == 4000){ // 该进程是primary
	//                 if(!message.equals("")){
	     		    	String[] strings = message.split(",");
	     		    	switch(strings[1]){
	     		    		case "1":
	     		    			flag = server3.createTRecord(strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8]);
	     		    			server3.sentMessage(message, 5001);
//	     		    			server3.sentMessage(message, 5002);
	     		    			while(true){ //收到两个acknowledgement时，回复信息
	     		    				if(server3.getCount()==1)
	     		    					break;
	     		    			}
	     		    			server3.setCount(0);
	     		    			if(flag)
	     		    				reply = "SUCCESS".getBytes();
	     		    			else 
	 								reply = "FAIL".getBytes();
	     		    			break;
	     		    		case "2":
	     		    			flag = server3.createSRecord(strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
	     		    			server3.sentMessage(message, 5001);
//	     		    			server3.sentMessage(message, 5002);
	     		    			while(true){ //收到两个acknowledgement时，回复信息
	     		    				if(server3.getCount()==1)
	     		    					break;
	     		    			}
	     		    			server3.setCount(0);
	     		    			if(flag)
	     		    				reply = "SUCCESS".getBytes();
	     		    			else 
	 								reply = "FAIL".getBytes();
	     		    			break;
	     		    		case "3":
	     		    			replyMessage = server3.getRecordCounts(strings[2]);
	     		    			reply = replyMessage.getBytes();
	     		    			break;
	     		    		case "4":
	     		    			flag = server3.editRecord(strings[2], strings[3], strings[4], strings[5]);
	     		    			server3.sentMessage(message, 5001);
//	     		    			server3.sentMessage(message, 5002);
	     		    			while(true){ //收到两个acknowledgement时，回复信息
	     		    				if(server3.getCount()==1)
	     		    					break;
	     		    			}
	     		    			server3.setCount(0);
	     		    			if(flag)
	     		    				reply = "SUCCESS".getBytes();
	     		    			else 
	 								reply = "FAIL".getBytes();
	     		    			break;
	     		    		case "5":
	     		    			flag = server3.transferRecord(strings[2], strings[3], strings[4]);
	     		    			server3.sentMessage(message, 5001);
//	     		    			server3.sentMessage(message, 5002);
	     		    			while(true){ //收到两个acknowledgement时，回复信息
	     		    				if(server3.getCount()==1)
	     		    					break;
	     		    			}
	     		    			server3.setCount(0);
	     		    			if(flag)
	     		    				reply = "SUCCESS".getBytes();
	     		    			else 
	 								reply = "FAIL".getBytes();
	     		    			break;
	     		    		case "7":
	     		    			replyMessage = server3.getRecordInfo(strings[2],strings[3]);
	     		    			reply = replyMessage.getBytes();
	     		    			break;
	     		    		default:
	     		    			System.out.println("error!");
	     		    	}
	//     		    	message="";
	     		    	
	     				try {
	//     					System.out.println("reply: "+String.valueOf(reply));
	     					DatagramPacket replyPacket = new DatagramPacket(reply, reply.length, request.getAddress(), request.getPort());
	     					datagramSocket.send(replyPacket);
	     					buffer = new byte[1000];
	     					reply = new byte[1000];
	     					
	     				} catch (IOException e) {
	     					// TODO Auto-generated catch block
	     					e.printStackTrace();
	     				}
	                 }
	                 else{    //这是一个backup
	                	 String[] strings = message.split(",");
	      		    	switch(strings[1]){
	      		    		case "1":
	      		    			flag = server3.createTRecord(strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8]);
	//      		    			if(flag)
	//      		    				reply = "SUCCESS".getBytes();
	//      		    			else 
	//  								reply = "FAIL".getBytes();
	      		    			break;
	      		    		case "2":
	      		    			flag = server3.createSRecord(strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
	//      		    			if(flag)
	//      		    				reply = "SUCCESS".getBytes();
	//      		    			else 
	//  								reply = "FAIL".getBytes();
	      		    			break;
	      		    		case "3":
	//      		    			replyMessage = server3.getRecordCounts(strings[1]);
	//      		    			reply = replyMessage.getBytes();
	      		    			break;
	      		    		case "4":
	      		    			flag = server3.editRecord(strings[2], strings[3], strings[4], strings[5]);
	//      		    			if(flag)
	//      		    				reply = "SUCCESS".getBytes();
	//      		    			else 
	//  								reply = "FAIL".getBytes();
	      		    			break;
	      		    		case "5":
	      		    			flag = server3.transferRecord(strings[2], strings[3], strings[4]);
	//      		    			if(flag)
	//      		    				reply = "SUCCESS".getBytes();
	//      		    			else 
	//  								reply = "FAIL".getBytes();
	      		    			break;
	      		    		case "7":
	//      		    			replyMessage = server3.getRecordInfo(strings[1],strings[2]);
	//      		    			reply = replyMessage.getBytes();
	      		    			break;
	      		    		default:
	      		    			System.out.println("error!");
	      		    	}
	//      		    	message="";
	      		    	
	//      				try {
	////      					System.out.println("reply: "+String.valueOf(reply));
	//      					DatagramPacket replyPacket = new DatagramPacket(reply, reply.length, request.getAddress(), request.getPort());
	//      					datagramSocket.send(replyPacket);
	//      					buffer = new byte[1000];
	//      					reply = new byte[1000];
	//      					
	//      				} catch (IOException e) {
	//      					// TODO Auto-generated catch block
	//      					e.printStackTrace();
	//      				}
	                 }
	                 }
//             }
//             }
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }finally {
             if(datagramSocket != null)
                 datagramSocket.close();
         }
    		
    	
    }
	
	
	 private void sentMessage(String messageString,int primaryPortNo){
	        DatagramSocket datagramSocket = null;
//	        String replyString=null;
	        
//	        Queue<String> queue = new LinkedBlockingQueue<String>();
//	        Queue<Thread> threadList = new LinkedBlockingQueue<Thread>();
//	        TimerTaskRun timerTaskRun = null;
//	        Lock lock = new ReentrantLock();
	        System.out.println("sending message");
	        try {
	            datagramSocket = new DatagramSocket(6003);
	            byte[] message = messageString.getBytes();
	            InetAddress host = InetAddress.getByName("localhost");

	            DatagramPacket request = new DatagramPacket(message, message.length,host,primaryPortNo);
	            datagramSocket.send(request);
//	            System.out.println("poryNum: "+ primaryPortNo);

//	            //get message
//	            byte[] buffer = new byte[1000];
//	            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
//	            datagramSocket.receive(reply);
	            
	            
//	            String recvStr = new String(reply.getData()).trim();
//	            // 200 means all RMs processed the task successfully
//	            if (recvStr.equals("SUCCESS")) {
//
//	                // clear the clock
//	                Thread thread2 = threadList.remove();
//	                thread2.stop();
//	                System.out.println("Killed");
//
//	                // send the next task if any
//	                if (queue.size() != 0) {
//	                    queue.remove();
//
//	                    if (queue.size() != 0) {
//	                        // get the next message
//	                        final String head = queue.peek();
//
//	                        timerTaskRun = new TimerTaskRun(datagramSocket, head, request);
//	                        Thread thread = new Thread(timerTaskRun);
//	                        threadList.add(thread);
//	                        thread.start();
//	                    }
//	                } else {
//	                }
//	            } else {
//	                // if it's a task message
//	                lock.lock();
//	                queue.add(recvStr);
//	                if (queue.size() == 1) {
//	                    lock.unlock();
//	                    // if the queue was empty, we send the task straight
//	                    final String head = queue.peek();
//	                    timerTaskRun = new TimerTaskRun(datagramSocket, head, request);
//	                    Thread thread = new Thread(timerTaskRun);
//	                    threadList.add(thread);
//	                    thread.start();
//	                } else {
//	                    lock.unlock();
//	                }
//	            }
	            
	            
//	            replyString=new String(reply.getData()).trim();
	        } catch (Exception e) {
	            System.out.println(e.getMessage());
	        }finally {
	            if(datagramSocket != null)
	                datagramSocket.close();
	        }
//	        return replyString;
	    }
    
	
	
	
//	 public void multicast(String message){
//	    	//Multicast
//	    	// args give message contents & destination multicast group (e.g. "228.5.6.7")
//	    	MulticastSocket socket = null;
//	        try{
////	        	System.setProperty("java.net.preferIPv4Stack", "true");
//	        	InetAddress group = InetAddress.getByName("228.5.6.7");
//	        	socket = new MulticastSocket(6789);
//	        	socket.joinGroup(group);
//	        	byte[] m = message.getBytes();
//	        	DatagramPacket messageOut = new DatagramPacket(m, m.length,group,6789);
//	        	socket.send(messageOut);
//	        	byte[] buffer = new byte[1000];
//	        	for(int i=0;i<2;i++){  // get messages from others in group
////	        		System.out.println("receiving...");
//	        		DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
//	        		socket.receive(messageIn);
//	        		System.out.println("Recieve:"+ new String(messageIn.getData()));
//	        	}
//	        	System.out.println("Server3");
//	        	socket.leaveGroup(group);
//	        }catch(SocketException e){
////	        	System.out.println("Socket: " + e.getMessage());
//	        	e.printStackTrace();
//	        }catch (IOException e) {
//	        	System.out.println("IO: " + e.getMessage());
//			}
//	        finally {
//	        	if(socket != null) 
//	        		socket.close();
//			}
//	    }


	@Override
	public boolean createTRecord(String managerId, String firstName, String lastName, String address, String phone,
			String specialization, String location) {
		TeacherRecord teacherRecord = new TeacherRecord(firstName, lastName, address, phone, specialization, location);
        int beforeNum=getLocalRecordsCount(managerId);
        
        //log
        String log=(new Date().toString()+" - "+managerId+" - creating a teacher record - "+teacherRecord.recordID);
        System.out.println(teacherRecord);

        if(managerId.startsWith("MTL")){
        	storingRecord(teacherRecord,MTLServer3);
            writeLog(log,loggingFileMTL);
		}
		else if(managerId.startsWith("DDO")){
			storingRecord(teacherRecord,DDOServer3);
	        writeLog(log,loggingFileDDO);
		}
		else{
			storingRecord(teacherRecord,LVLServer3);
	        writeLog(log,loggingFileLVL);
		}
        
        //
        int afterNum=getLocalRecordsCount(managerId);
       

        return beforeNum+1<=afterNum;
	}



	@Override
	public boolean createSRecord(String managerId, String firstName, String lastName, String coursesRegistered,
			String status, String date) {
		StudentRecord studentRecord = new StudentRecord(firstName, lastName, coursesRegistered, status, date);
        int beforeNum=getLocalRecordsCount(managerId);
        
        String log=(new Date().toString()+" - "+managerId+" - creating a student record - "+studentRecord.recordID);
        System.out.println(studentRecord);
        
        if(managerId.startsWith("MTL")){
        	storingRecord(studentRecord,MTLServer3);
            writeLog(log,loggingFileMTL);
		}
		else if(managerId.startsWith("DDO")){
			storingRecord(studentRecord,DDOServer3);
	        writeLog(log,loggingFileDDO);
		}
		else{
			storingRecord(studentRecord,LVLServer3);
	        writeLog(log,loggingFileLVL);
		}
        int afterNum=getLocalRecordsCount(managerId);
        
        return beforeNum+1<=afterNum;
	}



	@Override
	public String getRecordCounts(String managerId) {
		String DDONum = String.valueOf(getLocalRecordsCount("DDO1111"));
		 String LVLNum = String.valueOf(getLocalRecordsCount("LVL1111"));
		 String MTLNum = String.valueOf(getLocalRecordsCount("MTL1111"));
		 
		//log
       String log=(new Date().toString()+" - "+managerId+" - get records number ");
       if(managerId.startsWith("MTL")){
           writeLog(log,loggingFileMTL);
		}
		else if(managerId.startsWith("DDO")){
	        writeLog(log,loggingFileDDO);
		}
		else{
	        writeLog(log,loggingFileLVL);
		}

       return "Records Count: DDO:"+DDONum+" | LVL:"+LVLNum+" | MTL:"+MTLNum;
	}



	@Override
	public boolean editRecord(String managerId, String recordID, String fieldName, String newValue) {
		Record targetRecord=null;
		Collection<ArrayList<Record>> arrayListsSet = null;
		if(managerId.startsWith("MTL")){
			arrayListsSet=MTLServer3.values();
		}
		else if(managerId.startsWith("DDO")){
			 arrayListsSet=DDOServer3.values();
		}
		else{
			 arrayListsSet=LVLServer3.values();
		}
      
        for(ArrayList<Record> recordArrayListSet:arrayListsSet){
            for(Record record:recordArrayListSet){
                if(record.recordID.equalsIgnoreCase(recordID))
                    targetRecord=record;
                    break;
            }
        }
        if(targetRecord!=null){
            if(targetRecord instanceof TeacherRecord){
                synchronized (targetRecord) {
                    ((TeacherRecord) targetRecord).setValue(fieldName, newValue);  //shared resource - synchronized
                    System.out.println(targetRecord);
                }
            }
            else {
                synchronized (targetRecord) {
                    ((StudentRecord) targetRecord).setValue(fieldName, newValue);   //shared resource - synchronized
                    System.out.println(targetRecord);
                }
            }
            //log
            String log=(new Date().toString()+" - "+managerId+" - editing the record - "+recordID+" - Success");
            
            if(managerId.startsWith("MTL")){
                writeLog(log,loggingFileMTL);
    		}
    		else if(managerId.startsWith("DDO")){
    	        writeLog(log,loggingFileDDO);
    		}
    		else{
    	        writeLog(log,loggingFileLVL);
    		}
            return true;
        }
        else{
            //log
            String log=(new Date().toString()+" - "+managerId+" - editing the record - "+recordID+"- ERROR:Record not exist");
            
            if(managerId.startsWith("MTL")){
                writeLog(log,loggingFileMTL);
    		}
    		else if(managerId.startsWith("DDO")){
    	        writeLog(log,loggingFileDDO);
    		}
    		else{
    	        writeLog(log,loggingFileLVL);
    		}
            return false;
        }
	}



	@Override
	public boolean transferRecord(String managerId, String recordID, String remoteCenterServerName) {
		Record targetRecord=null;
		Collection<ArrayList<Record>> arrayListsSet = null;
		if(managerId.startsWith("MTL")){
			arrayListsSet=MTLServer3.values();
		}
		else if(managerId.startsWith("DDO")){
			 arrayListsSet=DDOServer3.values();
		}
		else{
			 arrayListsSet=LVLServer3.values();
		}
		
        for(ArrayList<Record> recordArrayListSet : arrayListsSet){
            for(Record record:recordArrayListSet){
                if(record.recordID.equalsIgnoreCase(recordID))
                    targetRecord=record;
                break;
            }
        }
        if(targetRecord==null){
            //log
            String log=(new Date().toString()+" - "+managerId+" - transferring the record - "+recordID+" - "+
            "Error:record not exist");
            
            if(managerId.startsWith("MTL")){
                writeLog(log,loggingFileMTL);
    		}
    		else if(managerId.startsWith("DDO")){
    	        writeLog(log,loggingFileDDO);
    		}
    		else{
    	        writeLog(log,loggingFileLVL);
    		}
            return false;
        }
        else{
            //remove
        	ArrayList<Record> theArrayList = null; 
        	if(managerId.startsWith("MTL")){
        		theArrayList=MTLServer3.get(targetRecord.lastName.charAt(0));
    		}
    		else if(managerId.startsWith("DDO")){
    			theArrayList=DDOServer3.get(targetRecord.lastName.charAt(0));
    		}
    		else{
    			theArrayList=LVLServer3.get(targetRecord.lastName.charAt(0));
    		}
        	
        	theArrayList.remove(targetRecord);
            
            //add
            boolean flag = true;
            if(remoteCenterServerName.startsWith("DDO"))
            	storingRecord(targetRecord,DDOServer3);
            else if(remoteCenterServerName.startsWith("LVL"))
            	storingRecord(targetRecord,LVLServer3);
            else if(remoteCenterServerName.startsWith("MTL"))
            	storingRecord(targetRecord,MTLServer3);
            else
                flag=false;
            //log
            if(flag){
                String log=(new Date().toString()+" - "+managerId+" - transferring the record - "+recordID+" - "+
                        "Success");
                if(managerId.startsWith("MTL")){
                    writeLog(log,loggingFileMTL);
        		}
        		else if(managerId.startsWith("DDO")){
        	        writeLog(log,loggingFileDDO);
        		}
        		else{
        	        writeLog(log,loggingFileLVL);
        		}
            }
            else{
                String log=(new Date().toString()+" - "+managerId+" - transferring the record - "+recordID+" - "+
                        "Fail");
                if(managerId.startsWith("MTL")){
                    writeLog(log,loggingFileMTL);
        		}
        		else if(managerId.startsWith("DDO")){
        	        writeLog(log,loggingFileDDO);
        		}
        		else{
        	        writeLog(log,loggingFileLVL);
        		}
            }
            return flag;
        }
	}



	@Override
	public String getRecordInfo(String managerId, String recordID) {
		Record targetRecord=null;
		Collection<ArrayList<Record>> arrayListsSet = null;
		if(managerId.startsWith("MTL")){
			arrayListsSet=MTLServer3.values();
		}
		else if(managerId.startsWith("DDO")){
			 arrayListsSet=DDOServer3.values();
		}
		else{
			 arrayListsSet=LVLServer3.values();
		}
		
        for(ArrayList<Record> recordArrayListSet:arrayListsSet){
            for(Record record:recordArrayListSet){
                if(record.recordID.equalsIgnoreCase(recordID))
                    targetRecord=record;
                break;
            }
        }
        if(targetRecord!=null)
            return targetRecord.toString();
        else
            return "the record is not exist";
	}
	
	private void storingRecord(Record record,HashMap<Character,ArrayList<Record>> hashMap){
        char cap=record.lastName.charAt(0);
        if(!hashMap.containsKey(cap)){
            ArrayList<Record> newArray=new ArrayList<Record>();
            newArray.add(record);
            hashMap.put(cap,newArray);
        }
        else{
            ArrayList<Record> theArray= hashMap.get(cap);
            theArray.add(record);
        }
    }
	
	 public int getLocalRecordsCount(String managerId){
	        int count=0;
	        
	        Collection<ArrayList<Record>> arrayListsSet = null;
			if(managerId.startsWith("MTL")){
				arrayListsSet=MTLServer3.values();
			}
			else if(managerId.startsWith("DDO")){
				 arrayListsSet=DDOServer3.values();
			}
			else{
				 arrayListsSet=LVLServer3.values();
			}
			
	        for(ArrayList<Record> recordArrayListSet :arrayListsSet){
	            for(Record record:recordArrayListSet){
	                count++;
	            }
	        }
	        return count;
	    }

	
	public void writeLog(String log,File file){
        if(!file.exists())
            return;
        try {
            synchronized (file) {
                FileWriter fileWriter = new FileWriter(file, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(log);
                bufferedWriter.newLine();
                bufferedWriter.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
