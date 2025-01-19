package com.ryses.refinery.setting.docker.status.domain;

import com.ryses.refinery.setting.dto.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ContainerStatus {
    private final String containerId;
    private final State state;
    private final Project project;
    private final String version;

    public enum State {
        Up,
        Shutdown
    }
}
