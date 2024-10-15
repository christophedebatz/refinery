package com.ryses.refinery.setting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ryses.refinery.setting.dto.Project;
import com.ryses.refinery.setting.dto.Setting;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class SettingService implements Consumer<Setting>, Supplier<Optional<Setting>>
{
    private final String REFINERY_HOME = ".refinery";
    private final String SETTING_FILENAME = "refinery.json";

    private static Optional<Setting> setting = Optional.empty();

    @Override
    public Optional<Setting> get() {
        this.initialize();

        if (setting.isPresent()) {
            return setting;
        }

        var path = Paths.get(getSettingFilename());

        try {
            setting = Optional.of(new ObjectMapper().readValue(path.toFile(), Setting.class));
        } catch (Throwable e) {
            return Optional.empty();
        }

        return setting;
    }

    @Override
    public void accept(Setting setting) {
        SettingService.setting = Optional.of(setting);

        try {
            var writer = new BufferedWriter(new FileWriter(getSettingFilename()));
            var mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            writer.write(mapper.writeValueAsString(setting));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onSettingLoaded(BiConsumer<Boolean, Optional<Setting>> callback) {
        var settings = get();
        var notSetProjectsCount = settings
                .map(setting -> setting.getProjects().stream().filter(Project::isNotSet).count())
                .orElse(1L)
        ;

        callback.accept(notSetProjectsCount == 0, settings);

    }

    private void initialize() {
        if (Files.exists(Paths.get(getSettingFilename()))) {
            return;
        }

        try {
            var directory = Path.of(System.getProperty("user.home") + File.separator + REFINERY_HOME);

            if (!Files.isDirectory(directory)) {
                Files.createDirectory(directory);
            }

            var file = SettingService.class.getResource("refinery.json");

            if (file != null) {
                Files.copy(Path.of(file.toURI()), Paths.get(getSettingFilename()));
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSettingFilename() {
        return String.format("%s%s%s%s%s", System.getProperty("user.home"), File.separator, REFINERY_HOME, File.separator, SETTING_FILENAME);
    }
}
