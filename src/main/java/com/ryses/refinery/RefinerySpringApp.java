package com.ryses.refinery;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;

@SpringBootApplication
public class RefinerySpringApp extends Application {

    @Autowired
    private TrayStageController trayController;

    private Parent root;
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        springContext = new SpringApplicationBuilder(RefinerySpringApp.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run()
        ;

        springContext
                .getAutowireCapableBeanFactory()
                .autowireBeanProperties(
                        this,
                        AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                        true
                )
        ;

        var fxmlLoader = new FXMLLoader(getClass().getResource("tray.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        root = fxmlLoader.load();
    }

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);

        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        var stage = new Stage();
        stage.setTitle("Refinery");
        stage.initOwner(primaryStage);
        stage.setWidth(400);
        stage.setHeight(450);
        stage.setIconified(true);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(new Scene(root, Color.TRANSPARENT));
        setTaskbarIcon();

        springContext.getBeanFactory().registerSingleton("firstStage", stage);
        trayController.display(stage);
    }

    @Override
    public void stop() throws Exception {
        springContext.stop();
    }

    private void setTaskbarIcon() {
        if (Taskbar.isTaskbarSupported()) {
            var taskbar = Taskbar.getTaskbar();

            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                var dockIcon = defaultToolkit.getImage(getClass().getResource("icon.png"));
                taskbar.setIconImage(dockIcon);
            }
        }
    }
}