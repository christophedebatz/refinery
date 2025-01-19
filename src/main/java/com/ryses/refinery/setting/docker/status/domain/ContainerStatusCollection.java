package com.ryses.refinery.setting.docker.status.domain;

import java.util.ArrayList;
import java.util.Collection;

public class ContainerStatusCollection {
    private final Collection<ContainerStatus> containerStatuses = new ArrayList<>();

    public void add(ContainerStatus containerStatus) {
        this.containerStatuses.add(containerStatus);
    }

    public ContainerStatus getStatusByContainerId(String containerId) {
        for (ContainerStatus containerStatus : containerStatuses) {
            if (containerStatus.getContainerId().contentEquals(containerId)) {
                return containerStatus;
            }
        }

        return null;
    }
}
