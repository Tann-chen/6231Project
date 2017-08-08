package frontEnd;

import helper.PortDefinition;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;


public class FailureDetector extends Thread {

    private ArrayList<Integer> replicasList;
    private ArrayList<Integer> heartBeatRecords;
    private int FD_PORT = PortDefinition.FailureDetector;     //failureDetector special FD_PORT
    private int runsTolerant=2;
    private int primary=5001;

    private DatagramSocket datagramSocket = null;
    private InetAddress inetAddress=null;


    public FailureDetector(){
        this.replicasList=new ArrayList<Integer>();
        this.heartBeatRecords=new ArrayList<Integer>();
    }

    public void addServer(int portNo){
        replicasList.add(portNo);
        heartBeatRecords.add(runsTolerant+1);
    }


    public void setPrimary(int primary){
        this.primary=primary;
    }

    @Override
    public void run() {

        try {
            //create belonging socket
            datagramSocket = new DatagramSocket(FD_PORT);
            inetAddress=InetAddress.getByName("localhost");


            byte[] buffer = new byte[500];
            //listening heatBeat
            while(true){
                DatagramPacket heartBeat = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(heartBeat);
                String source=new String(heartBeat.getData());
                System.out.println("FailureDetector:  "+source.trim()+" is alive");

                recording(source);

                if(heartBeatRecords.contains(0)){
                    if(!heartBeatRecords.contains(runsTolerant+1)){  //everyone is ok
                        for(int i=0;i<heartBeatRecords.size();i++){
                            heartBeatRecords.set(i,runsTolerant+1);
                        }
                    }else{
                        int failReplicaIndex=heartBeatRecords.indexOf(runsTolerant+1);   //someone fail
                        System.out.println("FailureDetector:  "+replicasList.get(failReplicaIndex)+" is crashed !!!");

                        //solutions -> start election
                        if(replicasList.get(failReplicaIndex)==primary){
                            int theLastOneHeartBeat = replicasList.get(heartBeatRecords.indexOf(0));
                            if (theLastOneHeartBeat == 5001) {
                                sentMessageForElection(6001);
                            } else if (theLastOneHeartBeat == 5002) {
                                sentMessageForElection(6002);
                            } else if (theLastOneHeartBeat == 5003) {
                                sentMessageForElection(6003);
                            }
                        }

                        //restore
                        for(int i=0;i<heartBeatRecords.size();i++){
                            heartBeatRecords.set(i,runsTolerant+1);
                        }

                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
    }

    public void recording(String source)throws Exception{
        int sourceReplicaNo=Integer.parseInt(source.trim());
        int index=-1;
        for(int i=0;i<replicasList.size();i++){
            if(replicasList.get(i)==sourceReplicaNo){
                index=i;
                break;
            }
        }
        if(index!=-1)
            heartBeatRecords.set(index,heartBeatRecords.get(index)-1);
        else{
            System.out.println("FailureDetector: receive invalid heartBeat package");
            System.out.println(source);
        }
    }


    public void sentMessageForElection(int targetPort){
        try {
            byte[] message = "$ELECTION".getBytes();
            DatagramPacket replyPacket = new DatagramPacket(message, message.length,inetAddress,targetPort);
            datagramSocket.send(replyPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
