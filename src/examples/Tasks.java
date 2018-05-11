package examples;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

public class Tasks extends Cloudlet {

private double arrivalTime;
	
		private double deadline;
		
		private int taskType;
	
		public double getArrivalTime() {
			return arrivalTime;
		}
	
		public void setArrivalTime(double arrivalTime) {
			this.arrivalTime = arrivalTime;
		}
		
		public double getDeadline() {
			return deadline;
		}
	
		public void setDeadline(double deadline) {
			this.deadline = deadline;
		}
	
		public int getTaskType() {
			return taskType;
		}
	
		public void setTaskType(int taskType) {
			this.taskType = taskType;
		}

		
	
		//constructor
		public Tasks 	(	int cloudletId,
							final int taskType,
							long cloudletLength, 
							int pesNumber, 
							long cloudletFileSize,
							long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
							UtilizationModel utilizationModelBw, 
							final double deadline,
							final double arrivalTime) {
							super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
							utilizationModelRam, utilizationModelBw, false);
			
			
			this.taskType = taskType;
			this.arrivalTime = arrivalTime;
			this.deadline = deadline;
			
		}

}
