package frontEnd;

import DCMS.FrontEnd;
import DCMS.FrontEndHelper;
import DCMS.FrontEndPOA;
import helper.PortDefinition;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import thread.UdpHandler2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class FrontEndImp extends FrontEndPOA {

    private static FrontEndImp frontEnd;
    public static final int FIFO_LISTEN_PORT_NBR = 4999;
    public static int primary_port_nbr = PortDefinition.S1_OPEARION_PORT;
    public static int msgId = 1000;

    private FrontEndImp() {}

    //singleton
    public static FrontEndImp getFrontEnd() {
        if (frontEnd == null)
            frontEnd = new FrontEndImp();
        return frontEnd;
    }

    public static void main(String[] args) {

        //config envir
        FailureDetector failureDetector=new FailureDetector();
        failureDetector.addServer(5001);
        failureDetector.addServer(5002);
        failureDetector.addServer(5003);
        failureDetector.start();



        //run CORBA and listen requests from clients
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(new String[]{"-ORBInitialHost", "localhost", "-ORBInitialPort", "1050"}, null);
            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            // create servant and register it with the ORB
            FrontEndImp frontEndSurvant = FrontEndImp.getFrontEnd();
            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(frontEndSurvant);
            FrontEnd href = FrontEndHelper.narrow(ref);
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            // bind the Object Reference in Naming
            String name = "FE";
            NameComponent path[] = ncRef.to_name(name);
            ncRef.rebind(path, href);

//            FifoUdpListener fifo = new FifoUdpListener(FIFO_LISTEN_PORT_NBR, primary_port_nbr);
//            fifo.run();

            orb.run();
            System.out.println("------");
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }


    public void setPrimaryServer(int primaryPortNo) {this.primary_port_nbr = primaryPortNo;}


    public synchronized int getMsgIdAndIncre() {
        msgId++;
        return msgId - 1;
    }

    @Override
    public boolean createTRecord(String managerId, String firstName, String lastName, String address, String phone, String specialization, String location){
        boolean flag = false;
        String messageString = getMsgIdAndIncre() + ",1," + managerId + "," + firstName + "," + lastName + "," + address + "," + phone + "," + specialization + "," + location;
        String reply = sendMsg2Fifo(messageString);
        if (reply.equals("SUCCESS")){
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean createSRecord(String managerId, String firstName, String lastName, String coursesRegistered, String status, String date) {
        boolean flag = false;
        String messageString = getMsgIdAndIncre() + ",2," + managerId + "," + firstName + "," + lastName + "," + coursesRegistered + "," + status + "," + date;
        String reply = sendMsg2Fifo(messageString);
        if (reply.equals("SUCCESS")){
            flag = true;
        }
        return flag;
    }

    @Override
    public String getRecordCounts(String managerId) {
        String messageString = getMsgIdAndIncre() + ",3," + managerId;
        return sendMsg2Fifo(messageString);
    }

    @Override
    public boolean editRecord(String managerId, String recordID, String fieldName, String newValue) {
        boolean flag = false;
        String messageString = getMsgIdAndIncre() + ",4," + managerId + "," + recordID + "," + fieldName + "," + newValue;
        String reply = sendMsg2Fifo(messageString);
        if (reply.equals("SUCCESS")) {
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean transferRecord(String managerId, String recordID, String remoteCenterServerName) {
        boolean flag = false;
        String messageString = getMsgIdAndIncre() + ",5," + managerId + "," + recordID + "," + remoteCenterServerName;
        String reply = sendMsg2Fifo(messageString);
        if (reply.equals("SUCCESS")) {
            flag = true;
        }
        return flag;
    }

    @Override
    public String getRecordInfo(String manageID, String recordID) {
        String messageString = getMsgIdAndIncre() + ",6," + manageID + "," + recordID;
        return sendMsg2Fifo(messageString);
    }


    private String sendMsg2Fifo(String messageString){
        DatagramSocket datagramSocket = null;
        String replyString = null;

        try {
            datagramSocket = new DatagramSocket(PortDefinition.FE_OPEARION_PORT);
            byte[] message = messageString.getBytes();
            InetAddress host = InetAddress.getByName("localhost");

            DatagramPacket request = new DatagramPacket(message, message.length, host,primary_port_nbr);
            datagramSocket.send(request);
            System.out.println("sent message ");

            //get message
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            
//            String recvStr = new String(reply.getData()).trim();
//            // 200 means all RMs processed the task successfully
//            if (recvStr.equals("200")) {
//
//                // clear the clock
//                Thread thread2 = threadList.remove();
//                thread2.stop();
//                System.out.println("Killed");
//
//                // send the next task if any
//                if (queue.size() != 0) {
//                    queue.remove();
//
//                    if (queue.size() != 0) {
//                        // get the next message
//                        final String head = queue.peek();
//
//                        timerTaskRun = new TimerTaskRun(datagramSocket, head, request);
//                        Thread thread = new Thread(timerTaskRun);
//                        threadList.add(thread);
//                        thread.start();
//                    }
//                } else {
//                }
//            } else {
//                // if it's a task message
//                lock.lock();
//                queue.add(recvStr);
//                if (queue.size() == 1) {
//                    lock.unlock();
//                    // if the queue was empty, we send the task straight
//                    final String head = queue.peek();
//                    timerTaskRun = new TimerTaskRun(datagramSocket, head, request);
//                    Thread thread = new Thread(timerTaskRun);
//                    threadList.add(thread);
//                    thread.start();
//                } else {
//                    lock.unlock();
//                }
//            }
            
            
            replyString=new String(reply.getData()).trim();
            System.out.println("reply String:---"+ replyString);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
		return replyString;
    }

    private void changingPrimary(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket swift=null;
                try {
                    swift = new DatagramSocket(PortDefinition.FE_PRIMARY);
                    InetAddress host = InetAddress.getByName("localhost");
                    byte[] buffer = new byte[200];

                    while(true){
                        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                        swift.receive(request);
                        String message=new String(request.getData());
                        System.out.println("receive a message to change primary "+request.getPort());
                        if(message.trim().equals("$PRIMARY")){
                            primary_port_nbr=(request.getPort()-1000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("-------");
                }finally {
                    if(swift != null)
                        swift.close();
                }
            }
        }).start();
    }
}
