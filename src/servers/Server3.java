package servers;


import helper.HeartBeat;
import helper.PortDefinition;
import helper.Timeout;
import records.Record;
import records.StudentRecord;
import records.TeacherRecord;
import thread.BullyElector3;
import thread.UdpHandler3;

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

public class Server3 implements CenterServer{


    private HashMap<Character,ArrayList<Record>> DDOServer;
    private HashMap<Character,ArrayList<Record>> MTLServer;
    private HashMap<Character,ArrayList<Record>> LVLServer;
    private File loggingFileDDO = new File("DDOServer3.txt");
    private File loggingFileMTL = new File("MTLServer3.txt");
    private File loggingFileLVL = new File("LVLServer3.txt");


    public Server3() {
        this.DDOServer = new HashMap<Character,ArrayList<Record>>();
        this.MTLServer = new HashMap<Character,ArrayList<Record>>();
        this.LVLServer = new HashMap<Character,ArrayList<Record>>();
    }

    public static void main(String[] args){
        //config envir
        Server3 server3 =new Server3();
        DatagramSocket datagramSocket=null;
        DatagramSocket acknowSocket=null;
        InetAddress inetAddress;

        //histories
        init();

        //heartbeat
        HeartBeat heartBeat=new HeartBeat(PortDefinition.S3_OPEARION_PORT);
        heartBeat.startUp();

        //bully
        BullyElector3 bullyElector=new BullyElector3(PortDefinition.S3_ELECTION_PORT);
        bullyElector.start();


        try {
            //environment config
            datagramSocket = new DatagramSocket(PortDefinition.S3_OPEARION_PORT);
            acknowSocket = new DatagramSocket(PortDefinition.S3_ACKOWLEDGE_PORT);
            InetAddress host = InetAddress.getByName("localhost");


            byte[] buffer = new byte[1000];
            while(true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(request);
                String message=new String(request.getData());

                if(!message.trim().equals("200")){      // not acknowledgment
                    byte[] acknowledge = "200".getBytes();

                    DatagramPacket acknow=null;

                    if(request.getPort()==PortDefinition.FE_INITIAL_PORT){
                        acknow = new DatagramPacket(acknowledge, acknowledge.length,host, PortDefinition.FE_INITIAL_PORT);
                        System.out.print("Server3: receive init work packet");
                    }
                    else
                        acknow = new DatagramPacket(acknowledge, acknowledge.length,host,(request.getPort()-1000));

                    datagramSocket.send(acknow);

                    new UdpHandler3(host,datagramSocket,acknowSocket, server3,request).start();
                    buffer=new byte[1000];
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("-------");
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
    }


    @Override
    public boolean createTRecord(String managerId, String firstName, String lastName, String address, String phone, String specialization, String location) {TeacherRecord teacherRecord = new TeacherRecord(firstName, lastName, address, phone, specialization, location);


        //log
        String log=(new Date().toString()+" - "+managerId+" - creating a teacher record - "+teacherRecord.recordID);
        System.out.println(teacherRecord);

        if(managerId.trim().startsWith("MTL")){
            storingRecord(teacherRecord,MTLServer);
            writeLog(log,loggingFileMTL);
        }
        else if(managerId.trim().startsWith("DDO")){
            storingRecord(teacherRecord,DDOServer);
            writeLog(log,loggingFileDDO);
        }
        else{
            storingRecord(teacherRecord,LVLServer);
            writeLog(log,loggingFileLVL);
        }

        return true;
    }



    @Override
    public boolean createSRecord(String managerId, String firstName, String lastName, String coursesRegistered, String status, String date) {

        StudentRecord studentRecord = new StudentRecord(firstName, lastName, coursesRegistered, status, date);

        String log=(new Date().toString()+" - "+managerId+" - creating a student record - "+studentRecord.recordID);
        System.out.println(studentRecord);

        if(managerId.trim().startsWith("MTL")){
            storingRecord(studentRecord,MTLServer);
            writeLog(log,loggingFileMTL);
        }
        else if(managerId.trim().startsWith("DDO")){
            storingRecord(studentRecord,DDOServer);
            writeLog(log,loggingFileDDO);
        }
        else{
            storingRecord(studentRecord,LVLServer);
            writeLog(log,loggingFileLVL);
        }


        return true;
    }


    @Override
    public String getRecordCounts(String managerId) {
        String DDONum = String.valueOf(getLocalRecordsCount("DDO"));
        String LVLNum = String.valueOf(getLocalRecordsCount("LVL"));
        String MTLNum = String.valueOf(getLocalRecordsCount("MTL"));

        //log
        String log=(new Date().toString()+" - "+managerId+" - get records number ");
        if(managerId.trim().startsWith("MTL")){
            writeLog(log,loggingFileMTL);
        }
        else if(managerId.trim().startsWith("DDO")){
            writeLog(log,loggingFileDDO);
        }
        else{
            writeLog(log,loggingFileLVL);
        }

        return "Records Count: DDO:"+DDONum+" | LVL:"+LVLNum+" | MTL:"+MTLNum;
    }



    @Override
    public boolean editRecord(String managerId, String recordID, String fieldName, String newValue){
        Record targetRecord=null;
        Collection<ArrayList<Record>> arrayListsSet = null;

        if(managerId.trim().startsWith("MTL")){
            arrayListsSet=MTLServer.values();
        }
        else if(managerId.trim().startsWith("DDO")){
            arrayListsSet=DDOServer.values();
        }
        else{
            arrayListsSet=LVLServer.values();
        }

        for(ArrayList<Record> recordArrayListSet:arrayListsSet){
            for(Record record:recordArrayListSet){
                if(record.recordID.equalsIgnoreCase(recordID.trim()))
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

            if(managerId.trim().startsWith("MTL")){
                writeLog(log,loggingFileMTL);
            }
            else if(managerId.trim().startsWith("DDO")){
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

            if(managerId.trim().startsWith("MTL")){
                writeLog(log,loggingFileMTL);
            }
            else if(managerId.trim().startsWith("DDO")){
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
        Collection<ArrayList<Record>> arrayListsSet;

        if(managerId.trim().startsWith("MTL")){
            arrayListsSet=MTLServer.values();
        }
        else if(managerId.trim().startsWith("DDO")){
            arrayListsSet=DDOServer.values();
        }
        else{
            arrayListsSet=LVLServer.values();
        }

        for(ArrayList<Record> recordArrayListSet : arrayListsSet){
            for(Record record:recordArrayListSet){
                if(record.recordID.equalsIgnoreCase(recordID.trim()))
                    targetRecord=record;
                break;
            }
        }

        if(targetRecord==null){
            //log
            String log=(new Date().toString()+" - "+managerId+" - transferring the record - "+recordID+" - "+
                    "Error:record not exist");

            if(managerId.trim().startsWith("MTL")){
                writeLog(log,loggingFileMTL);
            }
            else if(managerId.trim().startsWith("DDO")){
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
            if(managerId.trim().startsWith("MTL")){
                theArrayList=MTLServer.get(targetRecord.lastName.charAt(0));
            }
            else if(managerId.trim().startsWith("DDO")){
                theArrayList=DDOServer.get(targetRecord.lastName.charAt(0));
            }
            else{
                theArrayList=LVLServer.get(targetRecord.lastName.charAt(0));
            }

            theArrayList.remove(targetRecord);

            //add
            boolean flag = true;
            if(remoteCenterServerName.trim().startsWith("DDO"))
                storingRecord(targetRecord,DDOServer);
            else if(remoteCenterServerName.trim().startsWith("LVL"))
                storingRecord(targetRecord,LVLServer);
            else if(remoteCenterServerName.trim().startsWith("MTL"))
                storingRecord(targetRecord,MTLServer);
            else
                flag=false;
            //log
            if(flag){
                String log=(new Date().toString()+" - "+managerId+" - transferring the record - "+recordID+" - "+
                        "Success");
                if(managerId.trim().startsWith("MTL")){
                    writeLog(log,loggingFileMTL);
                }
                else if(managerId.trim().startsWith("DDO")){
                    writeLog(log,loggingFileDDO);
                }
                else{
                    writeLog(log,loggingFileLVL);
                }
            }
            else{
                String log=(new Date().toString()+" - "+managerId+" - transferring the record - "+recordID+" - "+
                        "Fail");
                if(managerId.trim().startsWith("MTL")){
                    writeLog(log,loggingFileMTL);
                }
                else if(managerId.trim().startsWith("DDO")){
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
    public String getRecordInfo(String managerId, String recordID){
        Record targetRecord=null;
        Collection<ArrayList<Record>> arrayListsSet = null;

        if(managerId.trim().startsWith("MTL")){
            arrayListsSet=MTLServer.values();
        }
        else if(managerId.trim().startsWith("DDO")){
            arrayListsSet=DDOServer.values();
        }
        else{
            arrayListsSet=LVLServer.values();
        }

        for(ArrayList<Record> recordArrayListSet:arrayListsSet){
            for(Record record:recordArrayListSet){
                if(record.recordID.equalsIgnoreCase(recordID.trim()))
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

    public int getLocalRecordsCount(String serverName){

        int count=0;

        Collection<ArrayList<Record>> arrayListsSet = null;
        if(serverName.trim().equals("MTL")){
            arrayListsSet=MTLServer.values();
        }
        else if(serverName.trim().equals("DDO")){
            arrayListsSet=DDOServer.values();
        }
        else{
            arrayListsSet=LVLServer.values();
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

    private static void init(){
        DatagramSocket socket=null;

        try{
            socket=new DatagramSocket(PortDefinition.S3_OPEARION_PORT);
            InetAddress host = InetAddress.getByName("localhost");
            byte[] message = "$INIT".getBytes();
            DatagramPacket replyPacket = new DatagramPacket(message, message.length, host,PortDefinition.FE_INITIAL_PORT);
            socket.send(replyPacket);


            byte[] bullyMsg = "$PRIMARY".getBytes();
            DatagramPacket bully = new DatagramPacket(bullyMsg, bullyMsg.length, host,PortDefinition.FE_PRIMARY);
            socket.send(bully);


        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(socket!=null)
                socket.close();
        }

    }

}
