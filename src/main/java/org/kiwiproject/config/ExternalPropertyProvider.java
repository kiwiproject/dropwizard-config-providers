package org.kiwiproject.config;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.collect.KiwiMaps.isNotNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Property provider that looks up configuration values from a known properties file. This provider loads the properties
 * so that other providers can access the values. The default config file is {@code .config.properties} in the executing
 * user's home directory.
 */
@Slf4j
public class ExternalPropertyProvider implements ConfigProvider {

    @VisibleForTesting
    static final Path DEFAULT_CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".kiwi.external.config.properties");

    @VisibleForTesting
    static final String DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY = "kiwi-external-config-path";

    @Getter(AccessLevel.PACKAGE)
    private Path propertiesPath;

    private Properties properties;

    /**
     * Creates the provider with the default config path.
     * <p>
     * This default will first look to see if there is a system property set with the full path, if not then the path
     * will be set to the {@link ExternalPropertyProvider#DEFAULT_CONFIG_PATH}.
     */
    public ExternalPropertyProvider() {
        var pathFromProps = System.getProperty(DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY);
        if (isNotBlank(pathFromProps)) {
            setPropertiesPath(Path.of(pathFromProps));
        } else {
            setPropertiesPath(DEFAULT_CONFIG_PATH);
        }
    }

    /**
     * Creates the provider with a given config path
     * @param configPath the path to a properties file with the config values
     */
    public ExternalPropertyProvider(Path configPath) {
        setPropertiesPath(configPath);
    }

    /**
     * This provider can provide if the properties have been loaded from the given file
     *
     * @return {@code true} if properties are loaded, {@code false} otherwise.
     */
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

    /**
     * Returns a property for a given key if it exists otherwise an {@link Optional#empty()}.
     *
     * @param propertyKey the key of the property to look up
     * @return An {@link Optional} with the requested property or empty if not found
     */
    public Optional<String> getProperty(String propertyKey) {
        return canProvide() ? Optional.ofNullable(properties.getProperty(propertyKey)) : Optional.empty();
    }

    /**
     * Executes given consumer if the requested property is found, otherwise runs the {@code orElse} function.
     *
     * @param propertyKey           the key of the property to look up
     * @param propertyValueConsumer a consumer to process the value of the property if found
     * @param orElse                a runnable to execute if the property is not found
     */
    public void usePropertyIfPresent(String propertyKey, Consumer<String> propertyValueConsumer, Runnable orElse) {
        var propertyValue = getProperty(propertyKey);
        propertyValue.ifPresentOrElse(propertyValueConsumer, orElse);
    }
}
