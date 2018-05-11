package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

public class OilBroker extends DatacenterBroker{
	
	
	/** The vtasks list. */
	protected List<? extends Tasks> taskList;
	
	
	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> taskSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> taskReceivedList;

	/** The cloudlets submitted. */
	protected int tasksSubmitted;
	
	
	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		
		
		
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitTaskList(List<? extends Tasks> list) {
		
		if(list.isEmpty())
			System.out.println(" List does not have any element ");
		
		getTasksList().addAll(list);
	}
	
	

	public OilBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Tasks> List<T> getTasksList() {
		return (List<T>) cloudletList;
	}
	
	
	
	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Tasks> void setTasksList(List<T> taskList) {
		this.cloudletList = taskList;
	}
	
	
	
	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Tasks> List<T> getTasksSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	
	
	
	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		int vmIndex = 0;
		int delay = 0; // delay for submission cloudlets
		Random randomDelay = new Random();// random delay generator object
		
		for (Tasks cloudlet : getTasksList()) {// using the sorted array ... edited by razin
			Vm vm;
			
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "+ cloudlet.getCloudletId() + " to VM #" + vm.getId());

			cloudlet.setVmId(vm.getId()); 
			
			schedule(getVmsToDatacentersMap().get(vm.getId()),cloudlet.getArrivalTime(),CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		  
		}
		
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
		
		
		
	}

}
