package org.kiwiproject.config;

import static org.kiwiproject.collect.KiwiMaps.isNotNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

@Slf4j
public class ExternalPropertyProvider implements ConfigProvider {

    @VisibleForTesting
    static final Path DEFAULT_CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".config.properties");

    @Getter
    private Path propertiesPath;

    private Properties properties;

    public ExternalPropertyProvider() {
        this(DEFAULT_CONFIG_PATH);
    }

    public ExternalPropertyProvider(Path configPath) {
        setPropertiesPath(configPath);
    }

    @Override
    public boolean canProvide() {
        return isNotNullOrEmpty(properties);
    }

    private void setPropertiesPath(Path configPath) {
        this.propertiesPath = configPath;
        updateProperties();
    }

    private void updateProperties() {
        properties = new Properties();

        if (Files.isReadable(propertiesPath)) {
            try {
                LOG.debug("Looking up configuration values from file {}", propertiesPath);
                properties.load(Files.newBufferedReader(propertiesPath));
            } catch (IOException e) {
                LOG.error("Unable to load properties from file: {}", propertiesPath, e);
            }
        }
    }

    public Optional<String> getProperty(String propertyKey) {
        return canProvide() ? Optional.ofNullable(properties.getProperty(propertyKey)) : Optional.empty();
    }

    public void usePropertyIfPresent(String propertyKey, Consumer<String> propertyValueConsumer, Runnable orElse) {
        var propertyValue = getProperty(propertyKey);
        propertyValue.ifPresentOrElse(propertyValueConsumer, orElse);
    }
}
