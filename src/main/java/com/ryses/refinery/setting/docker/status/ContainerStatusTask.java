package com.ryses.refinery.setting.docker.status;

import com.github.dockerjava.api.model.Container;
import com.ryses.refinery.setting.docker.domain.VersionedProjectContainer;
import com.ryses.refinery.setting.docker.status.domain.ContainerStatus;
import com.ryses.refinery.setting.docker.status.domain.ContainerStatusCollection;
import com.ryses.refinery.setting.dto.Project;
import javafx.concurrent.Task;

import java.util.Collection;
import java.util.function.Function;

public class ContainerStatusTask extends Task<ContainerStatusCollection> {

    private final Project project;
    private final Function<Container, ContainerStatus.State> statusComputer;
    private final Collection<VersionedProjectContainer> projectContainers;

    public ContainerStatusTask(Project project, Collection<VersionedProjectContainer> containers, Function<Container, ContainerStatus.State> statusComputer) {
        this.project = project;
        this.projectContainers = containers;
        this.statusComputer = statusComputer;
    }

    @Override
    protected ContainerStatusCollection call() throws Exception {
        var statuses = new ContainerStatusCollection();

        for (VersionedProjectContainer projectContainer : projectContainers) {
            var container = projectContainer.getContainer();
            var status = this.statusComputer.apply(container);
            statuses.add(new ContainerStatus(container.getId(), status, project, projectContainer.getVersion()));
        }

        return statuses;
    }
}
