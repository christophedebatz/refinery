package com.ryses.refinery.setting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
public class Setting {

    private Collection<Project> projects = new ArrayList<>();

    public Setting(Collection<Project> projects) {
        this.projects = projects;
    }
}
