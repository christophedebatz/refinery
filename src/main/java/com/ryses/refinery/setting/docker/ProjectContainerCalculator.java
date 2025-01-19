package com.ryses.refinery.setting.docker;

import com.github.dockerjava.api.model.Container;
import com.google.common.collect.Lists;
import com.ryses.refinery.setting.SettingService;
import com.ryses.refinery.setting.docker.domain.VersionedProjectContainer;
import com.ryses.refinery.setting.dto.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ProjectContainerCalculator {

    private final ContainerProvider containerProvider;
    private final SettingService settingService;
    private Collection<Container> containers = null;

    @Autowired
    public ProjectContainerCalculator(ContainerProvider containerProvider, SettingService settingService) {
        this.containerProvider = containerProvider;
        this.settingService = settingService;
    }

    public HashMap<String, Collection<VersionedProjectContainer>> calculate() {
        var settings = this.settingService.get();

        if (settings.isEmpty()) {
            return null;
        }

        var projectContainers = new HashMap<String, Collection<VersionedProjectContainer>>();
        var dockerContainers = this.loadContainers();

        dockerContainers.forEach(container -> {
            var pattern = Pattern.compile("(service-)?(?<name>[a-zA-Z-_]+)(-v(?<version>[0-9]+))?-php");
            var matcher = pattern.matcher(container.getImage());

            while (matcher.find()) {
                var serviceName = Optional.ofNullable(matcher.group("name"));
                var serviceVersion = Optional.ofNullable(matcher.group("version"))
                        .map(version -> String.format("v%s", version))
                        .orElse("default")
                ;

                if (serviceName.isEmpty()) {
                    continue;
                }

                try {
                    settings.ifPresent(setting -> {
                        setting.getProjects().forEach(p -> {
                            var sanitizedName = p.getName().replace("-service", "");

                            if (sanitizedName.equalsIgnoreCase(serviceName.get())) {
                                var key = getKey(p.getName());
                                var versionedContainer = new VersionedProjectContainer(container, serviceVersion);

                                if (projectContainers.containsKey(key)) {
                                    var versions = projectContainers.get(key);
                                    versions.add(versionedContainer);
                                } else {
                                    projectContainers.put(key, Lists.newArrayList(versionedContainer));
                                }
                            }
                        });
                    });
                } catch (IllegalStateException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });

        return projectContainers;
    }

    private String getKey(String name) {
        return MessageFormat.format("{0}", name);
    }

    private Collection<Container> loadContainers() {
        if (null == containers) {
            containers = this.containerProvider.provide();
        }

        return containers;
    }
}
