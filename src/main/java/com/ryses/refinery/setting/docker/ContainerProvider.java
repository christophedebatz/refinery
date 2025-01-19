package com.ryses.refinery.setting.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketException;
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
                    .withShowAll(true)
                    .withFilter("name", List.of("php-*"))
                    .exec()
            ;
        } catch (IOException e) {
            throw new RuntimeException("Docker seems to be closed. Nested error is " + e.getMessage(), e);
        }

        return containers;
    }

    public boolean isContainerRunning(String id) {
        try (DockerClient dockerClient = DockerClientBuilder.getInstance().build()) {
            var inspection = dockerClient.inspectContainerCmd(id).exec();
            return Boolean.TRUE.equals(inspection.getState().getRunning());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
