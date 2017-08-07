package frontEnd;


import DCMS.FrontEnd;
import DCMS.FrontEndHelper;
import DCMS.FrontEndPOA;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FrontEndImp extends FrontEndPOA{

    private static FrontEndImp frontEnd;
    private int primaryPortNo;

    private FrontEndImp(){}
    //singleton
    public static FrontEndImp getFrontEnd(){
        if(frontEnd==null)
            frontEnd=new FrontEndImp();
        return frontEnd;
    }

    public static void main(String[] args){
        int frontEndPortNo=5000;
        //config
        BullySelector.getBullySelector().addServer(5001);
        BullySelector.getBullySelector().addServer(5002);
        BullySelector.getBullySelector().addServer(5003);
        BullySelector.getBullySelector().startUp();

        //start up the periodical detecting
        FailureDetector failureDetector=new FailureDetector();
        failureDetector.addServer(5001);
        failureDetector.addServer(5002);
        failureDetector.addServer(5003);
        failureDetector.start();

        //run CORBA and listen requests from clients
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(new String[]{"-ORBInitialHost", "localhost", "-ORBInitialPort", "1050"}, null);
            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            // create servant and register it with the ORB
            FrontEndImp frontEndSurvant=FrontEndImp.getFrontEnd();
            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(frontEndSurvant);
            FrontEnd href = FrontEndHelper.narrow(ref);
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            // bind the Object Reference in Naming
            String name = "frontEnd";
            NameComponent path[] = ncRef.to_name(name);
            ncRef.rebind(path, href);
            orb.run();
            System.out.println("------");
        }
        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }


    public void setPrimaryServer(int primaryPortNo){
        this.primaryPortNo=primaryPortNo;
    }

    @Override
    public boolean createTRecord(String managerId, String firstName, String lastName, String address, String phone, String specialization, String location) {
        boolean flag = false;
        String messageString="1,"+managerId+","+firstName+","+lastName+","+address+","+phone+","+specialization+","+location;
        String reply=sentMessage(messageString);
        if(reply.equals("SUCCESS")){
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean createSRecord(String managerId, String firstName, String lastName, String coursesRegistered, String status, String date) {
        boolean flag=false;
        String messageString="2,"+managerId+","+firstName+","+lastName+","+coursesRegistered+","+status+","+date;
        String reply=sentMessage(messageString);
        if(reply.equals("SUCCESS")){
            flag = true;
        }
        return flag;
    }

    @Override
    public String getRecordCounts(String managerId) {
        String messageString="3,"+managerId;
        return sentMessage(messageString);
    }

    @Override
    public boolean editRecord(String managerId, String recordID, String fieldName, String newValue) {
        boolean flag=false;
        String messageString="4,"+managerId+","+recordID+","+fieldName+","+newValue;
        String reply=sentMessage(messageString);
        if(reply.equals("SUCCESS")){
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean transferRecord(String managerId, String recordID, String remoteCenterServerName) {
        boolean flag=false;
        String messageString="5,"+managerId+","+recordID+","+remoteCenterServerName;
        String reply=sentMessage(messageString);
        if(reply.equals("SUCCESS")){
            flag = true;
        }
        return flag;
    }

    @Override
    public String getRecordInfo(String manageID,String recordID) {
        String messageString="7,"+manageID+","+recordID;
        return sentMessage(messageString);
    }


    private String sentMessage(String messageString){
        DatagramSocket datagramSocket = null;
        String replyString=null;

        try {
            datagramSocket = new DatagramSocket();
            byte[] message = messageString.getBytes();
            InetAddress host = InetAddress.getByName("localhost");

            DatagramPacket request = new DatagramPacket(message, message.length,host,primaryPortNo);
            datagramSocket.send(request);

            //get message
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            replyString=new String(reply.getData()).trim();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
        return replyString;
    }
}
