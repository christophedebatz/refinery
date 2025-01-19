package com.ryses.refinery.setting.docker.domain;

import com.github.dockerjava.api.model.Container;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class VersionedProjectContainer {
    private Container container;
    private String version;
}
