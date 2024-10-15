package com.ryses.refinery.setting.docker;

import com.github.dockerjava.api.model.Container;
import com.ryses.refinery.setting.SettingService;
import com.ryses.refinery.setting.docker.domain.ProjectContainer;
import com.ryses.refinery.setting.dto.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class ProjectContainerCalculator {

    private final ContainerProvider containerProvider;
    private final SettingService settingService;
    private final Map<String, Collection<ProjectContainer>> projectContainers = new HashMap<>();

    @Autowired
    public ProjectContainerCalculator(ContainerProvider containerProvider, SettingService settingService) {
        this.containerProvider = containerProvider;
        this.settingService = settingService;
    }

    public Collection<ProjectContainer> calculate(Project project) {
        if (projectContainers.containsKey(project.getName())) {
            return projectContainers.get(project.getName());
        }

        var settings = this.settingService.get();

        if (settings.isEmpty()) {
            return Collections.emptyList();
        }

        var containers = this.containerProvider.provide();

        for (Container container : containers) {
            var pattern = Pattern.compile("(service-)?(?<name>[a-zA-Z-_]+)(-v(?<version>[0-9]+))?-php");
            var matcher = pattern.matcher(container.getImage());

            matcher.find();
            Optional<String> projectName = Optional.of(matcher.group("name"));
            Optional<String> projectVersion = Optional.ofNullable(matcher.group("version"));

            settings.get().getProjects().forEach(p -> {
                var sanitizedName = p.getName().replace("-service", "");

                if (sanitizedName.equalsIgnoreCase(projectName.get())) {
                    if (this.projectContainers.containsKey(p.getName())) {
                        var projects = this.projectContainers.get(p.getName());
                        projects.add(new ProjectContainer(p, container, projectVersion));
                        this.projectContainers.put(p.getName(), projects);
                    } else {
                        Collection<ProjectContainer> list = new ArrayList<>();
                        list.add(new ProjectContainer(p, container, projectVersion));
                        this.projectContainers.put(p.getName(), list);
                    }
                }
            });
        }

        return projectContainers.get(project.getName());
    }
}
