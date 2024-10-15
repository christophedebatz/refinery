package com.ryses.refinery.setting.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"name"})
public class Project {
    private String name = null;
    private String path = null;
    private String repository = null;
    private ProjectDocker docker = null;

    public Project(Project project) {
        this.name = project.getName();
        this.path = project.getPath();
        this.repository = project.repository;
        this.docker = project.docker;
    }

    @JsonIgnore
    public boolean isNotSet() {
        return path == null || path.isEmpty();
    }
}
