package com.ryses.refinery.setting.domain;

import com.ryses.refinery.setting.docker.domain.VersionedProjectContainer;
import com.ryses.refinery.setting.docker.status.domain.ProjectContainerState;
import com.ryses.refinery.setting.dto.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class ProjectCellContext {
    private Project project;
    private Collection<VersionedProjectContainer> projectContainers;
    private Collection<ProjectContainerState> containerStates;
    private boolean versionOpened;
}
