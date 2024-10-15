package com.ryses.refinery.setting;

import com.ryses.refinery.setting.docker.ProjectContainerCalculator;
import com.ryses.refinery.setting.dto.Project;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

@Controller
public class SettingController implements Initializable {

    private final SettingService settingService;
    private final ProjectContainerCalculator containerCalculator;

    @FXML
    private ListView<ProjectCellController> projectsList;

    @Autowired
    public SettingController(SettingService settingService, ProjectContainerCalculator containerCalculator) {
        this.settingService = settingService;
        this.containerCalculator = containerCalculator;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingService.get().ifPresent(setting -> this.onProjectChanged(setting.getProjects()));

        projectsList.setCellFactory(_ -> new ListCell<>() {

            @Override
            protected void updateItem(ProjectCellController item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty && item != null) {
                    setGraphic(item.getContainer());
                }

                if (getIndex() == 0) {
                    this.getStyleClass().add("first-item");
                }
            }
        });
    }

    private void onProjectChanged(Collection<Project> projects) {
        var items = projectsList.getItems();
        items.clear();

        projects
            .stream()
            .map(project -> new ProjectCellController(containerCalculator.calculate(project), this::onProjectChanged))
            .sorted()
            .forEachOrdered(items::add);

        projectsList.refresh();
    }
}
