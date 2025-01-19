package com.ryses.refinery.setting;

import com.ryses.refinery.setting.docker.ProjectContainerCalculator;
import com.ryses.refinery.setting.docker.status.ContainerStateSubscriber;
import com.ryses.refinery.setting.domain.ProjectCellContext;
import com.ryses.refinery.setting.dto.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
class CellControllerFactory {

    private static Map<Project, ProjectCellController> controllers;
    private static ContainerStateSubscriber stateSubscriber;
    private static ProjectContainerCalculator containerCalculator;

    @Autowired
    private CellControllerFactory(ContainerStateSubscriber stateSubscriber, ProjectContainerCalculator containerCalculator) {
        CellControllerFactory.stateSubscriber = stateSubscriber;
        CellControllerFactory.containerCalculator = containerCalculator;
    }

    public static ProjectCellController getObject(ProjectCellContext itemContext, BiConsumer<Project, File> onProjectDirectoryChosen) {
        var cellController = new ProjectCellController(CellControllerFactory.containerCalculator, CellControllerFactory.stateSubscriber, itemContext, onProjectDirectoryChosen);

        if (controllers == null) {
            controllers = new HashMap<>();
            controllers.put(itemContext.getProject(), cellController);
            return cellController;
        }

        if (controllers.containsKey(itemContext.getProject())) {
            return controllers.get(itemContext.getProject());
        }

        controllers.put(itemContext.getProject(), cellController);
        return cellController;
    }
}