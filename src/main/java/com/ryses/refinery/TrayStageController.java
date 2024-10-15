package com.ryses.refinery;

import com.ryses.refinery.setting.SettingService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class TrayStageController implements Initializable {
    private final ApplicationContext applicationContext;
    private final SettingService settingService;

    @FXML
    private Button closeButton;

    @FXML
    private TabPane tabs;

    @Autowired
    public TrayStageController(ApplicationContext applicationContext, SettingService settingService) {
        this.applicationContext = applicationContext;
        this.settingService = settingService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.settingService
            .onSettingLoaded((valid, _) -> {
                if (!valid) {
                    for (Tab tab : tabs.getTabs()) {
                        if (tab.getText().contains("Setting")) {
                            tabs.getSelectionModel().select(tab);
                            break;
                        }
                        tab.disableProperty().set(true);
                    }
                }
            }
        );

        closeButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, _ -> System.exit(0));
    }

    public void display(Stage stage) {
        var logoUrl = RefinerySpringApp.class.getResource("logo.png");
        var image = Toolkit.getDefaultToolkit().getImage(logoUrl);
        var trayIcon = new TrayIcon(image);
        var tray = SystemTray.getSystemTray();

        stage.getIcons().add(
            new Image(Objects.requireNonNull(RefinerySpringApp.class.getResourceAsStream("logo.png")))
        );

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Platform.runLater(() -> this.show(e));
            }

            private void show(MouseEvent e) {
                if (stage.isShowing()) {
                    stage.hide();
                    return;
                }

                stage.setX(e.getX() - stage.getWidth() / 2);
                stage.setY(e.getY());
                stage.toFront();
                stage.show();

                stage.focusedProperty().addListener((o, wasFocused, isFocused) -> {
                    if (isFocused) {
                        stage.show();
                    }
                });

                stage.getScene().setOnKeyReleased((KeyEvent event) -> {
                    if (KeyCode.ESCAPE == event.getCode() && stage.isShowing()) {
                        stage.hide();
                    }
                });
            }
        });
    }
}
