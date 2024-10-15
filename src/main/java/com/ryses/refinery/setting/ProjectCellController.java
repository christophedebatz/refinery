package com.ryses.refinery.setting;

import com.ryses.refinery.setting.docker.domain.ProjectContainer;
import com.ryses.refinery.setting.dto.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
class ProjectCellController implements Comparable<ProjectCellController>
{
    private Map<String, Button> versions = new HashMap<>();
    private VBox container;
    private Label name;

    public ProjectCellController(Collection<ProjectContainer> containers, Consumer<ObservableList<Project>> onChange) {
        if (containers.isEmpty()) {
            throw new RuntimeException("No project container found");
        }

        var project = containers.stream().findFirst().get().getProject();

        this.container = new VBox();
        this.container.setFillWidth(true);
        this.name = new Label(project.getName());
        this.name.setStyle("-fx-font-weight: bold");

        var browseButton = new Button("Browse...");
        var truncatedPath = project.getPath();

        if (project.getPath() != null) {
            var pathLength = project.getPath().length();
            truncatedPath = pathLength > 50 ? "..." + project.getPath().substring(pathLength - 50, pathLength) : project.getPath();
        }

        var path = new Label(truncatedPath);
        path.getStyleClass().add("small");

        var icon = project.isNotSet() ? FontAwesomeSolid.EXCLAMATION_CIRCLE : FontAwesomeSolid.CHECK_CIRCLE;
        var warning = new Button();
        warning.setGraphic(FontIcon.of(icon, Color.WHITE));
        warning.setStyle("-fx-cursor: none");

        if (project.isNotSet()) {
            var warningTooltip = new Tooltip("Project directory not set.");
            warningTooltip.setShowDelay(Duration.millis(100));
            warning.setTooltip(warningTooltip);
        }

        var openFolder = new Button();
        openFolder.setGraphic(FontIcon.of(FontAwesomeSolid.FOLDER_OPEN, Color.WHITE));
        openFolder.setOnMouseClicked(_ -> {
            try {
                Desktop.getDesktop().open(new File(project.getPath()));
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

            containers.forEach(container -> {
                var version = container.getVersion().map(v -> String.format("v%s", v)).orElse("default");
                var openVersionButton = new Button(version);
                boolean opened = true;

                if (!this.versions.containsKey(version)) {
                    this.versions.put(version, openVersionButton);
                    children.add(this.versions.get(version));
                } else {
                    if (this.versions.get(version) == null) {
                        this.versions.put(version, openVersionButton);
                        children.add(this.versions.get(version));
                    } else {
                        children.remove(this.versions.get(version));
                        this.versions.remove(version);
                        opened = false;
                    }
                }

                if (opened) {
                    this.versions.get(version).setOnMouseClicked(_ -> {
                        try {
                            String command = "/usr/bin/open -a Terminal " + project.getPath() + "/" + version;
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
            dirChooser.setTitle("Base directory for " + project.getName());
            dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            var window = ((Node)e.getSource()).getScene().getWindow();
            File dir = dirChooser.showDialog(window);

            if (dir == null) {
                return;
            }

            var settingService = new SettingService();
            settingService.get().ifPresent(setting -> {
                var filteredProject = setting.getProjects()
                        .stream()
                        .filter(p -> p.getName().equals(project.getName()))
                        .findFirst()
                ;

                filteredProject.ifPresent(p -> {
                    var clonedProject = new Project(p.getName(), dir.getAbsolutePath(), p.getRepository(), p.getDocker());
                    var projects = setting.getProjects();
                    projects.remove(filteredProject.get());
                    projects.add(clonedProject);
                    settingService.accept(setting);
                    onChange.accept(FXCollections.observableList(projects.stream().toList()));
                });
            });
        });
    }

    @Override
    public int compareTo(ProjectCellController project) {
        return this.name.getText().compareTo(project.getName().getText());
    }
}
