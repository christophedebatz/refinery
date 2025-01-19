package com.ryses.refinery.setting;

import com.ryses.refinery.setting.docker.ProjectContainerCalculator;
import com.ryses.refinery.setting.docker.status.ContainerStateSubscriber;
import com.ryses.refinery.setting.docker.status.domain.ContainerStatus;
import com.ryses.refinery.setting.domain.ProjectCellContext;
import com.ryses.refinery.setting.dto.Project;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import lombok.Getter;
import lombok.ToString;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
@ToString
class ProjectCellController implements Comparable<ProjectCellController>
{
    private final ProjectContainerCalculator containerCalculator;
    private final ContainerStateSubscriber stateSubscriber;
    private final ProjectCellContext context;
    private final VBox container;
    private final Label name;

    private final Map<String, Button> versions = new HashMap<>();

    public ProjectCellController(ProjectContainerCalculator containerCalculator, ContainerStateSubscriber stateSubscriber, ProjectCellContext context, BiConsumer<Project, File> onProjectDirectoryChosen) {
        this.containerCalculator = containerCalculator;
        this.stateSubscriber = stateSubscriber;
        this.context = context;
        var project = context.getProjectContainers();

        this.container = new VBox();
        this.container.setFillWidth(true);
        this.name = new Label(context.getProject().getName());
        this.name.setStyle("-fx-font-weight: bold");

        subscribeProjectState(status -> {
            System.out.println(
                MessageFormat.format("Container of {0}, version {1} is now {2}", status.getProject().getName(), status.getVersion(), status.getState().name()));
        });

        var browseButton = new Button("Browse...");
        var truncatedPath = context.getProject().getPath();

        if (truncatedPath != null) {
            var pathLength = truncatedPath.length();
            truncatedPath = pathLength > 50 ? "..." + truncatedPath.substring(pathLength - 50, pathLength) : truncatedPath;
        }

        var path = new Label(truncatedPath);
        path.getStyleClass().add("small");

        var icon = context.getProject().isNotSet() ? FontAwesomeSolid.EXCLAMATION_CIRCLE : FontAwesomeSolid.CHECK_CIRCLE;
        var warning = new Button();
        warning.setGraphic(FontIcon.of(icon, Color.WHITE));
        warning.setStyle("-fx-cursor: none");

        if (context.getProject().isNotSet()) {
            var warningTooltip = new Tooltip("Project directory not set.");
            warningTooltip.setShowDelay(Duration.millis(100));
            warning.setTooltip(warningTooltip);
        }

        var openFolder = new Button();
        openFolder.setGraphic(FontIcon.of(FontAwesomeSolid.FOLDER_OPEN, Color.WHITE));
        openFolder.setOnMouseClicked(_ -> {
            try {
                Desktop.getDesktop().open(new File(context.getProject().getPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        var detailsButton = new Button();
        detailsButton.setGraphic(FontIcon.of(FontAwesomeSolid.INFO_CIRCLE, Color.WHITE));

        var hBoxContainer = new HBox();
        var dockerSign = new Label();
        dockerSign.getStyleClass().add("small");
        dockerSign.setGraphic(FontIcon.of(FontAwesomeBrands.DOCKER, Color.WHITE));

        detailsButton.setOnMouseClicked(_ -> {
            var children = hBoxContainer.getChildren();

            if (children.contains(dockerSign)) {
                children.remove(dockerSign);
            } else {
                children.add(dockerSign);
            }

            this.context.setVersionOpened(true);

            context.getContainerStates().forEach(state -> {
                var openVersionButton = new Button(state.getVersion());

                if (!this.versions.containsKey(state.getVersion())) {
                    this.versions.put(state.getVersion(), openVersionButton);
                    children.add(this.versions.get(state.getVersion()));
                } else {
                    if (this.versions.get(state.getVersion()) == null) {
                        this.versions.put(state.getVersion(), openVersionButton);
                        children.add(this.versions.get(state.getVersion()));
                    } else {
                        children.remove(this.versions.get(state.getVersion()));
                        this.versions.remove(state.getVersion());
                        this.context.setVersionOpened(false);
                    }
                }

                if (this.context.isVersionOpened()) {
                    this.versions.get(state.getVersion()).setOnMouseClicked(_ -> {
                        try {
                            String command = "/usr/bin/open -a Terminal " + context.getProject().getPath() + "/" + state;
                            Runtime.getRuntime().exec(command);
                        } catch (IOException ex) {
                            // ignore
                        }
                    });
                }
            });

            var containerChildren = this.container.getChildren();

            if (!containerChildren.contains(hBoxContainer)) {
                containerChildren.add(hBoxContainer);
            }
        });

        var fileContainer = new HBox(7, browseButton, path, warning, openFolder, detailsButton);
        this.container.getChildren().add(name);
        this.container.getChildren().add(fileContainer);

        browseButton.setOnMouseClicked(e -> {
            var dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Base directory for " + context.getProject().getName());
            dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            var window = ((Node)e.getSource()).getScene().getWindow();
            var dir = dirChooser.showDialog(window);

            if (dir == null) {
                return;
            }

            onProjectDirectoryChosen.accept(context.getProject(), dir);
        });
    }

    @Override
    public int compareTo(ProjectCellController project) {
        return this.name.getText().compareTo(project.getName().getText());
    }

    private void subscribeProjectState(Consumer<ContainerStatus> onStateChanged) {
        var project = context.getProject();
        var versionedContainers = containerCalculator.calculate();

        if (versionedContainers.containsKey(project.getName())) {
            var containers = versionedContainers.get(project.getName());

            stateSubscriber.subscribe(project, containers, statuses -> {
                containers.forEach(container -> {
                    var status = statuses.getStatusByContainerId(container.getContainer().getId());
                    System.out.println("status of " + project.getName() + " version " + status.getVersion() + " is " + status);
                    onStateChanged.accept(status);
                });
            });
        }


    }
}
