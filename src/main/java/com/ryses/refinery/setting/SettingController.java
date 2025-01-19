package com.ryses.refinery.setting;

import com.ryses.refinery.setting.docker.ContainerProvider;
import com.ryses.refinery.setting.docker.ProjectContainerCalculator;
import com.ryses.refinery.setting.docker.status.ContainerStateSubscriber;
import com.ryses.refinery.setting.docker.status.domain.ContainerStatus;
import com.ryses.refinery.setting.docker.status.domain.ProjectContainerState;
import com.ryses.refinery.setting.domain.ProjectCellContext;
import com.ryses.refinery.setting.dto.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Controller
public class SettingController implements Initializable {

    private final ContainerProvider containerProvider;
    private final SettingService settingService;
    private final ProjectContainerCalculator containerCalculator;
    private final ContainerStateSubscriber statusSubscriber;

    @FXML
    private ListView<ProjectCellController> projectsList;

    @Autowired
    public SettingController(ContainerProvider containerProvider, SettingService settingService, ProjectContainerCalculator containerCalculator, ContainerStateSubscriber statusSubscriber) {
        this.containerProvider = containerProvider;
        this.settingService = settingService;
        this.containerCalculator = containerCalculator;
        this.statusSubscriber = statusSubscriber;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeProjects();

        projectsList.setCellFactory(_ -> new ListCell<>() {

            @Override
            protected void updateItem(ProjectCellController item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty && item != null) {
                    setGraphic(item.getContainer());

                    if (getIndex() == 0) {
                        getStyleClass().add("first-item");
                    } else {
                        getStyleClass().remove("first-item");
                    }
                }
            }
        });
    }

    private void initializeProjects() {
        settingService.get().ifPresent(setting -> {
            var projects = setting.getProjects()
                    .stream()
                    .sorted(Comparator.comparing(Project::getName))
                    .toList()
            ;

            var controllers = new ArrayList<ProjectCellController>();
            var versionedContainers = containerCalculator.calculate();

            for (Project project : projects) {
                var states = new ArrayList<ProjectContainerState>();

                if (versionedContainers.containsKey(project.getName())) {
                    var containers = versionedContainers.get(project.getName());

                    containers.forEach(container -> {
                        var running = containerProvider.isContainerRunning(container.getContainer().getId());
                        states.add(new ProjectContainerState(container, running ? ContainerStatus.State.Up : ContainerStatus.State.Shutdown));
                    });

                    var itemContext = new ProjectCellContext(project, containers, states, false);
                    controllers.add(CellControllerFactory.getObject(itemContext, this::onProjectDirectoryChosen));
                }
            }

            projectsList.setItems(FXCollections.observableList(controllers.stream().sorted().collect(Collectors.toList())));
        });
    }

    private void onProjectDirectoryChosen(Project project, File directory) {
        if (project == null || settingService.get().isEmpty()) {
            return;
        }

        var setting = settingService.get().get();
        var clonedProject = new Project(project.getName(), directory.getAbsolutePath(), project.getRepository(), project.getDocker());
        var projects = setting.getProjects();
        projects.remove(project);
        projects.add(clonedProject);
        settingService.accept(setting);

        this.initializeProjects();
        projectsList.refresh();
    }
}
