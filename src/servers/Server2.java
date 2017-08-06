package servers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import records.Record;
import records.StudentRecord;
import records.TeacherRecord;
import thread.UdpHandler;


public class Server2 implements CenterServer {

    final static String[] SchoolServers = {"MTL", "LVL", "DDO"};
    private static Server2[] schoolServersObjs = new Server2[3];   //this server is centerServer actually
    private HashMap<Character,ArrayList<Record>> records;
    private String name;


    public Server2(String SchoolServer) {
        this.name = SchoolServer;
        this.records=new HashMap<Character,ArrayList<Record>>();
        logFile(name, "Server " + name + "is running");
    }


    public static void main(String[] args) {
        //config three centers in the replica
        for (int i=0; i<3;i++) {
            Server2 center = new Server2(SchoolServers[i]);
            schoolServersObjs[i]=center;
        }


        //replica environment
        int port=5001;
        DatagramSocket datagramSocket = null;
        String message;
        try {
            datagramSocket = new DatagramSocket(port);
            byte[] buffer = new byte[1000];

            while(true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(request);
                message=new String(request.getData());
                new UdpHandler(request.getAddress(),request.getPort(),datagramSocket,schoolServersObjs,message).run();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("-------");
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }


    }


    public static void multicast(){

        //Multicast
        // args give message contents & destination multicast group (e.g. "228.5.6.7")
        MulticastSocket socket = null;
        try{
            System.setProperty("java.net.preferIPv4Stack", "true");
            InetAddress group = InetAddress.getByName("228.5.6.7");
            socket = new MulticastSocket(6789);
            socket.joinGroup(group);
            byte[] m = "Server2 nihao".getBytes();
            DatagramPacket messageOut = new DatagramPacket(m, m.length,group,6789);

            byte[] buffer = new byte[1000];
            for(int i=0;i<=2;i++){  // get messages from others in group
                System.out.println("receiving");
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                socket.receive(messageIn);
                System.out.println("Recieve:"+ new String(messageIn.getData()));
                socket.send(messageOut);
            }
            System.out.println("Server2");
            //    	socket.leaveGroup(group);
        }catch(SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
        finally {
            if(socket != null)
                socket.close();
        }
    }


    public static void logFile(String fileName, String Operation)throws SecurityException {

        fileName = fileName+"Server2.txt";
        File log = new File(fileName);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        try {
            if (!log.exists()) {
            }
            log.setWritable(true);
            FileWriter fileWriter = new FileWriter(log, true);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(Operation + " "
                    + dateFormat.format(date));
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("COULD NOT LOG!!");
        }
    }

    @Override
    public boolean createTRecord(String managerID, String firstName, String lastName, String address, String phone, String specialization, String location) {
        TeacherRecord record = new TeacherRecord(firstName, lastName, address, phone, specialization, location);
        storingRecord(record);
        logFile(this.name, " Create Teacher Record: " + record.toString());
        return true;
    }

    @Override
    public boolean createSRecord(String managerID, String firstName, String lastName, String courseRegistered, String status, String statusDate) {
        StudentRecord record = new StudentRecord(firstName, lastName, courseRegistered, status, statusDate);
        storingRecord(record);
        logFile(this.name, " Create Student Record2: " + record.toString());
        return true;
    }

    @Override
    public String getRecordCounts(String managerID) {
        String counts="";
        for(int i=0;i<3;i++){
            int count=schoolServersObjs[i].getLocalRecordsCount();
            counts=counts+SchoolServers[i]+" : "+count+" | ";
        }

        logFile(this.name, "Get the record count --" + "\n" + counts);
        return counts;
    }


    @Override
    public boolean editRecord(String managerID, String recordID, String fieldName, String newValue) {
        Record targetRecord = null;

        Collection<ArrayList<Record>> arrayListsSet = records.values();
        for (ArrayList<Record> recordArrayListSet : arrayListsSet) {
            for (Record record : recordArrayListSet) {
                if (record.recordID.equalsIgnoreCase(recordID))
                    targetRecord = record;
                break;
            }
        }
        if (targetRecord == null) return false;

        if (recordID.contains("TR")) {
            synchronized (targetRecord) {
                ((TeacherRecord) targetRecord).setValue(fieldName, newValue);
            }
        } else if (recordID.contains("SR")) {
            synchronized (targetRecord) {
                ((StudentRecord) targetRecord).setValue(fieldName, newValue);
            }
        }

        logFile(this.name, "Manger has edited the " + fieldName + " of " + recordID + " to new value: " + newValue);
        return true;
    }


    @Override
    public boolean transferRecord(String managerID, String recordID, String remoteSchoolServerName) {
        if (!Arrays.asList(SchoolServers).contains(remoteSchoolServerName.toUpperCase())) {
            logFile(this.name, remoteSchoolServerName + " server is not in the list - ERROR");
            return false;
        }

        Record targetRecord=null;

        Collection<ArrayList<Record>>arrayListsSet=records.values();
        for(ArrayList<Record> recordArrayListSet : arrayListsSet){
            for(Record record:recordArrayListSet){
                if(record.recordID.equalsIgnoreCase(recordID))
                    targetRecord=record;
                break;
            }
        }
        if(targetRecord==null){
            //log
            logFile(this.name, remoteSchoolServerName + " record is not in the list - ERROR");
            return false;
        }else{
            ArrayList<Record>theArrayList=records.get(targetRecord.lastName.charAt(0));
            synchronized (targetRecord) {
                theArrayList.remove(targetRecord);
            }
            logFile(name,"transfer records : remove record - "+recordID);

            int index=-1;
            for(int i=0;i<3;i++){
                if(SchoolServers[i].equals(remoteSchoolServerName)){
                    index=i;
                    break;
                }
            }

            //add
            if(index!=-1){
                if (recordID.startsWith("TR")){
                    schoolServersObjs[index].createTRecord(managerID,targetRecord.firstName,targetRecord.lastName,targetRecord.lastName,
                            ((TeacherRecord) targetRecord).phone,((TeacherRecord) targetRecord).specialization,((TeacherRecord) targetRecord).location);
                }else{
                    schoolServersObjs[index].createSRecord(managerID,targetRecord.firstName,targetRecord.lastName,((StudentRecord) targetRecord).coursesRegistered,
                            ((StudentRecord) targetRecord).status,((StudentRecord) targetRecord).date);
                }
                logFile(schoolServersObjs[index].name,"transfer records : add record :");
            }
        }
        return true;
    }



    @Override
    public String getRecordInfo(String managerId, String recordID) {
        Record targetRecord=null;

        Collection<ArrayList<Record>>arrayListsSet=records.values();
        for(ArrayList<Record> recordArrayListSet : arrayListsSet){
            for(Record record:recordArrayListSet){
                if(record.recordID.equalsIgnoreCase(recordID))
                    targetRecord=record;
                break;
            }
        }
        if(targetRecord==null){
            logFile(name,"get record : can not find record - ERROR");
            return "ERROR : The record id is invalid";
        }else{
            logFile(name,"get record :"+targetRecord.toString());
            return targetRecord.toString();
        }
    }


    private synchronized void storingRecord(Record record){
        char cap=record.lastName.charAt(0);
        if(!records.containsKey(cap)){
            ArrayList<Record> newArray=new ArrayList<Record>();
            newArray.add(record);
            records.put(cap,newArray);
        }
        else{
            ArrayList<Record> theArray= records.get(cap);
            theArray.add(record);
        }
    }

    public int getLocalRecordsCount(){
        int count=0;
        Collection<ArrayList<Record>> arrayListsSet=records.values();
        for(ArrayList<Record> recordArrayListSet :arrayListsSet){
            for(Record record:recordArrayListSet){
                count++;
            }
        }
        return count;
    }
}
