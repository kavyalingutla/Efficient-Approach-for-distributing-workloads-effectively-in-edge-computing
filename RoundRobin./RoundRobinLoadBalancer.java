package org.cloudbus.cloudsim.examples;


import java.util.List;

public class RoundRobinLoadBalancer {
    private int currentIndex;

    public RoundRobinLoadBalancer() {
        currentIndex = 0;
    }

    public synchronized int getNextVmId(List<Integer> vmIds) {
        if (vmIds.isEmpty()) {
            throw new IllegalArgumentException("List of VM IDs is empty");
        }

        int nextVmId = vmIds.get(currentIndex);
        currentIndex = (currentIndex + 1) % vmIds.size();
        return nextVmId;
    }

   
}


