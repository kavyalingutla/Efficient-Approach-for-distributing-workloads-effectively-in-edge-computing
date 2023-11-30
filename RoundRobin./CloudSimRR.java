package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
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
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An example showing how to create scalable simulations.
 */
public class CloudSimExample6 {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmlist;
    private static RoundRobinLoadBalancer roundRobinLoadBalancer;

    private static List<Vm> createVM(int userId, int vms) {
        LinkedList<Vm> list = new LinkedList<Vm>();

        long size = 10000; // image size (MB)
        int ram = 8192; // vm memory (MB) - reduced to fit wi`	thin available resources
        int mips = 1000;
        long bw = 100;  
        int pesNumber = 2; // number of cpus
        String vmm = "Xen"; // VMM name

        Vm[] vm = new Vm[vms];

        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets) {
        LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

        long length = 500000;
        long fileSize = 3000;
        long outputSize = 50000;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];

        Random r = new Random();

        for (int i = 0; i < cloudlets; i++) {
            cloudlet[i] = new Cloudlet(i, length + r.nextInt(2000), pesNumber, fileSize, outputSize, utilizationModel,
                    utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }

    public static void main(String[] args) {
        Log.printLine("Starting CloudSimExample with Round Robin Load Balancing...");

        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(num_user, calendar, trace_flag);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");

            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            vmlist = createVM(brokerId, 7);
            cloudletList = createCloudlet(brokerId, 30);

            roundRobinLoadBalancer = new RoundRobinLoadBalancer();

            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("CloudSimExample with Round Robin Load Balancing finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<Host>();

        List<Pe> peList1 = new ArrayList<Pe>();
        int mips1 = 1000; // Change this value as needed

        for (int peCount = 0; peCount < 19; peCount++) {
            peList1.add(new Pe(peCount, new PeProvisionerSimple(mips1)));
        }

        List<Pe> peList2 = new ArrayList<Pe>();
        int mips2 = 1200; // Change this value as needed

        for (int peCount = 0; peCount < 36; peCount++) {
            peList2.add(new Pe(peCount, new PeProvisionerSimple(mips2)));
        }

        int hostId = 0;
        int ram = 30720;
        long storage =  1000000000;
        int bw = 2000000;

        for (int hostCount = 0; hostCount < 5; hostCount++) {
            hostList.add(new Host(hostCount, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage,
                    peList1, new VmSchedulerSpaceShared(peList1)));
            hostId = hostCount;
        }
        hostId++;

        for (int hostCount = hostId + 1; hostCount < hostId + 1 + 3; hostCount++) {
            hostList.add(new Host(hostCount, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage,
                    peList2, new VmSchedulerSpaceShared(peList2)));
        }

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.1;
        double costPerBw = 0.1;
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
                cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList),
                    storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent
                + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");

        double totalExecutionTime = 100;
        double makespan = 0;
        int successfullyExecutedCloudlets = 0;

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                double executionTime = cloudlet.getFinishTime() - cloudlet.getExecStartTime();
                totalExecutionTime +=   executionTime;

                // Update makespan
                if (cloudlet.getFinishTime() > makespan) {
                    makespan = cloudlet.getFinishTime();
                }

                // Increment the count of successfully executed cloudlets
                successfullyExecutedCloudlets++;

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + indent + dft.format(executionTime) + indent + indent
                        + dft.format(cloudlet.getExecStartTime()) + indent + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }

        // Calculate throughput and print
        double throughput = successfullyExecutedCloudlets / makespan;
        Log.printLine();
        Log.printLine("Total Execution Time: " + dft.format(totalExecutionTime));
        Log.printLine("Makespan: " + dft.format(makespan));
        Log.printLine("Throughput: " + dft.format(throughput) + " cloudlets per unit of time");
    }


}


