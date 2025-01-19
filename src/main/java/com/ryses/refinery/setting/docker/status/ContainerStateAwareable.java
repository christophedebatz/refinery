package com.ryses.refinery.setting.docker.status;

import com.ryses.refinery.setting.docker.status.domain.ContainerStatus;

public interface ContainerStateAwareable {
    ContainerStatus.State getState();
}
