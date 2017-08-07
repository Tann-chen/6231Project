package client;

import DCMS.FrontEnd;
import DCMS.FrontEndHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import DCMS.FrontEnd;
import DCMS.FrontEndHelper;
import org.omg.CosNaming.NamingContextExt;


public class Manager{
	private String managerID;
	private FrontEnd frontEnd;
	private static File loggingFile=new File("Manager.txt");

	public Manager(String managerID){
		this.managerID = managerID;
		try{
			ORB orb = ORB.init(new String[]{"-ORBInitialHost", "localhost", "-ORBInitialPort", "1050"}, null);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			String name = "FE";
			frontEnd = FrontEndHelper.narrow(ncRef.resolve_str(name));
		} catch (Exception e) {
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
	}

	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization, String location){
		return frontEnd.createTRecord(managerID,firstName,lastName,address,phone,specialization,location);
	}

	public boolean createSRecord(String firstName, String lastName, String coursesRegistered, String status, String date){
		return frontEnd.createSRecord(managerID,firstName,lastName,coursesRegistered,status,date);
	}

	public String getRecordCounts(){
		return frontEnd.getRecordCounts(managerID);
	}

	public boolean editRecord(String recordID, String fieldName, String newValue){
		return frontEnd.editRecord(managerID,recordID,fieldName,newValue);
	}

	public boolean transferRecord(String recordID, String remoteCenterServerName){
		return frontEnd.transferRecord(managerID,recordID,remoteCenterServerName);
	}

	public String getRecordInfo(String recordID){
		return frontEnd.getRecordInfo(managerID,recordID);
	}

	private static void writelog(String log){
		try {
			synchronized (loggingFile) {
				FileWriter fileWriter = new FileWriter(loggingFile, true);
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
