package com.ryses.refinery.setting.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
public class ContainerProvider {

    private Collection<Container> containers = null;

    public Collection<Container> provide() {
        if (containers != null) {
            return containers;
        }

        try (DockerClient dockerClient = DockerClientBuilder.getInstance().build()) {
            containers = dockerClient
                    .listContainersCmd()
                    .withShowAll(false)
                    .withFilter("name", List.of("php-*"))
                    .exec()
                    .stream().distinct().toList()
            ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return containers;
    }
}
