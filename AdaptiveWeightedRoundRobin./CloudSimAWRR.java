
package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class AdaptiveWeightedRoundRobin {

    private static final int MOVING_AVERAGE_WINDOW = 5;

    private List<Vm> vms;
    private Map<Integer, Double> vmWeights;
    private Map<Integer, List<Double>> vmExecutionTimes;

    public AdaptiveWeightedRoundRobin(List<Vm> vms) {
        this.vms = vms;
        this.vmWeights = new HashMap<>();
        this.vmExecutionTimes = new HashMap<>();

        // Initialize weights and execution times for each VM
        for (Vm vm : vms) {
            vmWeights.put(vm.getId(), 1.0); // Initial weight is 1.0 for all VMs
            vmExecutionTimes.put(vm.getId(), new ArrayList<>());
        }
    }

    public void scheduleCloudlet(Cloudlet cloudlet) {
        // Update VM weights based on the average execution time
        updateWeights(cloudlet);

        // Select the VM with the highest weight
        int selectedVmId = selectVm();

        // For demonstration purposes, let's print the selected VM ID
        System.out.println("AWRR Scheduled Cloudlet " + cloudlet.getCloudletId() + " to VM " + selectedVmId);
    }

    private void updateWeights(Cloudlet cloudlet) {
        int cloudletVmId = cloudlet.getVmId();

        // Update the moving average execution time for the VM of the scheduled cloudlet
        List<Double> executionTimes = vmExecutionTimes.get(cloudletVmId);
        executionTimes.add(cloudlet.getActualCPUTime());

        if (executionTimes.size() > MOVING_AVERAGE_WINDOW) {
            executionTimes.remove(0); // Remove the oldest value if the window size is exceeded
        }

        double movingAverage = calculateMovingAverage(executionTimes);
        vmWeights.put(cloudletVmId, movingAverage);
    }

    private double calculateMovingAverage(List<Double> values) {
        double sum = 0;
        for (Double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    private int selectVm() {
        // Select the VM with the highest weight, considering VM performance
        double maxWeight = -1;
        int selectedVmId = -1;

        for (Map.Entry<Integer, Double> entry : vmWeights.entrySet()) {
            // Consider VM performance factor (e.g., MIPS)
            double performanceFactor = calculatePerformanceFactor(entry.getKey());
            double weightedPerformance = entry.getValue() * performanceFactor;

            if (weightedPerformance > maxWeight) {
                maxWeight = weightedPerformance;
                selectedVmId = entry.getKey();
            }
            
        }

        return selectedVmId;
    }

    private double calculatePerformanceFactor(int vmId) {
        // Example: Use MIPS as a performance factor
        double mips = vms.get(vmId).getMips();
        return mips;
    }

}
