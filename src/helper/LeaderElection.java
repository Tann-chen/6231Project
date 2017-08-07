package helper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class LeaderElection {

    private boolean isLeaderFlag = false;
    private boolean isElectingFlag = false;

    public String serverName;
    public String serverIP;
    public int serverID;
    public int serverPort;
    public int leaderElectionPort;
    public ServerListener rl = new ServerListener(serverName,leaderElectionPort);

    public int FE_port = 5000;
    public String FE_IP = "localhost";
    public ServerInfo[] gm = new ServerInfo[3];

    public LeaderElection(String serverName,String serverIP, int serverID,int serverPort,int leaderElectionPort){
        this.serverName = serverName;
        this.serverIP = serverIP;
        this.serverID = serverID;
        this.serverPort = serverPort;
        this.leaderElectionPort = leaderElectionPort;

        gm[0] = new helper.ServerInfo("server1", "localhost", 9001, 1, 8001);
        gm[1] = new helper.ServerInfo("server2", "localhost", 9002, 2, 8002);
        gm[2] = new helper.ServerInfo("server3", "localhost", 9003, 3, 8003);
        new Thread(rl).start();
    }

    //问FE谁是primary,返回primary_port
    public int initialElection() {
        DatagramSocket socket = null;
        try {
            String msg = "Is there a leader?";
            socket = new DatagramSocket();
            InetAddress FE_addr = InetAddress.getByName(FE_IP);
            byte[] sendData = msg.getBytes();
            byte[] receiveData = new byte[4096];
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, FE_addr, FE_port);
            socket.send(sendPacket);     //1.问FE有没有primary
            System.out.println(serverName + " is Sending message to FE: " + msg);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);    //receive from FE
            String out = new String(receivePacket.getData()).trim();
            System.out.println(serverName + " received message from FE: " + out);
            if (!Boolean.parseBoolean(out)){   //2.没有的话，把自己的portnum发给FE
                sendData = (Integer.toString(serverPort).getBytes());
                sendPacket = new DatagramPacket(sendData, sendData.length, FE_addr, FE_port);
                socket.send(sendPacket);
                System.out.println(serverName + " is sending local port to FE: " + Integer.toString(serverPort));
                socket.receive(receivePacket);
                out = new String(receivePacket.getData()).trim();
                System.out.println(out);   //收到from FE
                if (out.equalsIgnoreCase("succeed"))
                    this.isLeaderFlag = true;
                else
                    System.out.println("Error in leader election!");
                return serverPort;
            }
            else {         //2.如果有primary了-问谁是老大
                sendData = "leader?".getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length, FE_addr, FE_port);
                socket.send(sendPacket);
                System.out.println(serverName + " is questing leader port from FE.");
                socket.receive(receivePacket);
                out = new String(receivePacket.getData()).trim();
                System.out.println(serverName + " received leader port: " + out);
                return Integer.parseInt(out);
            }

        }catch (Exception e) {
            return -1;
        }finally{
            if(socket != null) socket.close();
        }
    }

    //发起选举，返回leader_port_num
    public int ElectLeader(){
        int newLeaderPort = -1;
        try {
            this.setElectingFlag(true);   //由它发起选举的
            System.out.println(serverName + " is holding an election.");
            if (this.isLeaderFlag){    //如果本是leader
                System.out.println(serverName + "is still leader.");
                newLeaderPort = serverPort;
                notifyNewLeader(FE_IP ,FE_port); //通知FE,它本身是primary
                for (int i = 0; i < gm.length; i++){
                    if (gm[i].getElection_port() > leaderElectionPort){    //通知port_num比它port_num大的，它是primary
                        notifyNewLeader(gm[i].getIP(), gm[i].getElection_port());
                    }
                }
            }
            else{       //如果它本身现在不是leader
                if(!sendMessage())
                {//elect self as co-ordinator
                    newLeaderPort = serverPort;    //自己作为leader
                    notifyNewLeader(FE_IP ,FE_port);
                    for (int i = 0; i < gm.length; i++){         //通知FE和比他大的，他是leader
                        if (gm[i].getElection_port() > leaderElectionPort){
                            notifyNewLeader(gm[i].getIP(), gm[i].getElection_port());
                        }
                    }
                }
                else
                    newLeaderPort = receiveLeader(serverIP, leaderElectionPort);  //接收信息，获得leader_port_num
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return newLeaderPort;

    }

    //notify IP,port that I am leader
    private void notifyNewLeader(String addr, int port) {
        String msg = Integer.toString(serverPort);
        DatagramSocket socket = null;
        try{
            socket = new DatagramSocket();
            InetAddress IP_addr = InetAddress.getByName(addr);
            byte[] sendData = msg.getBytes();
            byte[] receiveData = new byte[4096];
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP_addr, port);
            socket.send(sendPacket);    //把自己的port_num发给指定的IP,port
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String out = new String(receivePacket.getData()).trim();
            if (out.equalsIgnoreCase("succeed")){
                System.out.println("Leader port is updated in " + addr + ": " + port);
                this.isLeaderFlag = true;
            }
            else
                System.out.println("Failed to notify leader port to: " + addr + ": " + port);
        }
        catch(Exception e){
            System.out.println(e);
        }finally{
            if(socket != null) socket.close();
        }
    }

    //让指定的IP,port去接收信息,return收到的信息（port_num）
    private int receiveLeader(String addr, int port) {
        int out = -1;
        DatagramSocket socket = null;
        String msg = "success";
        try{
            socket = new DatagramSocket(port, InetAddress.getByName(addr));
            byte[] receiveData = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            out = Integer.parseInt(new String(receivePacket.getData()).trim());
            byte[] sendData = msg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
            socket.send(sendPacket);     //接收到后，回复success
        }
        catch(Exception e){
            System.out.println("Fail to get the leader port.");
        }finally{
            if(socket != null) socket.close();
        }
        return out;
    }

    private boolean sendMessage() {                   //2->0
        boolean result = false;                       //1->1,0
        for(int i = serverID; i < gm.length; i++){    //0->2,1,0
            try {
                Socket electionMessage = new Socket(InetAddress.getByName(gm[2-i].getIP()), gm[2-i].getElection_port());
                System.out.println( serverName+ " -> " + gm[2-i].getName() + ": respond successfully!");
                electionMessage.close(); //TODO:很奇怪
                result = true;
            } catch (Exception e) {
                System.out.println(serverName + " -> " + gm[2-i].getName() + ": no respond!");
            }
        }
        return result;
    }

    public boolean isLeaderFlag() {
        return isLeaderFlag;
    }

    public void setLeaderFlag(boolean isLeaderFlag) {
        this.isLeaderFlag = isLeaderFlag;
    }

    public boolean isElectingFlag() {
        return isElectingFlag;
    }

    public void setElectingFlag(boolean isElectingFlag) {
        this.isElectingFlag = isElectingFlag;
    }
}
