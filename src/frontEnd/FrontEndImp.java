package frontEnd;

import DCMS.FrontEnd;
import DCMS.FrontEndHelper;
import DCMS.FrontEndPOA;
import helper.PortDefinition;
import helper.Timeout;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;


public class FrontEndImp extends FrontEndPOA {

    private static FrontEndImp frontEnd;
    public static int primaryPortNbr = PortDefinition.S3_OPEARION_PORT;
    public static int msgId = 1000;
    private static ArrayList<String> historyOprations=new ArrayList<String>();
    private int index=-1;  //index of historyArr


    public static FailureDetector failureDetector;


    private FrontEndImp() {}

    //singleton
    public static FrontEndImp getFrontEnd() {
        if (frontEnd == null)
            frontEnd = new FrontEndImp();
        return frontEnd;
    }

    public static void main(String[] args) {

        //config envir
        failureDetector=new FailureDetector();
        failureDetector.addServer(5001);
        failureDetector.addServer(5002);
        failureDetector.addServer(5003);
        failureDetector.start();

        //functions threads
        listeningForChangingPrimary();
        InitForServer initForServer=new InitForServer(historyOprations);
        initForServer.start();


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
            orb.run();
            System.out.println("------");
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

        try {
            failureDetector.join();
        }catch (Exception e){
            e.getStackTrace();
        }
    }


    public void setPrimaryServer(int primaryPortNo) {primaryPortNbr = primaryPortNo;}

    public static void setPrimary(int newPrimary){
        primaryPortNbr=newPrimary;
        failureDetector.setPrimary(newPrimary);
    }


    public synchronized int getMsgIdAndIncre() {
        msgId++;
        return msgId - 1;
    }

    @Override
    public boolean createTRecord(String managerId, String firstName, String lastName, String address, String phone, String specialization, String location){
        boolean flag = false;
        String messageString = getMsgIdAndIncre() + ",1," + managerId + "," + firstName + "," + lastName + "," + address + "," + phone + "," + specialization + "," + location;
        String reply = sendMsgByQueue(messageString);
        if (reply.equals("SUCCESS")){
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean createSRecord(String managerId, String firstName, String lastName, String coursesRegistered, String status, String date) {
        boolean flag = false;
        String messageString = getMsgIdAndIncre() + ",2," + managerId + "," + firstName + "," + lastName + "," + coursesRegistered + "," + status + "," + date;
        String reply = sendMsgByQueue(messageString);
        if (reply.equals("SUCCESS")){
            flag = true;
        }
        return flag;
    }

    @Override
    public String getRecordCounts(String managerId) {
        String messageString = getMsgIdAndIncre() + ",3," + managerId;
        return sendMsgByQueue(messageString);
    }

    @Override
    public boolean editRecord(String managerId, String recordID, String fieldName, String newValue) {
        boolean flag = false;
        String messageString = getMsgIdAndIncre() + ",4," + managerId + "," + recordID + "," + fieldName + "," + newValue;
        String reply = sendMsgByQueue(messageString);
        if (reply.equals("SUCCESS")) {
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean transferRecord(String managerId, String recordID, String remoteCenterServerName){
        boolean flag = false;
        String messageString = getMsgIdAndIncre() + ",5," + managerId + "," + recordID + "," + remoteCenterServerName;
        String reply = sendMsgByQueue(messageString);
        if (reply.equals("SUCCESS")) {
            flag = true;
        }
        return flag;
    }

    @Override
    public String getRecordInfo(String manageID, String recordID) {
        String messageString = getMsgIdAndIncre() + ",6," + manageID + "," + recordID;
        return sendMsgByQueue(messageString);
    }


    private String sendMsgByQueue(String messageString){

        historyOprations.add(messageString);
        index+=1;

        DatagramSocket datagramSocket = null;
        String replyString = null;
        byte[] buffer = new byte[2000];
        try {
            datagramSocket = new DatagramSocket(PortDefinition.FE_OPEARION_PORT);
            byte[] message = messageString.getBytes();
            InetAddress host = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, host, primaryPortNbr);
            System.out.println("FE: request sent to "+primaryPortNbr);

            datagramSocket.send(request);

//            Timeout timeout=new Timeout(500);
//            timeout.startUp();
//            if(listeningForAcknow(timeout)){
//                datagramSocket.send(request);  //send again
//            }


            //get message
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            replyString=new String(reply.getData()).trim();
            System.out.println("FE receive result: "+ replyString);
            buffer = new byte[2000];
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
		return replyString;
    }

    private static void listeningForChangingPrimary(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket swift=null;
                try {
                    swift = new DatagramSocket(PortDefinition.FE_PRIMARY);
                    InetAddress host = InetAddress.getByName("localhost");
                    byte[] buffer = new byte[2000];

                    while(true){
                        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                        swift.receive(request);
                        String message=new String(request.getData());
                        if(message.trim().equals("$PRIMARY")){
                            setPrimary(request.getPort()-1000);
                        }
                        System.out.println("Now: the primary is changed to"+ primaryPortNbr);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }finally {
                    if(swift != null)
                        swift.close();
                }
            }
        }).start();
    }


    private boolean listeningForAcknow(Timeout timeout){
        DatagramSocket acknowSocket =null;
        boolean flag=false;
        try{
            while (timeout.flag){
                acknowSocket = new DatagramSocket(PortDefinition.FE_ACKOWLEDGE_PORT);
                byte[] buffer = new byte[500];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                acknowSocket.receive(reply);
                if(reply.getPort()==primaryPortNbr){
                    flag=true;
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(acknowSocket!=null)
                acknowSocket.close();
        }
        return flag;
    }

}
