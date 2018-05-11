package examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class Test1 {
	
	
	private static List<Tasks> taskList;
	
	/** The vmlist. */
	private static List<Vm> vmlist;
	
	private static int totalDC = 1;
	private static int totalTaskType = 2;
	private static ListMultimap<String, Double> executionTimes = ArrayListMultimap.create();
	private static HashMap<String, NormalDistribution> distributions = new HashMap<>();
	private static HashMap<String, NormalDistribution> trTimes = new HashMap<>();
	
	private static double ETC[][];
	
	private static List<Vm> createVM(int userId, int mips, int vms, int idShift) {
		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		//VM Parameters
		long size = 10000; //image size (MB)
		int ram = 512; //vm memory (MB)
		long bw = 1000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name

		//create VMs
		Vm[] vm = new Vm[vms];

		for(int i=0;i<vms;i++){
			vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
			list.add(vm[i]);
		}

		return list;
	}
	
	private static int gaussianMiGenerator(double sd,double mean, Random r,double mincap,double maxcap) {
		
		double Result = mean+r.nextGaussian()*sd;
		if(Result < mincap) {
			Result = 2*mincap-Result;
		}
		else if(Result > maxcap) {
		Result=Math.abs(2*maxcap- Result);
			
		}
		return (int)Result;
	}
	
	
	
	
	private static List<Tasks> createTasks(int userId, int cloudlets, int idShift, int START, int END) throws NumberFormatException, IOException{
		// Creates a container to store Cloudlets
		LinkedList<Tasks> list = new LinkedList<Tasks>();
		
		double[] arrList = new double[24000];
		File file = new File("/home/c00303945/eclipse-workspace/SummerProject/arrival1.dat");
		
		BufferedReader br = new BufferedReader(new FileReader(file));
	       
	       try {
	           int index = 0;
	           String line = null;
	           while ((line= br.readLine())!=null) {
	              
	               String[] lineArray = line.split(",");
	               arrList[index]= Double.parseDouble(lineArray[2]);
	               index++;
	           							} 
	       }catch (FileNotFoundException e) {
	           e.printStackTrace();
	       } 

		//cloudlet parameters
		long length = 4000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		int taskType = 1; 
		double arrivalTime = 0.0 ; 
		double deadline = 0.0;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Tasks[] task = new Tasks[cloudlets];
		Random rObj = new Random();
		rObj.setSeed(300);
		

		for(int i=0;i<cloudlets;i++){
			
			length = gaussianMiGenerator(500, 3800,rObj,START,END);
			
			if(length < 3800) {
				taskType = 1;
			}
			else {
				taskType = 2;
			}
			
			
			
			task[i] = new Tasks(idShift + i, taskType,length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel, deadline , arrList[i]);
			task[i].setUserId(userId);
			list.add(task[i]);
		}

		return list;
	}
	


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Log.printLine("Starting Simulation...");
		
		 try {
			 
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
         	boolean trace_flag = false;  // mean trace events

         	// Initialize the CloudSim library
         	
         	CloudSim.init(num_user, calendar, trace_flag);
         	
         	Datacenter datacenter1 = createDatacenter("Datacenter_1");
         	
         	OilBroker broker1 = createBroker();
        	int brokerId = broker1.getId();
        	
        	int mips = 2000;
        	vmlist = createVM(brokerId, mips , 4, 0);
        	taskList = createTasks(brokerId, 20, 0,3000,5000);
        	
        	
        	broker1.submitVmList(vmlist);
			broker1.submitTaskList(taskList);
			
			
			
			CloudSim.startSimulation();


        	// Final step: Print results when simulation is over
        	List<Tasks> newList = broker1.getCloudletReceivedList();

        	CloudSim.stopSimulation();

        	printCloudletList(newList);

        	Log.printLine("Simulation finished!");
			 
			 
		 }
		 catch (Exception e) {
	            e.printStackTrace();
	            Log.printLine("The simulation has been terminated due to an unexpected error");
	        }

	}
	
	/** A method that returns a hashmap with the distributions for all the tasks just ran.
	 * @param list
	 * @return
	 */

	private static void getDistribution(List<Tasks> list) {
		
		Tasks cloudlet;
		int size = list.size();
			
		for(int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				String key = cloudlet.getTaskType() + "." + (cloudlet.getResourceId()-2);
				
				double completionTime = cloudlet.getFinishTime() - cloudlet.getArrivalTime(); // current completion time
				// 
				executionTimes.put(key, completionTime);	
			}
		}
		
		for(String taskTbaseST : executionTimes.keySet()) {
			
			List<Double> times = executionTimes.get(taskTbaseST);
			double sum = 0;
			double sqsum = 0;
			for(Double time: times) {
				sum += time;
			}
			double mu = sum/times.size();
			try {
			if (mu == sum) {
				 double sigma = 0.0;
				 NormalDistribution distr = new NormalDistribution(mu, sigma);
					distributions.put(taskTbaseST, distr);
			}
			
			else {
				for(Double time: times) {
					sqsum += Math.pow(time-mu, 2);
				}
				double sigma = Math.sqrt(sqsum/(times.size()-1));
				NormalDistribution distr = new NormalDistribution(mu, sigma);
				distributions.put(taskTbaseST, distr);
			}
			}catch( NotStrictlyPositiveException exp) {
				}
			}
		}
	
	
	
	private static Datacenter createDatacenter(String name){
    	//machine
    	List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
    	List<Pe> peList = new ArrayList<Pe>();

    	int mips = 8000;

        // 3. Create PEs and add these into a list.
    	peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
    	peList.add(new Pe(1, new PeProvisionerSimple(mips)));
    	peList.add(new Pe(2, new PeProvisionerSimple(mips)));
    	peList.add(new Pe(3, new PeProvisionerSimple(mips)));
    	peList.add(new Pe(4, new PeProvisionerSimple(mips)));
    	peList.add(new Pe(5, new PeProvisionerSimple(mips)));
    	peList.add(new Pe(6, new PeProvisionerSimple(mips)));
    	peList.add(new Pe(7, new PeProvisionerSimple(mips)));
    	
        //4. Create Host with its id and list of PEs and add them to the list of machines
        int hostId=0;
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerTimeShared(peList)
    			)
    		); // This is our machine


        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
	
	
	private static OilBroker createBroker(){

	    	OilBroker broker = null;
	        try {
			broker = new OilBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	    	return broker;
	 }
	
	

    /**
     * Prints the Cloudlet objects
     * @param list  list of Cloudlets
     */
    private static void printCloudletList(List<Tasks> list) {
        int size = list.size();
        Tasks task;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + indent +
                "Data center ID" + indent + indent+ "VM ID" + indent + indent+ "Length"+indent+indent+"Type" +indent + indent+ "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "ArrivalTime");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
        	task = list.get(i);
            Log.print(indent + task.getCloudletId() + indent + indent);

            if (task.getCloudletStatus() == Cloudlet.SUCCESS){
                Log.print("SUCCESS");

            	Log.printLine( indent + indent + task.getResourceName(task.getResourceId())+ indent + indent + indent + task.getVmId() +
                     indent + indent + indent +task.getCloudletTotalLength() +indent+indent + task.getTaskType() +indent+indent+indent +dft.format(task.getActualCPUTime()) + indent + indent + dft.format(task.getExecStartTime())+
                         indent + indent +"  " +dft.format(task.getFinishTime())+indent+indent+indent+task.getArrivalTime());
            }
        }

    }
	

}
