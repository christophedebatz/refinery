package com.ryses.refinery.setting.docker.status.domain;

import com.ryses.refinery.setting.docker.domain.VersionedProjectContainer;
import com.ryses.refinery.setting.docker.status.ContainerStateAwareable;

public class ProjectContainerState extends VersionedProjectContainer implements ContainerStateAwareable {

    private final ContainerStatus.State state;

    public ProjectContainerState(VersionedProjectContainer projectContainer, ContainerStatus.State state) {
        super(projectContainer.getContainer(), projectContainer.getVersion());
        this.state = state;
    }

    @Override
    public ContainerStatus.State getState() {
        return state;
    }
}
