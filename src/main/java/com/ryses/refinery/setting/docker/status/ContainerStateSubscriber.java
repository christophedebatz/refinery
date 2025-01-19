package com.ryses.refinery.setting.docker.status;

import com.ryses.refinery.setting.docker.ContainerProvider;
import com.ryses.refinery.setting.docker.domain.VersionedProjectContainer;
import com.ryses.refinery.setting.docker.status.domain.ContainerStatus;
import com.ryses.refinery.setting.docker.status.domain.ContainerStatusCollection;
import com.ryses.refinery.setting.dto.Project;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class ContainerStateSubscriber {

    private final ContainerProvider containerProvider;
    private final Map<String, ContainerStatus> activeContainers = new HashMap<>();

    @Autowired
    public ContainerStateSubscriber(ContainerProvider containerProvider) {
        this.containerProvider = containerProvider;
    }

    public void subscribe(Project project, Collection<VersionedProjectContainer> containers, Consumer<ContainerStatusCollection> onStatusChanged) {
        var uniqueContainers = new ArrayList<VersionedProjectContainer>();

        for (VersionedProjectContainer container : containers) {
            var containerId = container.getContainer().getId();

            if (!activeContainers.containsKey(containerId)) {
                uniqueContainers.add(container);
                activeContainers.put(containerId, new ContainerStatus(containerId, containerProvider.isContainerRunning(containerId) ? ContainerStatus.State.Up : ContainerStatus.State.Shutdown, project, container.getVersion()));
            }
        }

        final ScheduledService<ContainerStatusCollection> service = new ScheduledService<>() {

            @Override
            protected Task<ContainerStatusCollection> createTask() {
                return new ContainerStatusTask(project, uniqueContainers, container -> {
                    var containerId = container.getId();
                    var running = containerProvider.isContainerRunning(containerId);
                    return running ? ContainerStatus.State.Up : ContainerStatus.State.Shutdown;
                });
            }
        };

        service.setOnSucceeded(_ -> {
            var containerStatuses = service.getValue();
            activeContainers.forEach((id, status) -> {
                var newStatus = containerStatuses.getStatusByContainerId(id);
                if (newStatus != null && status != null && !newStatus.getState().equals(status.getState())) {
                    activeContainers.put(id, newStatus);
                    onStatusChanged.accept(service.getValue());
                }
            });
        });
        service.setOnFailed(_ -> System.out.println(service.getException().getMessage()));
        service.setPeriod(Duration.seconds(3));

        service.start();
    }
}
