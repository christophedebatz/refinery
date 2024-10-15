package com.ryses.refinery.setting.docker.domain;

import com.github.dockerjava.api.model.Container;
import com.ryses.refinery.setting.dto.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@AllArgsConstructor
@Getter
@ToString
public class ProjectContainer {
    private Project project;
    private Container container;
    private Optional<String> version;
}
